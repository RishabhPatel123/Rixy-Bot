package com.rixy.bot

import android.content.Context
import com.rixy.bot.agent.AgentExecutor
import com.rixy.bot.agent.AgentTool
import com.rixy.bot.agent.ToolJson
import com.rixy.bot.agent.ToolRegistry
import com.rixy.bot.agent.ToolResult
import com.rixy.bot.network.AgentReply
import com.rixy.bot.network.GeminiParser
import kotlinx.coroutines.test.runTest
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class AgentTest {

    // ---- Function-call parsing ----

    @Test
    fun `parses function calls from a reply`() {
        val body = """
            {"candidates":[{"content":{"parts":[
              {"functionCall":{"name":"send_sms","args":{"number":"+15550001","message":"hi"}}}
            ]}}]}
        """.trimIndent()
        val reply = GeminiParser.parseAgentReply(body)
        assertTrue(reply is AgentReply.ToolCalls)
        val call = (reply as AgentReply.ToolCalls).calls.single()
        assertEquals("send_sms", call.name)
        assertEquals("+15550001", call.args.getString("number"))
    }

    @Test
    fun `parses plain text agent reply`() {
        val body = """{"candidates":[{"content":{"parts":[{"text":"Done!"}]}}]}"""
        val reply = GeminiParser.parseAgentReply(body)
        assertEquals(AgentReply.Text("Done!"), reply)
    }

    @Test
    fun `prefers tool calls when both text and calls present`() {
        val body = """
            {"candidates":[{"content":{"parts":[
              {"text":"let me check"},
              {"functionCall":{"name":"get_time","args":{}}}
            ]}}]}
        """.trimIndent()
        assertTrue(GeminiParser.parseAgentReply(body) is AgentReply.ToolCalls)
    }

    @Test
    fun `builds well-formed function response parts`() {
        val part = GeminiParser.functionResponsePart("get_time", JSONObject().put("status", "ok"))
        assertEquals("get_time", part.getJSONObject("functionResponse").getString("name"))
        assertEquals("ok", part.getJSONObject("functionResponse").getJSONObject("response").getString("status"))
    }

    @Test
    fun `tool declarations list every tool with a schema`() {
        val registry = ToolRegistry(listOf(FakeTool("t1"), FakeTool("t2")))
        val declarations = ToolJson.toolDeclarations(registry.all)
        assertEquals(2, declarations.length())
        val names = mutableListOf<String>()
        for (i in 0 until declarations.length()) {
            val fd = declarations.getJSONObject(i).getJSONArray("functionDeclarations")
            names += fd.getJSONObject(0).getString("name")
        }
        assertEquals(listOf("t1", "t2"), names)
    }

    // ---- Executor loop ----

    private open class FakeTool(
        override val name: String,
        private val result: ToolResult = ToolJson.ok("value" to 42),
    ) : AgentTool {
        override val description = "fake tool $name"
        override val parametersSchema = ToolJson.schema()
        override fun execute(context: Context, args: JSONObject) = result
    }

    /** Transport that replays scripted raw bodies. */
    private class ScriptedTransport(private val bodies: MutableList<String>) : com.rixy.bot.agent.AgentTransport {
        val calls = mutableListOf<Pair<JSONArray, JSONArray>>()
        override suspend fun turn(contents: JSONArray, tools: JSONArray): String {
            calls += contents to tools
            return bodies.removeAt(0)
        }
    }

    private fun toolCallBody(name: String, args: String = "{}"): String =
        """{"candidates":[{"content":{"parts":[{"functionCall":{"name":"$name","args":$args}}]}}]}"""

    private fun textBody(text: String): String =
        """{"candidates":[{"content":{"parts":[{"text":"$text"}]}}]}"""

    @Test
    fun `executes one tool then returns the reply`() = runTest {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<Context>()
        val tool = FakeTool("get_time")
        val transport = ScriptedTransport(mutableListOf(toolCallBody("get_time"), textBody("It is noon.")))
        var status: String? = null
        val executor = AgentExecutor(
            appContext = context,
            registry = ToolRegistry(listOf(tool)),
            transport = transport,
            permissionGate = { true },
            confirmationGate = { _, _ -> true },
            onAction = { status = it },
        )
        val outcome = executor.run(emptyList(), "what time is it?")
        assertTrue(outcome is AgentExecutor.Outcome.Reply)
        val reply = outcome as AgentExecutor.Outcome.Reply
        assertEquals("It is noon.", reply.text)
        assertEquals(1, reply.actions.size)
        assertTrue(reply.actions[0].contains("get_time"))
        assertEquals(2, transport.calls.size) // tool round + final round
        // The second turn's contents must include the functionResponse
        val secondContents = transport.calls[1].first
        val lastPart = secondContents.getJSONObject(secondContents.length() - 1)
        assertEquals("function", lastPart.getString("role"))
        status ?: error("onAction was never called")
    }

    @Test
    fun `rejected confirmation produces an error response, not an execution`() = runTest {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<Context>()
        var executed = false
        val tool = object : FakeTool("send_sms") {
            override fun needsConfirmation(args: JSONObject) = true
            override fun execute(context: Context, args: JSONObject): ToolResult {
                executed = true
                return ToolJson.ok("sent" to true)
            }
        }
        val transport = ScriptedTransport(mutableListOf(toolCallBody("send_sms"), textBody("ok, cancelled")))
        val executor = AgentExecutor(
            appContext = context,
            registry = ToolRegistry(listOf(tool)),
            transport = transport,
            permissionGate = { true },
            confirmationGate = { _, _ -> false },
        )
        val outcome = executor.run(emptyList(), "text mom")
        assertTrue(outcome is AgentExecutor.Outcome.Reply)
        assertTrue("tool must not run when rejected", !executed)
        val contents = transport.calls[1].first
        val payload = contents.getJSONObject(contents.length() - 1)
            .getJSONArray("parts").getJSONObject(0)
            .getJSONObject("functionResponse").getJSONObject("response")
        assertEquals("error", payload.getString("status"))
    }

    @Test
    fun `unknown tool yields an error response the model can recover from`() = runTest {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<Context>()
        val transport = ScriptedTransport(mutableListOf(toolCallBody("nonexistent"), textBody("recovered")))
        val executor = AgentExecutor(
            appContext = context,
            registry = ToolRegistry(listOf(FakeTool("real_tool"))),
            transport = transport,
            permissionGate = { true },
            confirmationGate = { _, _ -> true },
        )
        val outcome = executor.run(emptyList(), "go")
        assertEquals("recovered", (outcome as AgentExecutor.Outcome.Reply).text)
    }

    @Test
    fun `step budget exhaustion still produces a reply`() = runTest {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<Context>()
        val bodies = mutableListOf<String>().apply {
            repeat(AgentExecutor.MAX_STEPS) { add(toolCallBody("get_time")) }
            add(textBody("wrapped up"))
        }
        val transport = ScriptedTransport(bodies)
        val executor = AgentExecutor(
            appContext = context,
            registry = ToolRegistry(listOf(FakeTool("get_time"))),
            transport = transport,
            permissionGate = { true },
            confirmationGate = { _, _ -> true },
        )
        val outcome = executor.run(emptyList(), "loop forever")
        val reply = outcome as AgentExecutor.Outcome.Reply
        assertEquals("wrapped up", reply.text)
        assertEquals(AgentExecutor.MAX_STEPS, reply.actions.size)
    }
}
