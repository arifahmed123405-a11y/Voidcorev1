package com.example.core.contracts

data class VisionFrame(
    val width: Int,
    val height: Int,
    val format: String = "RGBA_8888",
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: Map<String, Any> = emptyMap()
)

data class DetectedObject(
    val label: String,
    val confidence: Float,
    val boundingBox: List<Float> = emptyList() // [left, top, right, bottom]
)

data class SceneAnalysisResult(
    val summary: String,
    val detectedObjects: List<DetectedObject> = emptyList(),
    val extractedText: String = "",
    val confidenceScore: Float = 1.0f
)

data class TextExtractionResult(
    val rawText: String,
    val lineBlocks: List<String> = emptyList(),
    val isConfident: Boolean = true
)

/**
 * Provider-neutral contract for computer vision and optical scene perception.
 */
interface VisionPerceptionProvider {
    val providerName: String
    val isCameraActive: Boolean

    suspend fun analyzeFrame(frame: VisionFrame): Result<SceneAnalysisResult>
    suspend fun extractText(frame: VisionFrame): Result<TextExtractionResult>
}

/**
 * Phase 0 Stub for Vision Perception.
 */
class Phase0VisionPerceptionProvider : VisionPerceptionProvider {
    override val providerName: String = "Phase0-Vision-Stub"
    override val isCameraActive: Boolean = false

    override suspend fun analyzeFrame(frame: VisionFrame): Result<SceneAnalysisResult> {
        return Result.success(
            SceneAnalysisResult(
                summary = "Vision provider interface established. Camera sensors deactivated for Phase 0.",
                detectedObjects = emptyList(),
                extractedText = ""
            )
        )
    }

    override suspend fun extractText(frame: VisionFrame): Result<TextExtractionResult> {
        return Result.success(
            TextExtractionResult(
                rawText = "",
                lineBlocks = emptyList(),
                isConfident = false
            )
        )
    }
}
