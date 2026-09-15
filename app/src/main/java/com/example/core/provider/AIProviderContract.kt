package com.example.core.provider

data class GenerationResponse(
    val text: String,
    val providerName: String,
    val modelUsed: String,
    val latencyMs: Long,
    val isSuccess: Boolean,
    val errorMessage: String? = null
)

data class ProviderConfig(
    val type: AIProviderType,
    var modelName: String = type.defaultModel,
    var isEnabled: Boolean = true,
    var priorityOrder: Int = 0,
    var customEndpointUrl: String? = null
)

interface AIProvider {
    val id: String
    val type: AIProviderType
    var config: ProviderConfig

    suspend fun generateResponse(
        prompt: String,
        contextHistory: List<Pair<String, String>> = emptyList()
    ): GenerationResponse

    suspend fun testConnectivity(): Pair<Boolean, String>
}
