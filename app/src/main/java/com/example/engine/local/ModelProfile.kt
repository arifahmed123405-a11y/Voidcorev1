package com.example.engine.local

enum class ModelProfileTier(val label: String) {
    LITE("Lite (Fast & Efficient)"),
    BALANCED("Balanced (Recommended)"),
    PRO("Pro (Deep Reasoner)")
}

data class ModelProfile(
    val id: String,
    val name: String,
    val tier: ModelProfileTier,
    val modelFormat: String,
    val parameterCount: String,
    val sizeBytes: Long,
    val ramRequiredMb: Int,
    val contextWindowTokens: Int,
    val sha256Checksum: String,
    val downloadUrl: String,
    val description: String,
    val recommendedDeviceSpec: String
) {
    val sizeFormatted: String
        get() {
            val mb = sizeBytes / (1024 * 1024)
            return if (mb >= 1024) {
                String.format("%.1f GB", mb / 1024.0)
            } else {
                "$mb MB"
            }
        }

    companion object {
        val LITE_PROFILE = ModelProfile(
            id = "voidcore_lite_1_5b",
            name = "VoidCore-Lite 1.5B (Int4)",
            tier = ModelProfileTier.LITE,
            modelFormat = "LiteRT / GGUF-Q4_K_M",
            parameterCount = "1.5 Billion",
            sizeBytes = 1_180_000_000L, // ~1.1 GB
            ramRequiredMb = 420,
            contextWindowTokens = 2048,
            sha256Checksum = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            downloadUrl = "https://models.voidcore.internal/voidcore-lite-1.5b-q4.bin",
            description = "Optimized for battery life, instant device control, and lightweight offline conversations.",
            recommendedDeviceSpec = "All devices with ≥ 3 GB RAM"
        )

        val BALANCED_PROFILE = ModelProfile(
            id = "voidcore_balanced_3b",
            name = "VoidCore-Balanced 3B (Int4)",
            tier = ModelProfileTier.BALANCED,
            modelFormat = "LiteRT / GGUF-Q4_K_M",
            parameterCount = "3.2 Billion",
            sizeBytes = 2_250_000_000L, // ~2.1 GB
            ramRequiredMb = 780,
            contextWindowTokens = 4096,
            sha256Checksum = "c5a0899298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852d41a",
            downloadUrl = "https://models.voidcore.internal/voidcore-balanced-3b-q4.bin",
            description = "Ideal balance of natural conversation, strict JSON intent extraction, and reasoning.",
            recommendedDeviceSpec = "Devices with ≥ 6 GB RAM"
        )

        val PRO_PROFILE = ModelProfile(
            id = "voidcore_pro_7b",
            name = "VoidCore-Pro 7B (Int4)",
            tier = ModelProfileTier.PRO,
            modelFormat = "LiteRT / GGUF-Q4_K_M",
            parameterCount = "7.0 Billion",
            sizeBytes = 3_900_000_000L, // ~3.6 GB
            ramRequiredMb = 1450,
            contextWindowTokens = 8192,
            sha256Checksum = "98fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855e3b0c442",
            downloadUrl = "https://models.voidcore.internal/voidcore-pro-7b-q4.bin",
            description = "Full on-device reasoning, complex multi-step instructions, and extended multi-turn context.",
            recommendedDeviceSpec = "Flagship devices with ≥ 8 GB RAM"
        )

        val ALL_PROFILES = listOf(LITE_PROFILE, BALANCED_PROFILE, PRO_PROFILE)
    }
}
