package com.rixy.bot.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Minimal Gemini REST client: streaming chat (optionally Google-Search-grounded),
 * image generation, plan generation, and a key test. The API key travels in the
 * x-goog-api-key header, never in the URL.
 */
class GeminiApiService(private val client: OkHttpClient) {

    /**
     * Streams the assistant reply for [history] (last item = the new user message).
     * When [webGrounded], the request carries the google_search tool and source
     * citations arrive as [StreamEvent.Sources].
     */
    fun streamChat(
        history: List<ChatTurn>,
        apiKey: String,
        model: String,
        webGrounded: Boolean = false,
    ): Flow<StreamEvent> = callbackFlow {
        val contents = JSONArray()
        history.forEach { turn ->
            val parts = JSONArray()
            if (turn.text.isNotEmpty()) {
                parts.put(JSONObject().put("text", turn.text))
            }
            turn.imageBase64?.let { image ->
                parts.put(
                    JSONObject().put(
                        "inlineData",
                        JSONObject()
                            .put("mimeType", turn.imageMimeType ?: "image/png")
                            .put("data", image)
                    )
                )
            }
            contents.put(
                JSONObject()
                    .put("role", if (turn.isFromUser) "user" else "model")
                    .put("parts", parts)
            )
        }
        val body = JSONObject()
            .put(
                "systemInstruction",
                JSONObject().put(
                    "parts",
                    JSONArray().put(JSONObject().put("text", SYSTEM_PROMPT))
                )
            )
            .put("contents", contents)
        if (webGrounded) {
            body.put("tools", JSONArray().put(JSONObject().put("google_search", JSONObject())))
        }

        val request = Request.Builder()
            .url("$BASE_URL/models/$model:streamGenerateContent?alt=sse")
            .header("x-goog-api-key", apiKey)
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val call = client.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                close(GeminiException.Network(e))
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        val errorBody = it.body?.string().orEmpty()
                        close(errorFor(it.code, errorBody, model))
                        return
                    }
                    try {
                        val source = it.body?.source() ?: throw GeminiException.Parse()
                        val emittedSources = mutableSetOf<String>()
                        while (!source.exhausted()) {
                            val line = source.readUtf8Line() ?: break
                            if (!line.startsWith("data:")) continue
                            val payload = line.removePrefix("data:").trim()
                            if (payload.isEmpty() || payload == "[DONE]") continue
                            GeminiParser.extractStreamText(payload)?.let { delta ->
                                trySend(StreamEvent.Delta(delta))
                            }
                            val sources = GeminiParser.extractSources(payload)
                            if (sources.isNotEmpty()) {
                                // Chunks repeat earlier citations; only surface new ones.
                                val fresh = sources.filter { emittedSources.add(it.uri) }
                                if (fresh.isNotEmpty()) trySend(StreamEvent.Sources(fresh))
                            }
                        }
                        close()
                    } catch (e: IOException) {
                        close(GeminiException.Network(e))
                    } catch (e: Exception) {
                        close(GeminiException.Parse(e))
                    }
                }
            }
        })
        awaitClose { call.cancel() }
    }.flowOn(Dispatchers.IO)

    /** Generates an image for a prompt using the dedicated image model. */
    suspend fun generateImage(prompt: String, apiKey: String): GeneratedImage {
        val body = JSONObject()
            .put(
                "contents",
                JSONArray().put(
                    JSONObject()
                        .put("role", "user")
                        .put("parts", JSONArray().put(JSONObject().put("text", prompt)))
                )
            )
            .put(
                "generationConfig",
                JSONObject().put("responseModalities", JSONArray().put("TEXT").put("IMAGE"))
            )
            .toString()
        val response = execute(
            "$BASE_URL/models/$IMAGE_MODEL:generateContent",
            apiKey,
            body,
            IMAGE_MODEL,
        )
        return GeminiParser.extractImageAndCaption(response)
    }

    /** Generates a prioritized plan for a goal (non-streaming). */
    suspend fun generatePlan(goal: String, apiKey: String, model: String): List<PlanItem> {
        val body = JSONObject()
            .put(
                "systemInstruction",
                JSONObject().put(
                    "parts",
                    JSONArray().put(JSONObject().put("text", PLAN_PROMPT))
                )
            )
            .put(
                "contents",
                JSONArray().put(
                    JSONObject()
                        .put("role", "user")
                        .put("parts", JSONArray().put(JSONObject().put("text", goal)))
                )
            )
            .put("generationConfig", JSONObject().put("temperature", 0.3))
            .toString()
        val response = execute(
            "$BASE_URL/models/$model:generateContent",
            apiKey,
            body,
            model,
        )
        val reply = GeminiParser.extractResponseText(response)
        return GeminiParser.parsePlan(reply)
    }

    /** Verifies a key; returns round-trip latency in ms. */
    suspend fun testApiKey(apiKey: String, model: String): Long {
        val started = System.currentTimeMillis()
        val body = JSONObject()
            .put(
                "contents",
                JSONArray().put(
                    JSONObject()
                        .put("role", "user")
                        .put("parts", JSONArray().put(JSONObject().put("text", "Reply with the single word: OK")))
                )
            )
            .put("generationConfig", JSONObject().put("maxOutputTokens", 8))
            .toString()
        execute("$BASE_URL/models/$model:generateContent", apiKey, body, model)
        return System.currentTimeMillis() - started
    }

    private suspend fun execute(url: String, apiKey: String, body: String, model: String): String =
        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(url)
                .header("x-goog-api-key", apiKey)
                .post(body.toRequestBody("application/json".toMediaType()))
                .build()
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string().orEmpty()
                if (!response.isSuccessful) throw errorFor(response.code, responseBody, model)
                responseBody
            }
        }

    private fun errorFor(code: Int, body: String, model: String): GeminiException = when (code) {
        401, 403 -> GeminiException.Auth()
        404 -> GeminiException.ModelNotFound(model)
        429 -> GeminiException.RateLimited()
        else -> {
            val message = GeminiParser.extractErrorMessage(body)
            GeminiException.Http(code, message ?: body.take(200))
        }
    }

    companion object {
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta"
        const val IMAGE_MODEL = "gemini-3.1-flash-image"
        private const val SYSTEM_PROMPT =
            "You are Rixy, a helpful, direct Android assistant. Answer clearly and concisely. " +
                "Use markdown: **bold**, bullet lists, numbered steps, and fenced code blocks with a " +
                "language tag for code. Prefer short paragraphs over walls of text."
        private const val PLAN_PROMPT =
            "You are a planning assistant. Given a goal, break it into 3-6 concrete, actionable " +
                "sub-tasks. Reply with ONLY a JSON array, no prose, where each item is " +
                "{\"title\": string (max 60 chars), \"description\": string (max 200 chars), " +
                "\"priority\": \"HIGH\" | \"MEDIUM\" | \"LOW\"}. Order items by execution order."
    }
}
