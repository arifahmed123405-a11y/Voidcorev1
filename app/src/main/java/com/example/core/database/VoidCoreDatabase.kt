package com.example.core.database

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import android.content.Context
import kotlinx.coroutines.flow.Flow

// 1. Audit Log Entity
@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val actionName: String,
    val target: String,
    val riskLevel: String, // LOW, MEDIUM, HIGH
    val securityDecision: String, // APPROVED, BLOCKED, USER_CONFIRMED, REJECTED
    val outcomeStatus: String, // SUCCESS, FAILED, CANCELLED
    val details: String
)

// 2. Durable User Memory Entity
@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val key: String,
    val content: String,
    val category: String, // PREFERENCE, ROUTINE, ALIAS, PROJECT_FACT
    val provenance: String, // "Learned from voice command", "Manual entry", etc.
    val timestamp: Long = System.currentTimeMillis()
)

// 3. Automation Entity
@Entity(tableName = "automations")
data class AutomationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val triggerType: String, // TIME_SCHEDULE, HEADPHONE_CONNECT, NOTIFICATION_TRIGGER, CALL_FOLLOWUP
    val triggerConfig: String, // e.g., "06:00 AM Daily", "Bluetooth connected", etc.
    val actionSummary: String,
    val isEnabled: Boolean = true,
    val lastRunTimestamp: Long = 0L
)

// 4. Project Workspace Entity
@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val goal: String,
    val status: String = "Active", // Active, Paused, Completed
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

// 5. Conversation Message Entity
@Entity(tableName = "conversation_messages")
data class ConversationMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String, // "user" or "assistant"
    val content: String,
    val actionCardJson: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface VoidCoreDao {
    // Audit Logs
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 200")
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity): Long

    // Memories
    @Query("SELECT * FROM memories ORDER BY timestamp DESC")
    fun getAllMemories(): Flow<List<MemoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryEntity): Long

    @Query("DELETE FROM memories WHERE id = :id")
    suspend fun deleteMemoryById(id: Long)

    @Query("DELETE FROM memories")
    suspend fun clearAllMemories()

    // Automations
    @Query("SELECT * FROM automations ORDER BY id ASC")
    fun getAllAutomations(): Flow<List<AutomationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAutomation(automation: AutomationEntity): Long

    @Update
    suspend fun updateAutomation(automation: AutomationEntity)

    @Query("DELETE FROM automations WHERE id = :id")
    suspend fun deleteAutomationById(id: Long)

    // Projects
    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProjectById(id: Long)

    // Messages
    @Query("SELECT * FROM conversation_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ConversationMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ConversationMessageEntity): Long

    @Query("DELETE FROM conversation_messages")
    suspend fun clearMessages()
}

@Database(
    entities = [
        AuditLogEntity::class,
        MemoryEntity::class,
        AutomationEntity::class,
        ProjectEntity::class,
        ConversationMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class VoidCoreDatabase : RoomDatabase() {
    abstract fun voidCoreDao(): VoidCoreDao

    companion object {
        @Volatile
        private var INSTANCE: VoidCoreDatabase? = null

        fun getDatabase(context: Context): VoidCoreDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VoidCoreDatabase::class.java,
                    "voidcore_database.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
