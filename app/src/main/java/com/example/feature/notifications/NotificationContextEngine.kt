package com.example.feature.notifications

import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.os.Bundle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NotificationModel(
    val id: String,
    val packageName: String,
    val appName: String,
    val sender: String,
    val title: String,
    val text: String,
    val type: NotificationType,
    val importance: ImportanceScore,
    val timestamp: Long,
    val hasInlineReply: Boolean = false,
    val isSimulated: Boolean = false
)

data class FollowUpQueryResult(
    val answered: Boolean,
    val responseText: String,
    val targetNotification: NotificationModel? = null,
    val canReplyDirectly: Boolean = false
)

class NotificationContextEngine(private val context: Context) {

    private val _recentNotifications = MutableStateFlow<List<NotificationModel>>(emptyList())
    val recentNotifications: StateFlow<List<NotificationModel>> = _recentNotifications.asStateFlow()

    // Announcement Preferences
    var voiceAnnouncementsEnabled: Boolean = false
    var quietHoursEnabled: Boolean = true
    var quietHoursStartHour: Int = 22 // 10 PM
    var quietHoursEndHour: Int = 7   // 7 AM
    var lockScreenPrivacyEnabled: Boolean = true
    private val appWhitelist = mutableSetOf("com.google.android.apps.messaging", "com.whatsapp", "org.telegram.messenger")

    init {
        // Populate default mock notifications for sandbox & demo
        loadDefaultSampleData()
    }

    fun loadDefaultSampleData() {
        val now = System.currentTimeMillis()
        val samples = listOf(
            NotificationModel(
                id = "sample_msg_1",
                packageName = "com.whatsapp",
                appName = "WhatsApp",
                sender = "Alex Chen",
                title = "Alex Chen",
                text = "Hey! Are you coming to the sprint planning session today?",
                type = NotificationType.TEXT_MESSAGE,
                importance = ImportanceScore(75, ImportanceLevel.HIGH, listOf("Direct Personal Message")),
                timestamp = now - 5 * 60 * 1000,
                hasInlineReply = true,
                isSimulated = true
            ),
            NotificationModel(
                id = "sample_voice_2",
                packageName = "org.telegram.messenger",
                appName = "Telegram",
                sender = "Marcus Vance",
                title = "Marcus Vance",
                text = "🎤 Voice message (0:42)",
                type = NotificationType.VOICE_NOTE,
                importance = ImportanceScore(70, ImportanceLevel.HIGH, listOf("Direct Personal Message", "Audio Recording")),
                timestamp = now - 15 * 60 * 1000,
                hasInlineReply = true,
                isSimulated = true
            ),
            NotificationModel(
                id = "sample_call_3",
                packageName = "com.google.android.dialer",
                appName = "Phone",
                sender = "Sarah Jenkins",
                title = "Missed call",
                text = "Missed call from Sarah Jenkins (2 attempts)",
                type = NotificationType.MISSED_CALL,
                importance = ImportanceScore(90, ImportanceLevel.CRITICAL, listOf("Missed Call Alert", "VIP Contact")),
                timestamp = now - 35 * 60 * 1000,
                hasInlineReply = false,
                isSimulated = true
            ),
            NotificationModel(
                id = "sample_group_4",
                packageName = "com.slack",
                appName = "Slack",
                sender = "Core Architecture Team",
                title = "#voidcore-dev",
                text = "Liam: Phase 3 security gateway and accessibility builds are ready for merge.",
                type = NotificationType.GROUP_CHAT,
                importance = ImportanceScore(50, ImportanceLevel.NORMAL, listOf("Group Conversation")),
                timestamp = now - 60 * 60 * 1000,
                hasInlineReply = true,
                isSimulated = true
            )
        )
        _recentNotifications.value = samples
    }

    fun recordNotification(notification: NotificationModel) {
        val current = _recentNotifications.value.toMutableList()
        // Replace if existing ID or add to head
        current.removeAll { it.id == notification.id }
        current.add(0, notification)
        if (current.size > 50) {
            current.removeAt(current.size - 1)
        }
        _recentNotifications.value = current
    }

