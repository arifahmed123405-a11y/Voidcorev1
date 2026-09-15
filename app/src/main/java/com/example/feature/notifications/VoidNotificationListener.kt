package com.example.feature.notifications

import android.app.Notification
import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class VoidNotificationListener : NotificationListenerService() {

    private lateinit var contextEngine: NotificationContextEngine

    override fun onCreate() {
        super.onCreate()
        contextEngine = NotificationContextEngine.getInstance(applicationContext)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
        _isConnected.value = true
        fetchActiveNotifications()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        instance = null
        _isConnected.value = false
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString() ?: ""

        val appName = try {
            val pm = packageManager
            pm.getApplicationLabel(pm.getApplicationInfo(sbn.packageName, 0)).toString()
        } catch (e: Exception) {
            sbn.packageName
        }

        val type = NotificationClassifier.classify(sbn, title, text, subText)
        val importance = NotificationClassifier.calculateImportance(sbn, type, title, text)

        var hasReply = false
        val actions = sbn.notification.actions
        if (actions != null) {
            for (action in actions) {
                if (!action.remoteInputs.isNullOrEmpty()) {
                    hasReply = true
                    break
                }
            }
        }

        val senderName = if (title.isNotBlank()) title else appName

        val model = NotificationModel(
            id = "${sbn.packageName}_${sbn.id}_${sbn.postTime}",
            packageName = sbn.packageName,
            appName = appName,
            sender = senderName,
            title = title,
            text = text,
            type = type,
            importance = importance,
            timestamp = sbn.postTime,
            hasInlineReply = hasReply,
            isSimulated = false
        )

        contextEngine.recordNotification(model)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // Handled passively
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
            _isConnected.value = false
        }
    }

    fun sendInlineReply(notificationId: String, replyMessage: String): Boolean {
        val sbn = activeNotifications?.firstOrNull { "${it.packageName}_${it.id}_${it.postTime}" == notificationId }
            ?: return false

        val actions = sbn.notification.actions ?: return false
        for (action in actions) {
            val remoteInputs = action.remoteInputs ?: continue
            for (remoteInput in remoteInputs) {
                val bundle = Bundle().apply {
                    putCharSequence(remoteInput.resultKey, replyMessage)
                }
                val fillInIntent = Intent()
                RemoteInput.addResultsToIntent(arrayOf(remoteInput), fillInIntent, bundle)
                try {
                    action.actionIntent.send(this, 0, fillInIntent)
                    return true
                } catch (e: Exception) {
                    return false
                }
            }
        }
        return false
    }

    private fun fetchActiveNotifications() {
        try {
            val active = activeNotifications ?: return
            for (sbn in active) {
                onNotificationPosted(sbn)
            }
        } catch (e: Exception) {
            // Ignore if permission issues
        }
    }

    companion object {
        @Volatile
        var instance: VoidNotificationListener? = null
            private set

        private val _isConnected = MutableStateFlow(false)
        val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    }
}
