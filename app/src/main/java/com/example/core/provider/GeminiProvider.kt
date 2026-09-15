package com.example.core.provider

import com.example.core.security.SecureKeyStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiProvider(
    private val keyStorage: SecureKeyStorage,
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
) : AIProvider {

    override val id: String = AIProviderType.GEMINI.id
    override val type: AIProviderType = AIProviderType.GEMINI
    override var config: ProviderConfig = ProviderConfig(type = type, priorityOrder = 0)

    override suspend fun generateResponse(
        prompt: String,
        contextHistory: List<Pair<String, String>>
    ): GenerationResponse = withContext(Dispatchers.IO) {
        val apiKey = keyStorage.getApiKey(id)
        if (apiKey.isBlank()) {
            return@withContext GenerationResponse(
                text = "",
                providerName = type.displayName,
                modelUsed = config.modelName,
                latencyMs = 0L,
                isSuccess = false,
                errorMessage = "API key not configured in Secure KeyStore"
            )
        }

        val startTime = System.currentTimeMillis()
        try {
            val model = if (config.modelName.isNotBlank()) config.modelName else type.defaultModel
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

            val contentsArray = JSONArray()

            // System prompt + history
            val systemInstruction = JSONObject().put("text", "You are VoidCore, an intelligent, concise, on-device assistant. Speak directly and naturally.")
            contentsArray.put(JSONObject().put("role", "user").put("parts", JSONArray().put(systemInstruction)))
            contentsArray.put(JSONObject().put("role", "model").put("parts", JSONArray().put(JSONObject().put("text", "Understood. I am VoidCore."))))

            // Add short term history
            for ((role, text) in contextHistory) {
                val mappedRole = if (role == "user") "user" else "model"
                contentsArray.put(JSONObject().put("role", mappedRole).put("parts", JSONArray().put(JSONObject().put("text", text))))
            }

            // Current user turn
            contentsArray.put(JSONObject().put("role", "user").put("parts", JSONArray().put(JSONObject().put("text", prompt))))

            val jsonBody = JSONObject().put("contents", contentsArray).toString()
            val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            val latency = System.currentTimeMillis() - startTime
            val bodyString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext GenerationResponse(
                    text = "",
                    providerName = type.displayName,
                    modelUsed = model,
                    latencyMs = latency,
                    isSuccess = false,
                    errorMessage = "HTTP ${response.code}: $bodyString"
                )
            }

            val json = JSONObject(bodyString)
            val candidates = json.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.getJSONObject("content")
                val parts = content.getJSONArray("parts")
                if (parts.length() > 0) {
                    val reply = parts.getJSONObject(0).getString("text").trim()
                    return@withContext GenerationResponse(
                        text = reply,
                        providerName = type.displayName,
                        modelUsed = model,
                        latencyMs = latency,
                        isSuccess = true
                    )
                }
            }

            GenerationResponse(
                text = "",
                providerName = type.displayName,
                modelUsed = model,
                latencyMs = latency,
                isSuccess = false,
                errorMessage = "No text candidate returned from Gemini"
            )
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            GenerationResponse(
                text = "",
                providerName = type.displayName,
                modelUsed = config.modelName,
                latencyMs = latency,
                isSuccess = false,
                errorMessage = e.message ?: "Network failure"
            )
        }
    }

    override suspend fun testConnectivity(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val apiKey = keyStorage.getApiKey(id)
        if (apiKey.isBlank()) {
            return@withContext Pair(false, "API Key Missing")
        }
        val startTime = System.currentTimeMillis()
        try {
            val model = if (config.modelName.isNotBlank()) config.modelName else type.defaultModel
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
            val jsonBody = JSONObject().put(
                "contents",
                JSONArray().put(JSONObject().put("parts", JSONArray().put(JSONObject().put("text", "Ping"))))
            ).toString()

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            val latency = System.currentTimeMillis() - startTime
            if (response.isSuccessful) {
                Pair(true, "Connected (${latency}ms)")
            } else {
                Pair(false, "HTTP ${response.code}")
            }
        } catch (e: Exception) {
            Pair(false, e.message ?: "Connection Error")
        }
    }
}
