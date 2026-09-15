package com.example.core.contracts

import kotlinx.coroutines.flow.Flow

data class GenerationRequest(
    val prompt: String,
    val systemInstruction: String? = null,
    val temperature: Float = 0.7f,
    val maxTokens: Int = 1024,
    val stopSequences: List<String> = emptyList()
)

data class GenerationResponse(
    val text: String,
    val finishReason: String = "STOP",
    val tokensUsed: Int = 0
)

data class StreamChunk(
    val text: String,
    val isDone: Boolean = false
)

/**
 * Provider-neutral contract for AI text generation and streaming.
 */
interface TextGenerationProvider {
    val providerId: String
    val isLocal: Boolean

    suspend fun generateText(request: GenerationRequest): Result<GenerationResponse>
    fun streamText(request: GenerationRequest): Flow<StreamChunk>
}

/**
 * Stub implementation for Phase 0 where no live AI network calls are made.
 */
class StubTextGenerationProvider(
    override val providerId: String = "phase0_neutral_stub",
    override val isLocal: Boolean = false
) : TextGenerationProvider {
    override suspend fun generateText(request: GenerationRequest): Result<GenerationResponse> {
        return Result.success(
            GenerationResponse(
                text = "Phase 0 Neutral Foundation: AI provider stubs active. No live network calls executed.",
                finishReason = "PHASE_0_STUB"
            )
        )
    }

    override fun streamText(request: GenerationRequest): Flow<StreamChunk> = kotlinx.coroutines.flow.flow {
        emit(StreamChunk(text = "Phase 0 Neutral Foundation: Stub streaming stream chunk."))
        emit(StreamChunk(text = "", isDone = true))
    }
}
