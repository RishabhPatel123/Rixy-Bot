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

/** One turn of conversation history sent to the API. */
data class ChatTurn(val isFromUser: Boolean, val text: String)

/** A single item of a generated plan. */
data class PlanItem(val title: String, val description: String, val priority: String)