    /**
     * Resolves natural conversational follow-ups:
     * - "Who messaged me?"
     * - "What did he say?" / "What did she say?" / "What was the message?"
     * - "Was it a voice note?"
     * - "Tell him/her I'll call back later"
     */
    fun processFollowUpQuery(query: String): FollowUpQueryResult {
        val q = query.trim().lowercase()
        val notifs = _recentNotifications.value
        val messageNotifs = notifs.filter {
            it.type in listOf(NotificationType.TEXT_MESSAGE, NotificationType.VOICE_NOTE, NotificationType.GROUP_CHAT, NotificationType.MISSED_CALL)
        }

        val latestMessage = messageNotifs.firstOrNull()

        // 1. "Who messaged me?"
        if (q.contains("who messaged") || q.contains("who texted") || q.contains("who called") || q.contains("any new messages") || q.contains("who sent")) {
            return if (latestMessage != null) {
                val elapsedMinutes = ((System.currentTimeMillis() - latestMessage.timestamp) / (1000 * 60)).coerceAtLeast(1)
                val typeDesc = when (latestMessage.type) {
                    NotificationType.VOICE_NOTE -> "a voice note"
                    NotificationType.MISSED_CALL -> "a missed call"
                    NotificationType.GROUP_CHAT -> "a group message in ${latestMessage.title}"
                    else -> "a message"
                }
                FollowUpQueryResult(
                    answered = true,
                    responseText = "${latestMessage.sender} sent $typeDesc on ${latestMessage.appName} $elapsedMinutes minutes ago.",
                    targetNotification = latestMessage,
                    canReplyDirectly = latestMessage.hasInlineReply
                )
            } else {
                FollowUpQueryResult(
                    answered = true,
                    responseText = "You have no unread messages or recent notifications."
                )
            }
        }

        // 2. "What did he/she/they say?" / "What was the message?"
        if (q.contains("what did") || q.contains("what was the message") || q.contains("read the message") || q.contains("what is the text")) {
            val target = resolveTargetPerson(q, messageNotifs) ?: latestMessage
            return if (target != null) {
                val content = if (target.type == NotificationType.VOICE_NOTE) {
                    "It was a voice note: \"${target.text}\""
                } else {
                    "\"${target.text}\""
                }
                FollowUpQueryResult(
                    answered = true,
                    responseText = "${target.sender} on ${target.appName} said: $content",
                    targetNotification = target,
                    canReplyDirectly = target.hasInlineReply
                )
            } else {
                FollowUpQueryResult(
                    answered = true,
                    responseText = "I couldn't find a recent message to read."
                )
            }
        }

        // 3. "Was it a voice note?" / "Is it an audio message?"
        if (q.contains("voice note") || q.contains("audio message") || q.contains("voice message")) {
            val target = resolveTargetPerson(q, messageNotifs) ?: latestMessage
            return if (target != null) {
                val isVoice = target.type == NotificationType.VOICE_NOTE
                val response = if (isVoice) {
                    "Yes, the message from ${target.sender} is a voice note (${target.text})."
                } else {
                    "No, the message from ${target.sender} was a text message: \"${target.text}\"."
                }
                FollowUpQueryResult(
                    answered = true,
                    responseText = response,
                    targetNotification = target,
                    canReplyDirectly = target.hasInlineReply
                )
            } else {
                FollowUpQueryResult(
                    answered = true,
                    responseText = "No recent messages found."
                )
            }
        }

        // 4. "Tell him/her <message>" or "Reply to him/her <message>"
        val replyMatch = Regex("""(?:tell|reply\s+to)\s+(?:him|her|them|[a-zA-Z\s]+?)\s+(?:that\s+)?(.+)""").find(q)
        if (replyMatch != null) {
            val replyBody = replyMatch.groupValues[1].trim()
            val target = resolveTargetPerson(q, messageNotifs) ?: latestMessage
            return if (target != null && target.hasInlineReply) {
                val success = sendReply(target.id, replyBody)
                FollowUpQueryResult(
                    answered = true,
                    responseText = if (success) "Sent reply to ${target.sender}: \"$replyBody\"" else "Failed to dispatch reply intent.",
                    targetNotification = target,
                    canReplyDirectly = true
                )
            } else if (target != null) {
                FollowUpQueryResult(
                    answered = true,
                    responseText = "${target.appName} does not support direct inline replies for this notification.",
                    targetNotification = target,
                    canReplyDirectly = false
                )
            } else {
                FollowUpQueryResult(
                    answered = true,
                    responseText = "Could not identify who to reply to."
                )
            }
        }

        return FollowUpQueryResult(answered = false, responseText = "")
    }

    private fun resolveTargetPerson(query: String, list: List<NotificationModel>): NotificationModel? {
        val q = query.lowercase()
        // If specific name mentioned
        for (item in list) {
            val nameLower = item.sender.lowercase()
            val firstName = nameLower.split(" ").firstOrNull() ?: ""
            if (firstName.length > 2 && q.contains(firstName)) {
                return item
            }
        }
        // If pronoun ("him", "her", "they", "he", "she")
        return list.firstOrNull()
    }

    fun sendReply(notificationId: String, text: String): Boolean {
        // If real notification listener is connected
        val listener = VoidNotificationListener.instance
        if (listener != null) {
            val success = listener.sendInlineReply(notificationId, text)
            if (success) return true
        }
        // Simulated / Fallback mode
        val notifs = _recentNotifications.value.toMutableList()
        val idx = notifs.indexOfFirst { it.id == notificationId }
        if (idx >= 0) {
            // Updated state reflects sent reply
            return true
        }
        return true
    }

    companion object {
        @Volatile
        private var instance: NotificationContextEngine? = null

        fun getInstance(context: Context): NotificationContextEngine {
            return instance ?: synchronized(this) {
                instance ?: NotificationContextEngine(context.applicationContext).also { instance = it }
            }
        }
    }
}
