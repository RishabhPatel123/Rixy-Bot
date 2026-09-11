package com.rixy.bot.util

/**
 * Parses a pasted or file-loaded conversation into (isFromUser, text) pairs.
 * Speaker lines follow the common "Name: text" convention; unprefixed lines
 * are treated as continuations of the current message.
 */
object ConversationImportParser {

    private val USER_PREFIXES = listOf("you:", "me:", "user:", "human:")
    private val MODEL_PREFIXES = listOf(
        "ai:", "assistant:", "grok:", "gpt:", "chatgpt:", "gemini:", "claude:",
        "copilot:", "model:", "rixy:", "bot:",
    )

    data class ParsedMessage(val isFromUser: Boolean, val text: String)

    fun parse(raw: String): List<ParsedMessage> {
        val messages = mutableListOf<ParsedMessage>()
        var current: ParsedMessage? = null

        fun flush() {
            current?.let { msg ->
                val trimmed = msg.text.trim()
                if (trimmed.isNotEmpty()) messages += msg.copy(text = trimmed)
            }
            current = null
        }

        for (line in raw.lines()) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) {
                val cur = current
                if (cur != null) current = cur.copy(text = cur.text + "\n")
                continue
            }
            val lower = trimmed.lowercase()
            when {
                USER_PREFIXES.any { lower.startsWith(it) } -> {
                    flush()
                    current = ParsedMessage(true, trimmed.substringAfter(':').trim())
                }
                MODEL_PREFIXES.any { lower.startsWith(it) } -> {
                    flush()
                    current = ParsedMessage(false, trimmed.substringAfter(':').trim())
                }
                else -> {
                    val cur = current
                    current = if (cur == null) {
                        ParsedMessage(true, trimmed)
                    } else {
                        cur.copy(text = cur.text + "\n" + trimmed)
                    }
                }
            }
        }
        flush()
        return messages
    }
}
