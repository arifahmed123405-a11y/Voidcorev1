package com.example.feature.files

import android.content.Context
import com.example.core.database.VoidCoreRepository
import kotlinx.coroutines.flow.first

data class SearchResultItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String, // FILE, MEMORY, AUTOMATION, AUDIT
    val iconName: String,
    val timestamp: Long = System.currentTimeMillis()
)

class UniversalSearchEngine(private val context: Context) {

    private val repo = VoidCoreRepository.getInstance(context)

    private val mockLocalFiles = listOf(
        SearchResultItem("file_1", "Sprint_Planning_Notes.pdf", "Documents • 2.4 MB • Modified yesterday", "FILE", "picture_as_pdf"),
        SearchResultItem("file_2", "VoidCore_Architecture_Spec.md", "Downloads • 48 KB • Modified today", "FILE", "description"),
        SearchResultItem("file_3", "Ahmed_Study_Group_Notes.pdf", "Documents • 1.1 MB • Sent last week", "FILE", "picture_as_pdf"),
        SearchResultItem("file_4", "Weekly_Trading_Snapshot.csv", "Finances • 320 KB • Modified 3 days ago", "FILE", "table_chart")
    )

    suspend fun search(query: String, filterCategory: String = "ALL"): List<SearchResultItem> {
        val q = query.trim().lowercase()
        val results = mutableListOf<SearchResultItem>()

        // 1. Files
        if (filterCategory == "ALL" || filterCategory == "FILE") {
            val matchedFiles = if (q.isEmpty()) mockLocalFiles else {
                mockLocalFiles.filter { it.title.lowercase().contains(q) || it.subtitle.lowercase().contains(q) }
            }
            results.addAll(matchedFiles)
        }

        // 2. Memories
        if (filterCategory == "ALL" || filterCategory == "MEMORY") {
            val memories = repo.memories.first()
            val matchedMemories = memories
                .filter { q.isEmpty() || it.key.lowercase().contains(q) || it.content.lowercase().contains(q) }
                .map {
                    SearchResultItem(
                        id = "mem_${it.id}",
                        title = it.key,
                        subtitle = "${it.category} • ${it.content}",
                        category = "MEMORY",
                        iconName = "psychology",
                        timestamp = it.timestamp
                    )
                }
            results.addAll(matchedMemories)
        }

        // 3. Automations
        if (filterCategory == "ALL" || filterCategory == "AUTOMATION") {
            val automations = repo.automations.first()
            val matchedAutos = automations
                .filter { q.isEmpty() || it.title.lowercase().contains(q) || it.actionSummary.lowercase().contains(q) }
                .map {
                    SearchResultItem(
                        id = "auto_${it.id}",
                        title = it.title,
                        subtitle = "${it.triggerConfig} • ${it.actionSummary}",
                        category = "AUTOMATION",
                        iconName = "auto_awesome",
                        timestamp = it.lastRunTimestamp
                    )
                }
            results.addAll(matchedAutos)
        }

        // 4. Audit Logs
        if (filterCategory == "ALL" || filterCategory == "AUDIT") {
            val logs = repo.auditLogs.first()
            val matchedLogs = logs
                .filter { q.isEmpty() || it.actionName.lowercase().contains(q) || it.details.lowercase().contains(q) }
                .map {
                    SearchResultItem(
                        id = "audit_${it.id}",
                        title = "${it.actionName} (${it.outcomeStatus})",
                        subtitle = "Risk: ${it.riskLevel} • ${it.details}",
                        category = "AUDIT",
                        iconName = "security",
                        timestamp = it.timestamp
                    )
                }
            results.addAll(matchedLogs)
        }

        return results
    }
}
