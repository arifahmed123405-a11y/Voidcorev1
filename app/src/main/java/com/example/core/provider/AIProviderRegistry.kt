package com.example.core.provider

import android.content.Context
import com.example.core.security.SecureKeyStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class FailoverEvent(
    val timestamp: Long = System.currentTimeMillis(),
    val attemptedProvider: String,
    val model: String,
    val error: String,
    val nextProvider: String?
)

class AIProviderRegistry(
    val context: Context,
    val keyStorage: SecureKeyStorage = SecureKeyStorage.getInstance(context)
) {
    val geminiProvider = GeminiProvider(keyStorage)
    val groqProvider = GroqProvider(keyStorage)
    val openAIProvider = OpenAIProvider(keyStorage)
    val anthropicProvider = AnthropicProvider(keyStorage)
    val openRouterProvider = OpenRouterProvider(keyStorage)
    val localFallbackProvider = LocalFallbackProvider()

    private val allProvidersList = listOf(
        geminiProvider,
        groqProvider,
        openAIProvider,
        anthropicProvider,
        openRouterProvider,
        localFallbackProvider
    )

    private val _providers = MutableStateFlow<List<AIProvider>>(allProvidersList)
    val providers: StateFlow<List<AIProvider>> = _providers.asStateFlow()

    private val _failoverHistory = MutableStateFlow<List<FailoverEvent>>(emptyList())
    val failoverHistory: StateFlow<List<FailoverEvent>> = _failoverHistory.asStateFlow()

    private val _lastSuccessfulProvider = MutableStateFlow<String>("Google Gemini")
    val lastSuccessfulProvider: StateFlow<String> = _lastSuccessfulProvider.asStateFlow()

    fun getProvider(type: AIProviderType): AIProvider {
        return allProvidersList.firstOrNull { it.type == type } ?: localFallbackProvider
    }

    fun updateProviderModel(type: AIProviderType, newModelName: String) {
        val provider = getProvider(type)
        provider.config.modelName = newModelName.trim()
        _providers.value = allProvidersList.toList()
    }

    fun setProviderEnabled(type: AIProviderType, isEnabled: Boolean) {
        if (type == AIProviderType.LOCAL_FALLBACK) return // Always keep local fallback enabled
        val provider = getProvider(type)
        provider.config.isEnabled = isEnabled
        _providers.value = allProvidersList.toList()
    }

    fun reorderPriority(fromIndex: Int, toIndex: Int) {
        val current = _providers.value.toMutableList()
        if (fromIndex in current.indices && toIndex in current.indices) {
            val item = current.removeAt(fromIndex)
            current.add(toIndex, item)
            current.forEachIndexed { index, provider ->
                provider.config.priorityOrder = index
            }
            _providers.value = current
        }
    }

    suspend fun generateWithFailover(
        prompt: String,
        contextHistory: List<Pair<String, String>> = emptyList()
    ): GenerationResponse {
        val candidateProviders = _providers.value
            .filter { it.config.isEnabled }
            .sortedBy { it.config.priorityOrder }

        for (i in candidateProviders.indices) {
            val provider = candidateProviders[i]
            val nextProvider = if (i + 1 < candidateProviders.size) candidateProviders[i + 1].type.displayName else null

            val result = provider.generateResponse(prompt, contextHistory)
            if (result.isSuccess && result.text.isNotBlank()) {
                _lastSuccessfulProvider.value = "${provider.type.displayName} (${result.modelUsed})"
                return result
            }

            // Log failover event
            val event = FailoverEvent(
                attemptedProvider = provider.type.displayName,
                model = result.modelUsed,
                error = result.errorMessage ?: "Unknown error / empty response",
                nextProvider = nextProvider
            )
            val updatedHistory = _failoverHistory.value.toMutableList()
            updatedHistory.add(0, event)
            if (updatedHistory.size > 20) updatedHistory.removeAt(updatedHistory.size - 1)
            _failoverHistory.value = updatedHistory
        }

        // Fallback to local deterministic engine if all candidates failed
        val fallbackResult = localFallbackProvider.generateResponse(prompt, contextHistory)
        _lastSuccessfulProvider.value = "${localFallbackProvider.type.displayName} (${fallbackResult.modelUsed})"
        return fallbackResult
    }

    companion object {
        @Volatile
        private var INSTANCE: AIProviderRegistry? = null

        fun getInstance(context: Context): AIProviderRegistry {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AIProviderRegistry(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
