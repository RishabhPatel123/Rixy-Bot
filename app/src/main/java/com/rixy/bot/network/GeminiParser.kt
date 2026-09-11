package com.rixy.bot.network

import org.json.JSONObject

/** One function call the model wants executed: tool name + JSON arguments. */
data class FunctionCallData(val name: String, val args: JSONObject)

/** Result of one agent LLM turn: either plain text or requested tool calls. */
sealed interface AgentReply {
    data class Text(val text: String) : AgentReply
    data class ToolCalls(val calls: List<FunctionCallData>) : AgentReply
}

/**
 * Pure parsing helpers for the Gemini REST API. Kept free of Android/IO
 * dependencies so they can be unit tested directly.
 */
object GeminiParser {

    /**
     * Extracts the concatenated text of one SSE `data:` payload from
     * streamGenerateContent, or null if the chunk carries no text.
     */
    fun extractStreamText(dataJson: String): String? {
        return try {
            val root = JSONObject(dataJson)
            val parts = root.optJSONArray("candidates")
                ?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?: return null
            val sb = StringBuilder()
            for (i in 0 until parts.length()) {
                sb.append(parts.optJSONObject(i)?.optString("text", "").orEmpty())
            }
            if (sb.isEmpty()) null else sb.toString()
        } catch (_: Exception) {
            null
        }
    }

    /** Extracts the full text of a generateContent response body. */
    fun extractResponseText(body: String): String {
        return extractStreamText(body)
            ?: throw GeminiException.Parse(IllegalStateException("no text in response"))
    }

    /** Extracts an error message from an error response body, if parseable. */
    fun extractErrorMessage(body: String): String? = try {
        JSONObject(body).optJSONObject("error")?.optString("message")
    } catch (_: Exception) {
        null
    }

    /**
     * Extracts web-grounding sources from a stream chunk:
     * candidates[0].groundingMetadata.groundingChunks[].web.{title, uri}.
     * Returns an empty list when the chunk carries none.
     */
    fun extractSources(dataJson: String): List<Source> {
        return try {
            val chunks = JSONObject(dataJson)
                .optJSONArray("candidates")
                ?.optJSONObject(0)
                ?.optJSONObject("groundingMetadata")
                ?.optJSONArray("groundingChunks")
                ?: return emptyList()
            val sources = mutableListOf<Source>()
            for (i in 0 until chunks.length()) {
                val web = chunks.optJSONObject(i)?.optJSONObject("web") ?: continue
                val uri = web.optString("uri")
                if (uri.isNotEmpty()) {
                    sources += Source(title = web.optString("title").ifEmpty { uri }, uri = uri)
                }
            }
            sources
        } catch (_: Exception) {
            emptyList()
        }
    }

    /** Extracts the first inline image plus any text as caption from a generateContent body. */    fun extractImageAndCaption(body: String): GeneratedImage {
        return try {
            val parts = JSONObject(body)
                .optJSONArray("candidates")
                ?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?: throw GeminiException.Parse()
            var image: GeneratedImage? = null
            val caption = StringBuilder()
            for (i in 0 until parts.length()) {
                val part = parts.optJSONObject(i) ?: continue
                val inline = part.optJSONObject("inlineData")
                    ?: part.optJSONObject("inline_data") // snake_case variant
                if (inline != null && image == null) {
                    image = GeneratedImage(
                        base64 = inline.optString("data"),
                        mimeType = inline.optString("mimeType").ifEmpty { inline.optString("mime_type") }.ifEmpty { "image/png" },
                        caption = "",
                    )
                } else {
                    caption.append(part.optString("text"))
                }
            }
            image?.copy(caption = caption.toString().trim())
                ?: throw GeminiException.Parse(IllegalStateException("no image in response"))
        } catch (e: GeminiException) {
            throw e
        } catch (e: Exception) {
            throw GeminiException.Parse(e)
        }
    }

    /** Parses an agent turn: either tool calls or the final text answer. */
    fun parseAgentReply(body: String): AgentReply {
        val parts = try {
            JSONObject(body)
                .optJSONArray("candidates")
                ?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
        } catch (e: Exception) {
            throw GeminiException.Parse(e)
        } ?: throw GeminiException.Parse(IllegalStateException("no content"))
        val calls = mutableListOf<FunctionCallData>()
        val text = StringBuilder()
        for (i in 0 until parts.length()) {
            val part = parts.optJSONObject(i) ?: continue
            val call = part.optJSONObject("functionCall")
            if (call != null) {
                calls += FunctionCallData(
                    name = call.optString("name"),
                    args = call.optJSONObject("args") ?: JSONObject(),
                )
            } else {
                text.append(part.optString("text"))
            }
        }
        return if (calls.isNotEmpty()) AgentReply.ToolCalls(calls) else AgentReply.Text(text.toString().trim())
    }

    /** Builds a model-role content part echoing a function call (for history). */
    fun functionCallPart(name: String, args: JSONObject): JSONObject =
        JSONObject()
            .put("functionCall", JSONObject().put("name", name).put("args", args))

    /** Builds the function-role content sent back after executing a tool. */
    fun functionResponsePart(name: String, payload: JSONObject): JSONObject =
        JSONObject()
            .put(
                "functionResponse",
                JSONObject().put("name", name).put("response", payload)
            )

    /**
     * Parses a plan out of a model reply. Tolerates markdown code fences and
     * leading prose; expects a JSON array of {title, description, priority}.
     */
    fun parsePlan(reply: String): List<PlanItem> {
        val json = extractJsonArray(reply) ?: throw GeminiException.Parse()
        val arr = org.json.JSONArray(json)
        val items = mutableListOf<PlanItem>()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val title = obj.optString("title").trim()
            if (title.isEmpty()) continue
            items += PlanItem(
                title = title,
                description = obj.optString("description").trim(),
                priority = normalizePriority(obj.optString("priority")),
            )
        }
        if (items.isEmpty()) throw GeminiException.Parse()
        return items
    }

    fun normalizePriority(raw: String): String = when (raw.trim().uppercase()) {
        "HIGH", "H", "URGENT", "P0" -> "HIGH"
        "LOW", "L", "NICE-TO-HAVE", "P2" -> "LOW"
        else -> "MEDIUM"
    }

    /** Finds the outermost JSON array in text, ignoring code fences. */
    private fun extractJsonArray(text: String): String? {
        val unfenced = text
            .replace("```json", "```")
            .split("```")
            .firstOrNull { it.contains('[') }
            ?: text
        val start = unfenced.indexOf('[')
        if (start < 0) return null
        var depth = 0
        var inString = false
        var escaped = false
        for (i in start until unfenced.length) {
            val c = unfenced[i]
            when {
                escaped -> escaped = false
                c == '\\' && inString -> escaped = true
                c == '"' -> inString = !inString
                !inString && c == '[' -> depth++
                !inString && c == ']' -> {
                    depth--
                    if (depth == 0) return unfenced.substring(start, i + 1)
                }
            }
        }
        return null
    }
}
