package com.example.core.provider

enum class AIProviderType(
    val id: String,
    val displayName: String,
    val defaultModel: String,
    val availableModels: List<String>,
    val hasFreeTier: Boolean,
    val description: String,
    val websiteUrl: String
) {
    GEMINI(
        id = "gemini",
        displayName = "Google Gemini",
        defaultModel = "gemini-2.5-flash",
        availableModels = listOf("gemini-2.5-flash", "gemini-1.5-flash", "gemini-2.5-pro", "gemini-2.0-flash"),
        hasFreeTier = true,
        description = "High-speed multimodal reasoning with generous free tier",
        websiteUrl = "https://aistudio.google.com"
    ),
    GROQ(
        id = "groq",
        displayName = "Groq Cloud",
        defaultModel = "llama-3.3-70b-versatile",
        availableModels = listOf("llama-3.3-70b-versatile", "llama-3.1-8b-instant", "mixtral-8x7b-32768"),
        hasFreeTier = true,
        description = "Ultra-fast LPU inference (500+ tokens/sec) with free tier",
        websiteUrl = "https://console.groq.com"
    ),
    OPENAI(
        id = "openai",
        displayName = "OpenAI",
        defaultModel = "gpt-4o-mini",
        availableModels = listOf("gpt-4o-mini", "gpt-4o", "gpt-4-turbo"),
        hasFreeTier = false,
        description = "Industry standard GPT reasoning and general knowledge",
        websiteUrl = "https://platform.openai.com"
    ),
    ANTHROPIC(
        id = "anthropic",
        displayName = "Anthropic Claude",
        defaultModel = "claude-3-5-sonnet-20241022",
        availableModels = listOf("claude-3-5-sonnet-20241022", "claude-3-haiku-20240307", "claude-3-opus-20240229"),
        hasFreeTier = false,
        description = "Nuanced, high-precision steerable intelligence",
        websiteUrl = "https://console.anthropic.com"
    ),
    OPENROUTER(
        id = "openrouter",
        displayName = "OpenRouter",
        defaultModel = "deepseek/deepseek-chat",
        availableModels = listOf("deepseek/deepseek-chat", "meta-llama/llama-3.3-70b-instruct", "google/gemini-flash-1.5"),
        hasFreeTier = true,
        description = "Universal multi-provider gateway supporting open models",
        websiteUrl = "https://openrouter.ai"
    ),
    LOCAL_FALLBACK(
        id = "local_fallback",
        displayName = "On-Device Engine",
        defaultModel = "VoidCore-Lite-1.5B (Int4)",
        availableModels = listOf("VoidCore-Lite-1.5B (Int4)", "VoidCore-Balanced-3B (Int4)", "VoidCore-Pro-7B (Int4)"),
        hasFreeTier = true,
        description = "100% offline, zero-network embedded neural runtime (LiteRT-LM / GGUF)",
        websiteUrl = "local://device"
    );

    companion object {
        fun fromId(id: String): AIProviderType {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: LOCAL_FALLBACK
        }
    }
}
