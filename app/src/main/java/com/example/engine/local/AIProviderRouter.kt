package com.example.engine.local

import com.example.engine.security.StructuredIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

enum class AIProviderMode {
    LOCAL_ONLY,
    HYBRID_PREFER_LOCAL,
    CLOUD_ONLY
}

class AIProviderRouter(
    val localRuntime: LocalModelRuntime = LocalModelRuntime()
) {
    var currentMode: AIProviderMode = AIProviderMode.HYBRID_PREFER_LOCAL
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun resolveIntent(rawPrompt: String, context: List<String> = emptyList()): StructuredIntent {
        val pairs = context.map { "turn" to it }
        return localRuntime.resolveAmbiguousIntent(rawPrompt, pairs)
    }

    suspend fun generateDeepReasoningResponse(rawPrompt: String, context: List<String> = emptyList()): String {
        val pairs = context.map { "turn" to it }
        if (currentMode == AIProviderMode.LOCAL_ONLY) {
            return localRuntime.generate(rawPrompt, pairs)
        }

        val geminiApiKey = try {
            System.getenv("GEMINI_API_KEY") ?: ""
        } catch (e: Exception) {
            ""
        }

        if (geminiApiKey.isNullOrBlank() || geminiApiKey == "MY_GEMINI_API_KEY") {
            // Fallback cleanly to local model without failing
            return localRuntime.generate(rawPrompt, pairs)
        }

        return try {
            queryGeminiApi(geminiApiKey, rawPrompt, pairs)
        } catch (e: Exception) {
            localRuntime.generate(rawPrompt, pairs)
        }
    }

    private suspend fun queryGeminiApi(apiKey: String, prompt: String, context: List<Pair<String, String>>): String = withContext(Dispatchers.IO) {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
        
        val contentsArray = JSONArray()
        val userPart = JSONObject().put("text", "You are VoidCore assistant. Answer conversationally, concisely and directly. User says: $prompt")
        val contentObj = JSONObject().put("parts", JSONArray().put(userPart))
        contentsArray.put(contentObj)

        val jsonBody = JSONObject().put("contents", contentsArray).toString()
        val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val response = httpClient.newCall(request).execute()
        if (response.isSuccessful) {
            val bodyString = response.body?.string() ?: ""
            val json = JSONObject(bodyString)
            val candidates = json.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.getJSONObject("content")
                val parts = content.getJSONArray("parts")
                if (parts.length() > 0) {
                    return@withContext parts.getJSONObject(0).getString("text").trim()
                }
            }
        }
        return@withContext localRuntime.generate(prompt, context)
    }
}
