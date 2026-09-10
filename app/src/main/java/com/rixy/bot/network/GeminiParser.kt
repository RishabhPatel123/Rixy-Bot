package com.rixy.bot.network

import org.json.JSONObject

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
