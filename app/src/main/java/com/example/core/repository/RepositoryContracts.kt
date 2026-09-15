package com.example.core.repository

import com.example.core.model.ActivityLogEntry
import com.example.core.model.AudioProfile
import com.example.core.model.AutomationRule
import com.example.core.model.CommandAlias
import com.example.core.model.ContextEntry
import com.example.core.model.ConversationContext
import com.example.core.model.MemoryEntry
import com.example.core.model.TrustRule
import com.example.core.model.UserPreferences
import com.example.core.model.VoiceProfile
import com.example.core.model.VoicePresets
import com.example.core.model.WorkflowDefinition
import com.example.core.model.WorkflowInstance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface PreferencesRepository {
    val preferencesFlow: Flow<UserPreferences>
    suspend fun getPreferences(): UserPreferences
    suspend fun updatePreferences(preferences: UserPreferences)
}

interface VoiceProfileRepository {
    fun getAvailableProfiles(): List<VoiceProfile>
    fun getProfileById(id: String): VoiceProfile?
    val audioProfileFlow: Flow<AudioProfile>
    suspend fun getAudioProfile(): AudioProfile
    suspend fun updateAudioProfile(profile: AudioProfile)
}

interface TrustRulesRepository {
    val rulesFlow: Flow<List<TrustRule>>
    suspend fun getRules(): List<TrustRule>
    suspend fun addRule(rule: TrustRule)
    suspend fun updateRule(rule: TrustRule)
    suspend fun deleteRule(ruleId: String)
    suspend fun getAliases(): List<CommandAlias>
    suspend fun addAlias(alias: CommandAlias)
}

interface ContextRepository {
    val currentContextFlow: Flow<ConversationContext>
    suspend fun getActiveContext(): ConversationContext
    suspend fun addEntry(entry: ContextEntry)
    suspend fun clearContext()
}

interface WorkflowRepository {
    val definitionsFlow: Flow<List<WorkflowDefinition>>
    suspend fun getDefinitions(): List<WorkflowDefinition>
    suspend fun saveDefinition(definition: WorkflowDefinition)
    suspend fun getActiveInstances(): List<WorkflowInstance>
    suspend fun saveInstance(instance: WorkflowInstance)
}

interface AutomationRepository {
    val automationsFlow: Flow<List<AutomationRule>>
    suspend fun getAutomations(): List<AutomationRule>
    suspend fun saveAutomation(rule: AutomationRule)
    suspend fun toggleAutomation(id: String, enabled: Boolean)
}

interface ActivityRepository {
    val activityLogsFlow: Flow<List<ActivityLogEntry>>
    suspend fun getRecentLogs(limit: Int = 100): List<ActivityLogEntry>
    suspend fun logActivity(entry: ActivityLogEntry)
    suspend fun clearLogs()
}

interface MemoryRepository {
    val memoryEntriesFlow: Flow<List<MemoryEntry>>
    suspend fun getAllMemories(): List<MemoryEntry>
    suspend fun saveMemory(entry: MemoryEntry)
    suspend fun deleteMemory(id: String)
    suspend fun searchMemories(query: String): List<MemoryEntry>
}

// In-memory reference implementations for Phase 0
class InMemoryPreferencesRepository : PreferencesRepository {
    private val _prefs = MutableStateFlow(UserPreferences())
    override val preferencesFlow: Flow<UserPreferences> = _prefs.asStateFlow()

    override suspend fun getPreferences(): UserPreferences = _prefs.value

    override suspend fun updatePreferences(preferences: UserPreferences) {
        _prefs.value = preferences
    }
}

class InMemoryVoiceProfileRepository : VoiceProfileRepository {
    private val _audioProfile = MutableStateFlow(AudioProfile())
    override val audioProfileFlow: Flow<AudioProfile> = _audioProfile.asStateFlow()

    override fun getAvailableProfiles(): List<VoiceProfile> = VoicePresets.ALL_PRESETS

    override fun getProfileById(id: String): VoiceProfile? = VoicePresets.getById(id)

    override suspend fun getAudioProfile(): AudioProfile = _audioProfile.value

    override suspend fun updateAudioProfile(profile: AudioProfile) {
        _audioProfile.value = profile
    }
}

