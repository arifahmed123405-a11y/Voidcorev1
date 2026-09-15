package com.example.core.contracts

data class ModelSpec(
    val modelId: String,
    val modelName: String,
    val parameterCount: String,
    val quantizedType: String = "INT4",
    val memoryFootprintBytes: Long = 0L
)

data class LocalInferenceRequest(
    val prompt: String,
    val context: List<String> = emptyList(),
    val maxOutputTokens: Int = 512
)

data class LocalInferenceResponse(
    val responseText: String,
    val latencyMs: Long,
    val isFallback: Boolean = false
)

/**
 * Contract for provider-neutral local on-device inference engines.
 */
interface LocalInferenceEngine {
    val engineName: String
    val currentModel: ModelSpec?

    fun isModelLoaded(): Boolean
    suspend fun loadModel(spec: ModelSpec): Boolean
    suspend fun unloadModel()
    suspend fun infer(request: LocalInferenceRequest): Result<LocalInferenceResponse>
}

/**
 * Phase 0 implementation of LocalInferenceEngine with honest boundary reporting.
 */
class Phase0LocalInferenceEngine : LocalInferenceEngine {
    override val engineName: String = "Phase0-Local-Stub"
    override val currentModel: ModelSpec? = null

    override fun isModelLoaded(): Boolean = false

    override suspend fun loadModel(spec: ModelSpec): Boolean = false

    override suspend fun unloadModel() {}

    override suspend fun infer(request: LocalInferenceRequest): Result<LocalInferenceResponse> {
        return Result.success(
            LocalInferenceResponse(
                responseText = "Local inference runtime ready for Phase 1 adapter registration.",
                latencyMs = 0L,
                isFallback = false
            )
        )
    }
}
