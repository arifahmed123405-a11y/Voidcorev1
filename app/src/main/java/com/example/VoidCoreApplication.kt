package com.example

import android.app.Application
import com.example.core.database.AutomationEntity
import com.example.core.database.MemoryEntity
import com.example.core.database.ProjectEntity
import com.example.core.database.VoidCoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class VoidCoreApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val repo = VoidCoreRepository.getInstance(this)
        
        // Seed initial templates if empty
        CoroutineScope(Dispatchers.IO).launch {
            val automations = repo.automations.first()
            if (automations.isEmpty()) {
                repo.saveAutomation(
                    AutomationEntity(
                        title = "6:00 AM Morning Briefing",
                        triggerType = "TIME_SCHEDULE",
                        triggerConfig = "Every morning at 6:00 AM",
                        actionSummary = "Call-style wake surface: spoken weather, priority tasks, overnight notifications & agenda",
                        isEnabled = true
                    )
                )
                repo.saveAutomation(
                    AutomationEntity(
                        title = "Callback Reminder in 30 Min",
                        triggerType = "CALL_FOLLOWUP",
                        triggerConfig = "After incoming call screen message",
                        actionSummary = "Sets high-priority alarm & notification for returned call",
                        isEnabled = true
                    )
                )
                repo.saveAutomation(
                    AutomationEntity(
                        title = "Headphones Connected -> Study Playlist",
                        triggerType = "HEADPHONE_CONNECT",
                        triggerConfig = "When Bluetooth or wired audio connects",
                        actionSummary = "Suggests focus mode and opens media",
                        isEnabled = true
                    )
                )
                repo.saveAutomation(
                    AutomationEntity(
                        title = "Bedtime Mode & Night Voice",
                        triggerType = "TIME_SCHEDULE",
                        triggerConfig = "Daily at 10:30 PM",
                        actionSummary = "Dim presence brightness, lower TTS cadence, activate DND check",
                        isEnabled = false
                    )
                )
            }

            val memories = repo.memories.first()
            if (memories.isEmpty()) {
                repo.addMemory(
                    key = "Primary User Name",
                    content = "User prefers to be addressed by name in briefings",
                    category = "PREFERENCE",
                    provenance = "Initial system setup"
                )
                repo.addMemory(
                    key = "Morning Routine",
                    content = "Prefers weather and high priority messages read first before news",
                    category = "ROUTINE",
                    provenance = "Learned preference"
                )
            }

            val projects = repo.projects.first()
            if (projects.isEmpty()) {
                repo.saveProject(
                    ProjectEntity(
                        title = "VoidCore Operating Workspace",
                        goal = "Autonomous, local-first on-device personal intelligence hub",
                        status = "Active",
                        notes = "All device operations gated by security policy and local audit."
                    )
                )
            }
        }
    }
}
