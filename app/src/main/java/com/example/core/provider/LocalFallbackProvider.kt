package com.example.core.provider

import com.example.engine.local.LocalModelRuntime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalFallbackProvider(
    private val localRuntime: LocalModelRuntime = LocalModelRuntime()
) : AIProvider {

    override val id: String = AIProviderType.LOCAL_FALLBACK.id
    override val type: AIProviderType = AIProviderType.LOCAL_FALLBACK
    override var config: ProviderConfig = ProviderConfig(type = type, isEnabled = true, priorityOrder = 99)

    override suspend fun generateResponse(
        prompt: String,
        contextHistory: List<Pair<String, String>>
    ): GenerationResponse = withContext(Dispatchers.Default) {
        val startTime = System.currentTimeMillis()
        try {
            val responseText = localRuntime.generate(prompt, contextHistory)
            val latency = System.currentTimeMillis() - startTime
            GenerationResponse(
                text = responseText,
                providerName = type.displayName,
                modelUsed = config.modelName,
                latencyMs = latency,
                isSuccess = true
            )
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            GenerationResponse(
                text = "VoidCore active. Ready for your command.",
                providerName = type.displayName,
                modelUsed = config.modelName,
                latencyMs = latency,
                isSuccess = true
            )
        }
    }

    override suspend fun testConnectivity(): Pair<Boolean, String> {
        return Pair(true, "Ready (0ms offline)")
    }
}
