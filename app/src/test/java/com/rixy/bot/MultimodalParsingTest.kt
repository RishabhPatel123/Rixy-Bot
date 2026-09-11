package com.rixy.bot

import com.rixy.bot.network.GeminiException
import com.rixy.bot.network.GeminiParser
import com.rixy.bot.util.ConversationImportParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class MultimodalParsingTest {

    // ---- Grounding sources ----

    @Test
    fun `extracts grounding sources from a stream chunk`() {
        val payload = """
            {"candidates":[{"content":{"parts":[{"text":"answer"}]},
            "groundingMetadata":{"groundingChunks":[
              {"web":{"uri":"https://a.example/1","title":"Example One"}},
              {"web":{"uri":"https://a.example/2"}}
            ]}}]}
        """.trimIndent().replace("\n", "")
        val sources = GeminiParser.extractSources(payload)
        assertEquals(2, sources.size)
        assertEquals("Example One", sources[0].title)
        assertEquals("https://a.example/2", sources[1].uri) // empty title falls back to uri
        assertEquals("https://a.example/2", sources[1].title)
    }

    @Test
    fun `returns empty when chunk has no grounding metadata`() {
        val payload = """{"candidates":[{"content":{"parts":[{"text":"hi"}]}}]}"""
        assertTrue(GeminiParser.extractSources(payload).isEmpty())
        assertTrue(GeminiParser.extractSources("garbage").isEmpty())
    }

    // ---- Image generation response ----

    @Test
    fun `extracts inline image and caption`() {
        val body = """
            {"candidates":[{"content":{"parts":[
              {"text":"A cat in space."},
              {"inlineData":{"mimeType":"image/png","data":"aGVsbG8="}}
            ]}}]}
        """.trimIndent()
        val image = GeminiParser.extractImageAndCaption(body)
        assertEquals("aGVsbG8=", image.base64)
        assertEquals("image/png", image.mimeType)
        assertEquals("A cat in space.", image.caption)
    }

    @Test
    fun `accepts snake_case inline_data`() {
        val body = """
            {"candidates":[{"content":{"parts":[
              {"inline_data":{"mime_type":"image/jpeg","data":"AAA="}}
            ]}}]}
        """.trimIndent()
        val image = GeminiParser.extractImageAndCaption(body)
        assertEquals("image/jpeg", image.mimeType)
        assertEquals("", image.caption)
    }

    @Test(expected = GeminiException.Parse::class)
    fun `throws when no image present`() {
        GeminiParser.extractImageAndCaption("""{"candidates":[{"content":{"parts":[{"text":"no image"}]}}]}""")
    }

    // ---- Conversation import ----

    @Test
    fun `parses speaker-prefixed conversation`() {
        val raw = """
            You: what is Kotlin?
            Grok: Kotlin is a JVM language.
            It is concise and null-safe.
            You: nice
            Assistant: anything else?
        """.trimIndent()
        val parsed = ConversationImportParser.parse(raw)
        assertEquals(4, parsed.size)
        assertEquals(true, parsed[0].isFromUser)
        assertEquals("what is Kotlin?", parsed[0].text)
        assertEquals(false, parsed[1].isFromUser)
        assertTrue(parsed[1].text.contains("concise and null-safe"))
        assertEquals(true, parsed[2].isFromUser)
        assertEquals("nice", parsed[2].text)
    }

    @Test
    fun `unprefixed lines start a user message and continue the current one`() {
        // An unprefixed first line starts a user message
        val lead = ConversationImportParser.parse("no speaker here\ncontinues")
        assertEquals(1, lead.size)
        assertEquals(true, lead[0].isFromUser)
        assertTrue(lead[0].text.contains("continues"))

        // After a speaker line, unprefixed lines continue that message
        val follow = ConversationImportParser.parse("AI: hello there\nhow can I help?\nyou bet")
        assertEquals(1, follow.size)
        assertEquals(false, follow[0].isFromUser)
        assertTrue(follow[0].text.contains("how can I help"))
        assertTrue(follow[0].text.contains("you bet"))
    }

    @Test
    fun `empty and junk input yields nothing`() {
        assertTrue(ConversationImportParser.parse("").isEmpty())
        assertTrue(ConversationImportParser.parse("   \n  \n").isEmpty())
    }

    @Test
    fun `recognizes common assistant names case-insensitively`() {
        val parsed = ConversationImportParser.parse("GPT: one\nCLAUDE: two\nCOPILOT: three\nChatGPT: four")
        assertEquals(4, parsed.size)
        assertEquals(true, parsed.all { !it.isFromUser })
    }
}
