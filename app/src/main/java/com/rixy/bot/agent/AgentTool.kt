package com.rixy.bot.agent

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Result of executing one tool. */
sealed interface ToolResult {
    data class Ok(val data: JSONObject) : ToolResult
    data class Error(val message: String) : ToolResult
}

/**
 * One capability the agent can execute on the device. Implementations must be
 * safe to call from any thread; the executor runs them on Dispatchers.IO.
 */
interface AgentTool {
    val name: String
    val description: String

    /** Gemini function parameters schema, e.g. {"type":"object","properties":{...},"required":[...]}. */
    val parametersSchema: JSONObject

    /** Runtime permission this tool needs, or null. */
    val requiredPermission: String? get() = null

    /** True when executing this with [args] has irreversible side effects. */
    fun needsConfirmation(args: JSONObject): Boolean = false

    /** Human-readable one-liner for the confirmation dialog. */
    fun summarize(args: JSONObject): String = name

    fun execute(context: Context, args: JSONObject): ToolResult
}

/** Helpers for building schemas and results. */
object ToolJson {
    fun schema(vararg properties: Pair<String, JSONObject>, required: List<String> = emptyList()): JSONObject {
        val props = JSONObject()
        properties.forEach { (key, value) -> props.put(key, value) }
        return JSONObject()
            .put("type", "object")
            .put("properties", props)
            .apply { if (required.isNotEmpty()) put("required", JSONArray(required)) }
    }

    fun stringProp(description: String) =
        JSONObject().put("type", "string").put("description", description)

    fun intProp(description: String) =
        JSONObject().put("type", "integer").put("description", description)

    fun ok(vararg fields: Pair<String, Any>): ToolResult =
        ToolResult.Ok(JSONObject().apply { fields.forEach { (k, v) -> put(k, v) } })

    fun error(message: String): ToolResult = ToolResult.Error(message)

    fun toolDeclarations(tools: List<AgentTool>): JSONArray =
        JSONArray().apply {
            tools.forEach { tool ->
                put(
                    JSONObject()
                        .put("functionDeclarations", JSONArray().put(
                            JSONObject()
                                .put("name", tool.name)
                                .put("description", tool.description)
                                .put("parameters", tool.parametersSchema)
                        ))
                )
            }
        }
}

/** All available agent tools, keyed by name. */
class ToolRegistry(tools: List<AgentTool>) {
    private val byName = tools.associateBy { it.name }
    val all: List<AgentTool> = tools

    fun find(name: String): AgentTool? = byName[name]
}
