package com.example.core.database

import android.content.Context
import kotlinx.coroutines.flow.Flow

class VoidCoreRepository(private val dao: VoidCoreDao) {

    val auditLogs: Flow<List<AuditLogEntity>> = dao.getAllAuditLogs()
    val memories: Flow<List<MemoryEntity>> = dao.getAllMemories()
    val automations: Flow<List<AutomationEntity>> = dao.getAllAutomations()
    val projects: Flow<List<ProjectEntity>> = dao.getAllProjects()
    val conversationMessages: Flow<List<ConversationMessageEntity>> = dao.getAllMessages()

    suspend fun logAudit(
        actionName: String,
        target: String,
        riskLevel: String,
        securityDecision: String,
        outcomeStatus: String,
        details: String
    ) {
        dao.insertAuditLog(
            AuditLogEntity(
                actionName = actionName,
                target = target,
                riskLevel = riskLevel,
                securityDecision = securityDecision,
                outcomeStatus = outcomeStatus,
                details = details
            )
        )
    }

    suspend fun addMemory(key: String, content: String, category: String, provenance: String) {
        dao.insertMemory(
            MemoryEntity(
                key = key,
                content = content,
                category = category,
                provenance = provenance
            )
        )
    }

    suspend fun deleteMemory(id: Long) {
        dao.deleteMemoryById(id)
    }

    suspend fun clearMemories() {
        dao.clearAllMemories()
    }

    suspend fun saveAutomation(automation: AutomationEntity) {
        if (automation.id == 0L) {
            dao.insertAutomation(automation)
        } else {
            dao.updateAutomation(automation)
        }
    }

    suspend fun toggleAutomation(automation: AutomationEntity, isEnabled: Boolean) {
        dao.updateAutomation(automation.copy(isEnabled = isEnabled))
    }

    suspend fun deleteAutomation(id: Long) {
        dao.deleteAutomationById(id)
    }

    suspend fun saveProject(project: ProjectEntity) {
        if (project.id == 0L) {
            dao.insertProject(project)
        } else {
            dao.updateProject(project.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun deleteProject(id: Long) {
        dao.deleteProjectById(id)
    }

    suspend fun saveMessage(sender: String, content: String, actionCardJson: String? = null) {
        dao.insertMessage(
            ConversationMessageEntity(
                sender = sender,
                content = content,
                actionCardJson = actionCardJson
            )
        )
    }

    suspend fun clearConversation() {
        dao.clearMessages()
    }

    companion object {
        @Volatile
        private var INSTANCE: VoidCoreRepository? = null

        fun getInstance(context: Context): VoidCoreRepository {
            return INSTANCE ?: synchronized(this) {
                val db = VoidCoreDatabase.getDatabase(context)
                val repo = VoidCoreRepository(db.voidCoreDao())
                INSTANCE = repo
                repo
            }
        }
    }
}
