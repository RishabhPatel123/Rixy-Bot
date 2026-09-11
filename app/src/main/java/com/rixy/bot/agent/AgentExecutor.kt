package com.rixy.bot.agent

import android.content.Context
import androidx.core.content.ContextCompat
import com.rixy.bot.network.GeminiParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/** One LLM round-trip: sends contents + tools, returns the raw response body. */
interface AgentTransport {
    suspend fun turn(contents: JSONArray, tools: JSONArray): String
}

/**
 * The agent loop: observe → decide (LLM) → gate (permission/confirmation) →
 * act → feed result back, until a final text answer or the step budget runs out.
 */
class AgentExecutor(
    private val appContext: Context,
    private val registry: ToolRegistry,
    private val transport: AgentTransport,
    /** Returns true when the tool's permission is (now) granted; may suspend on the user. */
    private val permissionGate: suspend (AgentTool) -> Boolean,
    /** Returns true when the user allowed this irreversible action; may suspend on the user. */
    private val confirmationGate: suspend (AgentTool, JSONObject) -> Boolean,
    /** Progress status for the UI, e.g. "Sending SMS…". */
    private val onAction: (String) -> Unit = {},
) {
    sealed interface Outcome {
        data class Reply(val text: String, val actions: List<String>) : Outcome
        data class Failed(val message: String) : Outcome
    }

    suspend fun run(history: List<Pair<Boolean, String>>, userRequest: String): Outcome {
        val contents = JSONArray()
        history.forEach { (isFromUser, text) ->
            if (text.isNotBlank()) {
                contents.put(
                    JSONObject()
                        .put("role", if (isFromUser) "user" else "model")
                        .put("parts", JSONArray().put(JSONObject().put("text", text)))
                )
            }
        }
        contents.put(
            JSONObject()
                .put("role", "user")
                .put("parts", JSONArray().put(JSONObject().put("text", userRequest)))
        )

        val tools = ToolJson.toolDeclarations(registry.all)
        val actionLog = mutableListOf<String>()

        repeat(MAX_STEPS) { step ->
            val body = transport.turn(contents, tools)
            when (val reply = GeminiParser.parseAgentReply(body)) {
                is com.rixy.bot.network.AgentReply.Text -> {
                    return if (reply.text.isBlank()) {
                        Outcome.Failed("The model returned an empty answer.")
                    } else {
                        Outcome.Reply(reply.text, actionLog)
                    }
                }
                is com.rixy.bot.network.AgentReply.ToolCalls -> {
                    // Echo the model's function calls back into history.
                    contents.put(
                        JSONObject()
                            .put("role", "model")
                            .put(
                                "parts",
                                JSONArray().apply {
                                    reply.calls.forEach { call ->
                                        put(GeminiParser.functionCallPart(call.name, call.args))
                                    }
                                },
                            )
                    )
                    reply.calls.forEach { call -> executeCall(call, contents, actionLog) }
                    if (step == MAX_STEPS - 1) {
                        // Budget exhausted: ask for a wrap-up without tools.
                        val final = runCatching { transport.turn(contents, JSONArray()) }
                            .getOrNull()
                            ?.let { GeminiParser.parseAgentReply(it) }
                        val text = (final as? com.rixy.bot.network.AgentReply.Text)?.text
                            ?: "Stopped after $MAX_STEPS steps. Actions so far: " +
                                actionLog.joinToString("; ").ifEmpty { "none" }
                        return Outcome.Reply(text, actionLog)
                    }
                }
            }
        }
        return Outcome.Failed("Agent stopped unexpectedly.")
    }

    private suspend fun executeCall(
        call: com.rixy.bot.network.FunctionCallData,
        contents: JSONArray,
        actionLog: MutableList<String>,
    ) {
        val tool = registry.find(call.name)
        if (tool == null) {
            respond(contents, call.name, JSONObject().put("status", "error").put("message", "Unknown tool."))
            return
        }
        onAction(tool.summarize(call.args))

        val permission = tool.requiredPermission
        if (permission != null &&
            ContextCompat.checkSelfPermission(appContext, permission) !=
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            val granted = permissionGate(tool)
            if (!granted) {
                respond(
                    contents, call.name,
                    JSONObject().put("status", "error").put("message", "User declined the permission. Tell them and stop.")
                )
                return
            }
        }

        if (tool.needsConfirmation(call.args)) {
            val allowed = confirmationGate(tool, call.args)
            if (!allowed) {
                respond(
                    contents, call.name,
                    JSONObject().put("status", "error").put("message", "User rejected this action. Do not retry it.")
                )
                return
            }
        }

        val result = withContext(Dispatchers.IO) {
            runCatching { tool.execute(appContext, call.args) }.getOrElse {
                ToolResult.Error(it.message ?: "Tool crashed.")
            }
        }
        val payload = when (result) {
            is ToolResult.Ok -> result.data.put("status", "ok")
            is ToolResult.Error -> JSONObject().put("status", "error").put("message", result.message)
        }
        respond(contents, call.name, payload)
        actionLog += if (result is ToolResult.Ok) {
            "✅ ${tool.summarize(call.args)}"
        } else {
            "⚠️ ${tool.summarize(call.args)} — ${(result as ToolResult.Error).message}"
        }
    }

    private fun respond(contents: JSONArray, name: String, payload: JSONObject) {
        contents.put(
            JSONObject()
                .put("role", "function")
                .put("parts", JSONArray().put(GeminiParser.functionResponsePart(name, payload)))
        )
    }

    companion object {
        const val MAX_STEPS = 6
    }
}
