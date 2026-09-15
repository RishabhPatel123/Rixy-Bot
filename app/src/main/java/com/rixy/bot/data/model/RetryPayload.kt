package com.rixy.bot.data.model

import org.json.JSONObject

/**
 * Persisted alongside a failed message so it can be retried with the exact
 * original request (mode, prompt, attachment, grounding flag).
 */
data class RetryPayload(
    val mode: String,
    val prompt: String,
    val attachmentPath: String? = null,
    val webGrounded: Boolean = false,
) {
    fun toJson(): String = JSONObject()
        .put("mode", mode)
        .put("prompt", prompt)
        .put("attachment", attachmentPath ?: JSONObject.NULL)
        .put("web", webGrounded)
        .toString()

    companion object {
        fun fromJson(json: String): RetryPayload? = runCatching {
            val obj = JSONObject(json)
            RetryPayload(
                mode = obj.optString("mode"),
                prompt = obj.optString("prompt"),
                attachmentPath = if (obj.isNull("attachment")) null else obj.optString("attachment"),
                webGrounded = obj.optBoolean("web", false),
            )
        }.getOrNull()
    }
}
