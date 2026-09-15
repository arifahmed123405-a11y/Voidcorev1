package com.example.feature.notifications

import android.app.Notification
import android.service.notification.StatusBarNotification

enum class NotificationType {
    TEXT_MESSAGE,
    VOICE_NOTE,
    IMAGE,
    VIDEO,
    DOCUMENT,
    MISSED_CALL,
    GROUP_CHAT,
    SYSTEM,
    OTHER
}

enum class ImportanceLevel {
    LOW,
    NORMAL,
    HIGH,
    CRITICAL
}

data class ImportanceScore(
    val score: Int, // 0 - 100
    val level: ImportanceLevel,
    val reasons: List<String>
)

object NotificationClassifier {

    private val urgentKeywords = listOf("urgent", "emergency", "asap", "call me", "help", "critical", "immediately", "important")
    private val voiceNoteKeywords = listOf("voice message", "voice note", "audio message", "audio clip", "ptt", "🎤")
    private val imageKeywords = listOf("photo", "image", "picture", "📷", "sent a photo")
    private val videoKeywords = listOf("video", "movie", "📹", "sent a video")
    private val docKeywords = listOf("document", ".pdf", ".docx", ".xlsx", ".zip", "attachment", "file", "📄")

    fun classify(
        sbn: StatusBarNotification,
        title: String,
        text: String,
        subText: String = ""
    ): NotificationType {
        val combined = "$title $text $subText".lowercase()
        val pkg = sbn.packageName.lowercase()
        val category = sbn.notification.category

        // 1. Missed Call
        if (category == Notification.CATEGORY_MISSED_CALL || combined.contains("missed call") || combined.contains("call from")) {
            return NotificationType.MISSED_CALL
        }

        // 2. Voice Note
        if (voiceNoteKeywords.any { combined.contains(it) }) {
            return NotificationType.VOICE_NOTE
        }

        // 3. Image / Video / Document
        val extras = sbn.notification.extras
        if (extras.containsKey(Notification.EXTRA_PICTURE) || imageKeywords.any { combined.contains(it) }) {
            return NotificationType.IMAGE
        }
        if (videoKeywords.any { combined.contains(it) }) {
            return NotificationType.VIDEO
        }
        if (docKeywords.any { combined.contains(it) }) {
            return NotificationType.DOCUMENT
        }

        // 4. Group Chat
        val isGroup = extras.getBoolean(Notification.EXTRA_IS_GROUP_CONVERSATION, false) ||
                extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE) != null ||
                title.contains(":") || combined.contains("group")
        if (isGroup && (category == Notification.CATEGORY_MESSAGE || pkg.contains("whatsapp") || pkg.contains("telegram") || pkg.contains("slack") || pkg.contains("discord"))) {
            return NotificationType.GROUP_CHAT
        }

        // 5. Standard Direct Message
        if (category == Notification.CATEGORY_MESSAGE || pkg.contains("message") || pkg.contains("whatsapp") || pkg.contains("telegram") || pkg.contains("signal") || pkg.contains("viber")) {
            return NotificationType.TEXT_MESSAGE
        }

        // 6. System / Other
        if (category == Notification.CATEGORY_SYSTEM || category == Notification.CATEGORY_SERVICE || category == Notification.CATEGORY_STATUS) {
            return NotificationType.SYSTEM
        }

        return NotificationType.OTHER
    }

    fun calculateImportance(
        sbn: StatusBarNotification,
        type: NotificationType,
        title: String,
        text: String,
        isVipContact: Boolean = false
    ): ImportanceScore {
        var score = 40 // Baseline
        val reasons = mutableListOf<String>()

        val combined = "$title $text".lowercase()

        // VIP contact boost
        if (isVipContact) {
            score += 35
            reasons.add("VIP Contact")
        }

        // Missed calls are urgent
        if (type == NotificationType.MISSED_CALL) {
            score += 40
            reasons.add("Missed Call Alert")
        }

        // Direct 1-on-1 messages
        if (type == NotificationType.TEXT_MESSAGE || type == NotificationType.VOICE_NOTE) {
            score += 20
            reasons.add("Direct Personal Message")
        }

        // Group messages get a slight penalty to prevent spam
        if (type == NotificationType.GROUP_CHAT) {
            score -= 10
            reasons.add("Group Conversation")
        }

        // Urgent keyword detector
        if (urgentKeywords.any { combined.contains(it) }) {
            score += 25
            reasons.add("Urgent Keyword Detected")
        }

        // System notifications baseline low
        if (type == NotificationType.SYSTEM || type == NotificationType.OTHER) {
            score = (score - 20).coerceAtLeast(10)
            reasons.add("Standard System Notification")
        }

        val clampedScore = score.coerceIn(0, 100)
        val level = when {
            clampedScore >= 80 -> ImportanceLevel.CRITICAL
            clampedScore >= 60 -> ImportanceLevel.HIGH
            clampedScore >= 35 -> ImportanceLevel.NORMAL
            else -> ImportanceLevel.LOW
        }

        return ImportanceScore(clampedScore, level, reasons)
    }
}
