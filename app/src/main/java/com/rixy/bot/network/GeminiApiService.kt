package com.rixy.bot.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
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
 * Minimal Gemini REST client: streaming chat, plan generation, and a key test.
 * The API key travels in the x-goog-api-key header, never in the URL.
 */
class GeminiApiService(private val client: OkHttpClient) {

    /** Streams the assistant reply for [history] (last item = the new user message). */
    fun streamChat(history: List<ChatTurn>, apiKey: String, model: String): Flow<String> =
        kotlinx.coroutines.flow.callbackFlow {
            val contents = JSONArray()
            history.forEach { turn ->
                contents.put(
                    JSONObject()
                        .put("role", if (turn.isFromUser) "user" else "model")
                        .put("parts", JSONArray().put(JSONObject().put("text", turn.text)))
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
                .toString()

            val request = Request.Builder()
                .url("$BASE_URL/models/$model:streamGenerateContent?alt=sse")
                .header("x-goog-api-key", apiKey)
                .post(body.toRequestBody("application/json".toMediaType()))
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
                            while (!source.exhausted()) {
                                val line = source.readUtf8Line() ?: break
                                if (!line.startsWith("data:")) continue
                                val payload = line.removePrefix("data:").trim()
                                if (payload.isEmpty() || payload == "[DONE]") continue
                                GeminiParser.extractStreamText(payload)?.let { delta ->
                                    trySend(delta)
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
            model
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
