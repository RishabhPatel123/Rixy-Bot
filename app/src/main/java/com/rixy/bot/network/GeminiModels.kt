package com.rixy.bot.network

/** Typed failures surfaced to the UI so users always know what actually went wrong. */
sealed class GeminiException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class Auth : GeminiException("auth")
    class ModelNotFound(val model: String) : GeminiException("model")
    class RateLimited : GeminiException("rate_limit")
    class Http(val code: Int, val body: String) : GeminiException("http $code")
    class Network(cause: Throwable) : GeminiException("network", cause)
    class Parse(cause: Throwable? = null) : GeminiException("parse", cause)
}

/** Events emitted while streaming a chat reply. */
sealed interface StreamEvent {
    data class Delta(val text: String) : StreamEvent
    data class Sources(val sources: List<Source>) : StreamEvent
}

/** One web source cited by a grounded answer. */
data class Source(val title: String, val uri: String)

/** One turn of conversation history sent to the API. Only the newest turn may carry an image. */
data class ChatTurn(
    val isFromUser: Boolean,
    val text: String,
    val imageBase64: String? = null,
    val imageMimeType: String? = null,
)

/** A generated image with an optional caption. */
data class GeneratedImage(val base64: String, val mimeType: String, val caption: String)

/** A single item of a generated plan. */
data class PlanItem(val title: String, val description: String, val priority: String)
