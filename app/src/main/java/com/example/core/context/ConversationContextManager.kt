package com.example.core.context

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DialogTurn(
    val role: String, // "user" or "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

class ConversationContextManager(
    private val maxTurns: Int = 10
) {
    private val _turns = MutableStateFlow<List<DialogTurn>>(emptyList())
    val turns: StateFlow<List<DialogTurn>> = _turns.asStateFlow()

    fun recordTurn(role: String, content: String) {
        val trimmed = content.trim()
        if (trimmed.isBlank()) return

        val currentList = _turns.value.toMutableList()
        currentList.add(DialogTurn(role = role, content = trimmed))
        while (currentList.size > maxTurns) {
            currentList.removeAt(0)
        }
        _turns.value = currentList
    }

    fun getFormattedHistory(): List<Pair<String, String>> {
        return _turns.value.map { Pair(it.role, it.content) }
    }

    fun clearContext() {
        _turns.value = emptyList()
    }
}
