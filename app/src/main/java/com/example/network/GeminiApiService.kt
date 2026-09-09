package com.example.network

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit


class GeminiApiService(private val okHttpClient: OkHttpClient) {
    private val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"


    suspend fun generateTaskPlan(goal: String): List<JSONObject> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            // Fallback for UI if key is missing
            return@withContext listOf(
                JSONObject().apply {
                    put("title", "Configure Gemini API Key")
                    put("description", "Please add your Gemini API Key in Settings > Secrets to use AI planning.")
                    put("priority", "Urgent")
                }
            )
        }

        val url = "$BASE_URL?key=$apiKey"
        
        val systemInstruction = "You are a task orchestration AI. Break down the user's high-level goal into exactly 2 to 4 concrete sub-tasks. Each sub-task must have a 'title', 'description' (max 2 sentences), and 'priority' ('Normal', 'High', or 'Urgent'). Return ONLY a valid JSON array of objects. Do not include markdown formatting or backticks. Example: [{\"title\":\"Research\",\"description\":\"Find info.\",\"priority\":\"Normal\"}]"
        
        val requestBodyJson = JSONObject().apply {
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().apply { put("text", systemInstruction) }))
            })
            put("contents", JSONArray().put(
                JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().apply { put("text", goal) }))
                }
            ))
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.4)
                put("responseModalities", JSONArray().put("TEXT"))
            })
        }

        val request = Request.Builder()
            .url(url)
            .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
            .build()

        try {
            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                return@withContext emptyList()
            }
            
            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            val text = candidates?.optJSONObject(0)?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)?.optString("text") ?: "[]"
            
            // Clean markdown if present
            var cleanText = text.trim()
            if (cleanText.startsWith("```json")) {
                cleanText = cleanText.removePrefix("```json").removeSuffix("```").trim()
            } else if (cleanText.startsWith("```")) {
                cleanText = cleanText.removePrefix("```").removeSuffix("```").trim()
            }
            
            val subTasksArray = JSONArray(cleanText)
            val result = mutableListOf<JSONObject>()
            for (i in 0 until subTasksArray.length()) {
                result.add(subTasksArray.getJSONObject(i))
            }
            result
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun generateChatResponse(userMessage: String, botName: String, botRole: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "I am a prototype AI. Please add a valid Gemini API Key to enable my real responses."
        }

        val url = "$BASE_URL?key=$apiKey"
        
        val systemInstruction = "You are $botName, an AI coworker acting as a $botRole in a simulated terminal/browser environment. Respond concisely (1-2 sentences) confirming the execution of the user's instructions based on your role."
        
        val requestBodyJson = JSONObject().apply {
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().apply { put("text", systemInstruction) }))
            })
            put("contents", JSONArray().put(
                JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().apply { put("text", userMessage) }))
                }
            ))
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.7)
                put("responseModalities", JSONArray().put("TEXT"))
            })
        }

        val request = Request.Builder()
            .url(url)
            .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
            .build()

        try {
            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                return@withContext "I'm having trouble connecting to my neural core."
            }
            
            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            candidates?.optJSONObject(0)?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)?.optString("text") ?: "Done."
        } catch (e: Exception) {
            e.printStackTrace()
            "Network error communicating with the brain."
        }
    }
}
