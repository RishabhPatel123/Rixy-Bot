package com.rixy.bot

import com.rixy.bot.network.GeminiException
import com.rixy.bot.network.GeminiParser
import com.rixy.bot.ui.viewmodel.ChatViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class GeminiParserTest {

    @Test
    fun `extracts text from an SSE chunk`() {
        val payload = """
            {"candidates":[{"content":{"parts":[{"text":"Hello"},{"text":" world"}]}}]}
        """.trimIndent()
        assertEquals("Hello world", GeminiParser.extractStreamText(payload))
    }

    @Test
    fun `returns null for chunks without text`() {
        val payload = """{"candidates":[{"finishReason":"STOP"}]}"""
        assertNull(GeminiParser.extractStreamText(payload))
    }

    @Test
    fun `returns null for malformed json`() {
        assertNull(GeminiParser.extractStreamText("not json"))
    }

    @Test
    fun `extracts full response text`() {
        val body = """
            {"candidates":[{"content":{"parts":[{"text":"Reply body"}]}}]}
        """.trimIndent()
        assertEquals("Reply body", GeminiParser.extractResponseText(body))
    }

    @Test(expected = GeminiException.Parse::class)
    fun `throws on response without text`() {
        GeminiParser.extractResponseText("""{"candidates":[]}""")
    }

    @Test
    fun `parses a plan from fenced json`() {
        val reply = """
            Here is your plan:
            ```json
            [
              {"title": "Research", "description": "Gather sources", "priority": "HIGH"},
              {"title": "Draft", "description": "Write it up", "priority": "p2"},
              {"title": "Review", "description": "", "priority": ""}
            ]
            ```
        """.trimIndent()
        val plan = GeminiParser.parsePlan(reply)
        assertEquals(3, plan.size)
        assertEquals("Research", plan[0].title)
        assertEquals("HIGH", plan[0].priority)
        assertEquals("LOW", plan[1].priority) // p2 normalized
        assertEquals("MEDIUM", plan[2].priority) // blank normalized
    }

    @Test
    fun `parses a bare json array`() {
        val reply = """[{"title":"Only step","description":"Do it","priority":"medium"}]"""
        val plan = GeminiParser.parsePlan(reply)
        assertEquals(1, plan.size)
        assertEquals("MEDIUM", plan[0].priority)
    }

    @Test(expected = GeminiException.Parse::class)
    fun `throws when no array present`() {
        GeminiParser.parsePlan("Sorry, I can't plan that.")
    }

    @Test
    fun `plan with empty titles is dropped`() {
        val reply = """[{"title":"","description":"x","priority":"LOW"},{"title":"Real","description":"y","priority":"LOW"}]"""
        val plan = GeminiParser.parsePlan(reply)
        assertEquals(1, plan.size)
        assertEquals("Real", plan[0].title)
    }

    @Test
    fun `extracts nested brackets inside strings`() {
        val reply = """[{"title":"Use [brackets] in title","description":"d","priority":"LOW"}]"""
        val plan = GeminiParser.parsePlan(reply)
        assertEquals("Use [brackets] in title", plan[0].title)
    }

    @Test
    fun `derives chat titles from prompts`() {
        assertEquals("Hello", ChatViewModel.deriveTitle("Hello"))
        val long = "x".repeat(100)
        val title = ChatViewModel.deriveTitle(long)
        assertTrue(title.length <= 48 && title.endsWith("…"))
        assertEquals("New chat", ChatViewModel.deriveTitle("   \n "))
        assertNotNull(ChatViewModel.deriveTitle("multi\nline prompt"))
    }
}
