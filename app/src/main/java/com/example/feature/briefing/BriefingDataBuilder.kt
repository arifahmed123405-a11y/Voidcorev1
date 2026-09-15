package com.example.feature.briefing

import android.content.Context
import android.os.BatteryManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BriefingPreferences(
    val scheduledTime: String = "06:00 AM",
    val isEnabled: Boolean = true,
    val includeGreeting: Boolean = true,
    val includeWeather: Boolean = true,
    val includeCalendar: Boolean = true,
    val includeTasks: Boolean = true,
    val includeNotifications: Boolean = true,
    val includeMissedCalls: Boolean = true,
    val includeBattery: Boolean = true,
    val includeNews: Boolean = true,
    val includePrioritySummary: Boolean = true
)

data class BriefingBlock(
    val title: String,
    val text: String,
    val isSpoken: Boolean = true,
    val iconName: String = "info"
)

class BriefingDataBuilder(private val context: Context) {

    fun buildBriefing(prefs: BriefingPreferences = BriefingPreferences()): List<BriefingBlock> {
        val blocks = mutableListOf<BriefingBlock>()
        val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val now = Date()

        if (prefs.includeGreeting) {
            blocks.add(
                BriefingBlock(
                    title = "Greeting",
                    text = "Good morning. It is ${timeFormat.format(now)} on ${dateFormat.format(now)}. VoidCore presence initialized for your day.",
                    iconName = "wb_sunny"
                )
            )
        }

        if (prefs.includeWeather) {
            blocks.add(
                BriefingBlock(
                    title = "Weather",
                    text = "Currently 68 degrees with clear skies in your area. High of 75 expected, no severe weather alerts active.",
                    iconName = "cloud"
                )
            )
        }

        if (prefs.includeCalendar) {
            blocks.add(
                BriefingBlock(
                    title = "First Appointment",
                    text = "Your first appointment is Sprint Planning at 9:30 AM. Travel time is clear.",
                    iconName = "event"
                )
            )
        }

        if (prefs.includeTasks) {
            blocks.add(
                BriefingBlock(
                    title = "Tasks & Reminders",
                    text = "You have 3 tasks due today: Review VoidCore architecture, submit study notes, and return call to Ahmed.",
                    iconName = "checklist"
                )
            )
        }

        if (prefs.includeNotifications) {
            blocks.add(
                BriefingBlock(
                    title = "Overnight Messages",
                    text = "You received 2 priority overnight messages: Ahmed sent project files, and system security verified clean audit logs.",
                    iconName = "mark_chat_unread"
                )
            )
        }

        if (prefs.includeBattery) {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            val batteryPct = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 88
            blocks.add(
                BriefingBlock(
                    title = "Battery & Health",
                    text = "Device battery is at $batteryPct%. All security gates verified active.",
                    iconName = "battery_std"
                )
            )
        }

        if (prefs.includeNews) {
            blocks.add(
                BriefingBlock(
                    title = "Selected Briefing Topics",
                    text = "In tech: On-device intelligence reaches record adoption with privacy-first local models.",
                    iconName = "newspaper"
                )
            )
        }

        if (prefs.includePrioritySummary) {
            blocks.add(
                BriefingBlock(
                    title = "Focus Summary",
                    text = "What needs your attention first: Review Ahmed's study notes before your 9:30 AM meeting.",
                    iconName = "star"
                )
            )
        }

        return blocks
    }
}
