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

class AnthropicProvider(
    private val keyStorage: SecureKeyStorage,
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
) : AIProvider {

    override val id: String = AIProviderType.ANTHROPIC.id
    override val type: AIProviderType = AIProviderType.ANTHROPIC
    override var config: ProviderConfig = ProviderConfig(type = type, priorityOrder = 3)

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
            val url = config.customEndpointUrl ?: "https://api.anthropic.com/v1/messages"

            val messagesArray = JSONArray()
            for ((role, text) in contextHistory) {
                messagesArray.put(JSONObject().put("role", role).put("content", text))
            }
            messagesArray.put(JSONObject().put("role", "user").put("content", prompt))

            val jsonBody = JSONObject()
                .put("model", model)
                .put("max_tokens", 800)
                .put("system", "You are VoidCore, an intelligent, concise on-device assistant. Answer directly.")
                .put("messages", messagesArray)
                .toString()

            val request = Request.Builder()
                .url(url)
                .addHeader("x-api-key", apiKey)
                .addHeader("anthropic-version", "2023-06-01")
                .post(jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType()))
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
            val contentArray = json.optJSONArray("content")
            if (contentArray != null && contentArray.length() > 0) {
                val firstContent = contentArray.getJSONObject(0)
                val text = firstContent.getString("text").trim()
                return@withContext GenerationResponse(
                    text = text,
                    providerName = type.displayName,
                    modelUsed = model,
                    latencyMs = latency,
                    isSuccess = true
                )
            }

            GenerationResponse(
                text = "",
                providerName = type.displayName,
                modelUsed = model,
                latencyMs = latency,
                isSuccess = false,
                errorMessage = "No text content returned from Claude"
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
            val url = config.customEndpointUrl ?: "https://api.anthropic.com/v1/messages"

            val jsonBody = JSONObject()
                .put("model", model)
                .put("max_tokens", 5)
                .put("messages", JSONArray().put(JSONObject().put("role", "user").put("content", "ping")))
                .toString()

            val request = Request.Builder()
                .url(url)
                .addHeader("x-api-key", apiKey)
                .addHeader("anthropic-version", "2023-06-01")
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