class InMemoryTrustRulesRepository : TrustRulesRepository {
    private val _rules = MutableStateFlow<List<TrustRule>>(emptyList())
    override val rulesFlow: Flow<List<TrustRule>> = _rules.asStateFlow()
    private val aliases = mutableListOf<CommandAlias>()

    override suspend fun getRules(): List<TrustRule> = _rules.value

    override suspend fun addRule(rule: TrustRule) {
        _rules.value = _rules.value + rule
    }

    override suspend fun updateRule(rule: TrustRule) {
        _rules.value = _rules.value.map { if (it.id == rule.id) rule else it }
    }

    override suspend fun deleteRule(ruleId: String) {
        _rules.value = _rules.value.filterNot { it.id == ruleId }
    }

    override suspend fun getAliases(): List<CommandAlias> = aliases.toList()

    override suspend fun addAlias(alias: CommandAlias) {
        aliases.add(alias)
    }
}

class InMemoryContextRepository : ContextRepository {
    private val _context = MutableStateFlow(ConversationContext())
    override val currentContextFlow: Flow<ConversationContext> = _context.asStateFlow()

    override suspend fun getActiveContext(): ConversationContext = _context.value

    override suspend fun addEntry(entry: ContextEntry) {
        val current = _context.value
        _context.value = current.copy(
            entries = current.entries + entry,
            lastUpdated = System.currentTimeMillis()
        )
    }

    override suspend fun clearContext() {
        _context.value = ConversationContext()
    }
}

class InMemoryWorkflowRepository : WorkflowRepository {
    private val _definitions = MutableStateFlow<List<WorkflowDefinition>>(emptyList())
    override val definitionsFlow: Flow<List<WorkflowDefinition>> = _definitions.asStateFlow()
    private val instances = mutableListOf<WorkflowInstance>()

    override suspend fun getDefinitions(): List<WorkflowDefinition> = _definitions.value

    override suspend fun saveDefinition(definition: WorkflowDefinition) {
        _definitions.value = _definitions.value.filterNot { it.id == definition.id } + definition
    }

    override suspend fun getActiveInstances(): List<WorkflowInstance> = instances.toList()

    override suspend fun saveInstance(instance: WorkflowInstance) {
        instances.removeAll { it.instanceId == instance.instanceId }
        instances.add(instance)
    }
}

class InMemoryAutomationRepository : AutomationRepository {
    private val _automations = MutableStateFlow<List<AutomationRule>>(emptyList())
    override val automationsFlow: Flow<List<AutomationRule>> = _automations.asStateFlow()

    override suspend fun getAutomations(): List<AutomationRule> = _automations.value

    override suspend fun saveAutomation(rule: AutomationRule) {
        _automations.value = _automations.value.filterNot { it.id == rule.id } + rule
    }

    override suspend fun toggleAutomation(id: String, enabled: Boolean) {
        _automations.value = _automations.value.map {
            if (it.id == id) it.copy(isEnabled = enabled) else it
        }
    }
}

class InMemoryActivityRepository : ActivityRepository {
    private val _logs = MutableStateFlow<List<ActivityLogEntry>>(emptyList())
    override val activityLogsFlow: Flow<List<ActivityLogEntry>> = _logs.asStateFlow()

    override suspend fun getRecentLogs(limit: Int): List<ActivityLogEntry> =
        _logs.value.takeLast(limit).reversed()

    override suspend fun logActivity(entry: ActivityLogEntry) {
        _logs.value = _logs.value + entry
    }

    override suspend fun clearLogs() {
        _logs.value = emptyList()
    }
}

class InMemoryMemoryRepository : MemoryRepository {
    private val _memories = MutableStateFlow<List<MemoryEntry>>(emptyList())
    override val memoryEntriesFlow: Flow<List<MemoryEntry>> = _memories.asStateFlow()

    override suspend fun getAllMemories(): List<MemoryEntry> = _memories.value

    override suspend fun saveMemory(entry: MemoryEntry) {
        _memories.value = _memories.value.filterNot { it.id == entry.id } + entry
    }

    override suspend fun deleteMemory(id: String) {
        _memories.value = _memories.value.filterNot { it.id == id }
    }

    override suspend fun searchMemories(query: String): List<MemoryEntry> {
        val q = query.lowercase()
        return _memories.value.filter {
            it.key.lowercase().contains(q) || it.value.lowercase().contains(q)
        }
    }
}
