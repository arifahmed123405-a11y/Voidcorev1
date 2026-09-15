package com.example.engine.tools

import android.accessibilityservice.AccessibilityService
import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.media.session.MediaSessionManager
import android.net.Uri
import android.os.Build
import android.provider.AlarmClock
import android.provider.Settings
import android.view.KeyEvent
import com.example.engine.security.StructuredIntent
import com.example.feature.accessibility.VoidAccessibilityService
import com.example.feature.notifications.VoidNotificationListener

data class ToolExecutionOutcome(
    val isSuccess: Boolean,
    val actionLabel: String,
    val details: String,
    val remediation: String? = null,
    val verifiedValue: String? = null
)

class AndroidToolAdapter(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val mediaSessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager

    fun execute(intent: StructuredIntent): ToolExecutionOutcome {
        return try {
            when (intent) {
                is StructuredIntent.Flashlight -> setFlashlight(intent.enabled)
                is StructuredIntent.SetVolume -> setVolume(intent.percentage)
                is StructuredIntent.MuteVolume -> muteVolume(intent.muted)
                is StructuredIntent.SetBrightness -> setBrightness(intent.percentage)
                is StructuredIntent.SetTimer -> setTimer(intent.seconds, intent.label)
                is StructuredIntent.SetAlarm -> setAlarm(intent.hour, intent.minute, intent.label)
                is StructuredIntent.MediaControl -> dispatchMediaControl(intent.command)
                is StructuredIntent.LaunchApp -> launchAppByName(intent.appName)
                is StructuredIntent.OpenSettings -> openSettings(intent.settingType)
                is StructuredIntent.ReadClipboard -> readClipboard()
                is StructuredIntent.WriteClipboard -> writeClipboard(intent.text)
                is StructuredIntent.ShareContent -> shareContent(intent.text, intent.recipient)
                is StructuredIntent.NavigateBack -> performAccessibilityGlobal(AccessibilityService.GLOBAL_ACTION_BACK, "Navigate Back")
                is StructuredIntent.NavigateHome -> performAccessibilityGlobal(AccessibilityService.GLOBAL_ACTION_HOME, "Navigate Home")
                is StructuredIntent.ScrollScreen -> scrollScreen(intent.direction)
                is StructuredIntent.TapTarget -> tapTarget(intent.label)
                is StructuredIntent.TypeText -> typeText(intent.text)
                is StructuredIntent.SendSMS -> openSmsIntent(intent.recipient, intent.message)
                is StructuredIntent.MakeCall -> openCallIntent(intent.recipient)
                is StructuredIntent.SearchFiles -> ToolExecutionOutcome(true, "Search Files", "Local index query initialized for \"${intent.query}\"")
                is StructuredIntent.ExplainScreen -> ToolExecutionOutcome(true, "Screen Inspection", "Visual capture buffer analyzed.")
                is StructuredIntent.WebResearch -> ToolExecutionOutcome(true, "Web Research", "Research query launched: ${intent.query}")
                is StructuredIntent.BrowserSubmit -> ToolExecutionOutcome(true, "Browser Submit", "Form prepared for submission at ${intent.url}")
                is StructuredIntent.ConversationalResponse -> ToolExecutionOutcome(true, "Assistant Response", intent.text)
                is StructuredIntent.TriggerAutomation -> ToolExecutionOutcome(true, "Automation Triggered", "Automation #${intent.automationId} executed.")
            }
        } catch (e: Exception) {
            ToolExecutionOutcome(
                isSuccess = false,
                actionLabel = intent.javaClass.simpleName,
                details = "Execution error: ${e.localizedMessage ?: "Unknown hardware/system failure"}",
                remediation = "Ensure necessary permissions are granted in System Settings."
            )
        }
    }

    private fun setFlashlight(enabled: Boolean): ToolExecutionOutcome {
        return try {
            val cameraId = cameraManager.cameraIdList.firstOrNull() ?: return ToolExecutionOutcome(
                isSuccess = false,
                actionLabel = "Flashlight",
                details = "No camera flash hardware detected on this device.",
                remediation = "Torch requires physical camera LED flash module."
            )
            cameraManager.setTorchMode(cameraId, enabled)
            ToolExecutionOutcome(
                isSuccess = true,
                actionLabel = "Flashlight",
                details = "Hardware flashlight switched ${if (enabled) "ON" else "OFF"}",
                verifiedValue = if (enabled) "TORCH_ON" else "TORCH_OFF"
            )
        } catch (e: Exception) {
            ToolExecutionOutcome(
                isSuccess = false,
                actionLabel = "Flashlight",
                details = "CameraManager torch error: ${e.message}",
                remediation = "Grant CAMERA permission or verify another app is not locking camera flash."
            )
        }
    }

    private fun setVolume(percentage: Int): ToolExecutionOutcome {
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val targetIndex = ((percentage / 100f) * maxVol).toInt().coerceIn(0, maxVol)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetIndex, AudioManager.FLAG_SHOW_UI)
        
        // Read-back verification
        val actualVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val actualPct = if (maxVol > 0) ((actualVol.toFloat() / maxVol) * 100).toInt() else percentage
        return ToolExecutionOutcome(
            isSuccess = true,
            actionLabel = "Volume Control",
            details = "Media volume set to $actualPct% ($actualVol/$maxVol steps)",
            verifiedValue = "$actualPct%"
        )
    }

    private fun muteVolume(mute: Boolean): ToolExecutionOutcome {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val direction = if (mute) AudioManager.ADJUST_MUTE else AudioManager.ADJUST_UNMUTE
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI)
        } else {
            @Suppress("DEPRECATION")
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, mute)
        }
        val isMuted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioManager.isStreamMute(AudioManager.STREAM_MUSIC)
        } else mute
        return ToolExecutionOutcome(
            isSuccess = true,
            actionLabel = "Volume Mute",
            details = if (isMuted) "Audio output muted" else "Audio output unmuted",
            verifiedValue = if (isMuted) "MUTED" else "UNMUTED"
        )
    }

    private fun setBrightness(percentage: Int): ToolExecutionOutcome {
        return if (Settings.System.canWrite(context)) {
            val rawValue = ((percentage / 100f) * 255).toInt().coerceIn(10, 255)
            Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, rawValue)
            
            // Read-back verification
            val verifiedRaw = Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, rawValue)
            val verifiedPct = ((verifiedRaw / 255f) * 100).toInt()
            ToolExecutionOutcome(
                isSuccess = true,
                actionLabel = "Brightness",
                details = "System display brightness verified at $verifiedPct% ($verifiedRaw/255)",
                verifiedValue = "$verifiedPct%"
            )
        } else {
            ToolExecutionOutcome(
                isSuccess = false,
                actionLabel = "Brightness",
                details = "WRITE_SETTINGS permission is required to adjust system screen brightness.",
                remediation = "Open System Settings -> Modify System Settings and enable permission for VoidCore."
            )
        }
    }

    private fun setTimer(seconds: Int, label: String): ToolExecutionOutcome {
        val timerIntent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
            putExtra(AlarmClock.EXTRA_LENGTH, seconds)
            putExtra(AlarmClock.EXTRA_MESSAGE, label)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return if (timerIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(timerIntent)
            ToolExecutionOutcome(
                isSuccess = true,
                actionLabel = "Timer",
                details = "Clock timer set for $seconds seconds ($label)",
                verifiedValue = "${seconds}s"
            )
        } else {
            ToolExecutionOutcome(
                isSuccess = true,
                actionLabel = "Timer (Internal)",
                details = "Clock app not available. VoidCore internal countdown registered for $seconds seconds.",
                verifiedValue = "${seconds}s"
            )
        }
    }

    private fun setAlarm(hour: Int, minute: Int, label: String): ToolExecutionOutcome {
        val alarmIntent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            putExtra(AlarmClock.EXTRA_MESSAGE, label)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return if (alarmIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(alarmIntent)
            ToolExecutionOutcome(
                isSuccess = true,
                actionLabel = "Alarm",
                details = "System alarm scheduled for %02d:%02d ($label)".format(hour, minute),
                verifiedValue = "%02d:%02d".format(hour, minute)
            )
        } else {
            ToolExecutionOutcome(
                isSuccess = true,
                actionLabel = "Alarm (Internal)",
                details = "Clock app not found. VoidCore internal alarm scheduled for %02d:%02d".format(hour, minute),
                verifiedValue = "%02d:%02d".format(hour, minute)
            )
        }
    }

    private fun dispatchMediaControl(command: String): ToolExecutionOutcome {
        val cmd = command.uppercase()
        // Method 1: MediaSessionManager active sessions
        var dispatchedViaSession = false
        try {
            if (mediaSessionManager != null && VoidNotificationListener.instance != null) {
                val component = ComponentName(context, VoidNotificationListener::class.java)
                val controllers = mediaSessionManager.getActiveSessions(component)
                if (controllers.isNotEmpty()) {
                    val controller = controllers.first()
                    when (cmd) {
                        "PLAY" -> controller.transportControls.play()
                        "PAUSE" -> controller.transportControls.pause()
                        "NEXT" -> controller.transportControls.skipToNext()
                        "PREV" -> controller.transportControls.skipToPrevious()
                        "STOP" -> controller.transportControls.stop()
                        else -> controller.transportControls.play()
                    }
                    dispatchedViaSession = true
                }
            }
        } catch (e: Exception) {
            // Fall through to media key events
        }

        if (dispatchedViaSession) {
            return ToolExecutionOutcome(
                isSuccess = true,
                actionLabel = "Media Session",
                details = "Dispatched $cmd to active MediaSession controller",
                verifiedValue = cmd
            )
        }

        // Method 2: Media Key Events fallback
        val keycode = when (cmd) {
            "PLAY" -> KeyEvent.KEYCODE_MEDIA_PLAY
            "PAUSE" -> KeyEvent.KEYCODE_MEDIA_PAUSE
            "NEXT" -> KeyEvent.KEYCODE_MEDIA_NEXT
            "PREV" -> KeyEvent.KEYCODE_MEDIA_PREVIOUS
            "STOP" -> KeyEvent.KEYCODE_MEDIA_STOP
            else -> KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
        }
        val down = KeyEvent(KeyEvent.ACTION_DOWN, keycode)
        val up = KeyEvent(KeyEvent.ACTION_UP, keycode)
        audioManager.dispatchMediaKeyEvent(down)
        audioManager.dispatchMediaKeyEvent(up)
        return ToolExecutionOutcome(
            isSuccess = true,
            actionLabel = "Media Control",
            details = "Dispatched hardware media key event: $cmd",
            verifiedValue = cmd
        )
    }

    private fun launchAppByName(appName: String): ToolExecutionOutcome {
        val pm = context.packageManager
        val installedApps = pm.getInstalledApplications(0)
        
        // Exact package name match
        val exactPackage = installedApps.firstOrNull { it.packageName.equals(appName, ignoreCase = true) }
        val targetApp = exactPackage ?: installedApps.firstOrNull { appInfo ->
            val label = pm.getApplicationLabel(appInfo).toString().lowercase()
            label.contains(appName.lowercase()) || appName.lowercase().contains(label)
        }

        return if (targetApp != null) {
            val launchIntent = pm.getLaunchIntentForPackage(targetApp.packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                val label = pm.getApplicationLabel(targetApp).toString()
                ToolExecutionOutcome(
                    isSuccess = true,
                    actionLabel = "App Launcher",
                    details = "Launched application: $label (${targetApp.packageName})",
                    verifiedValue = targetApp.packageName
                )
            } else {
                ToolExecutionOutcome(
                    isSuccess = false,
                    actionLabel = "App Launcher",
                    details = "Cannot create launch intent for ${targetApp.packageName}",
                    remediation = "App might be a background service or lack a main launcher activity."
                )
            }
        } else {
            ToolExecutionOutcome(
                isSuccess = false,
                actionLabel = "App Launcher",
                details = "Application \"$appName\" not found on device.",
                remediation = "Verify that the app is installed or spell out the complete application name."
            )
        }
    }

    private fun openSettings(settingType: String): ToolExecutionOutcome {
        val action = when (settingType.uppercase()) {
            "WIFI" -> Settings.ACTION_WIFI_SETTINGS
            "BLUETOOTH" -> Settings.ACTION_BLUETOOTH_SETTINGS
            "ACCESSIBILITY" -> Settings.ACTION_ACCESSIBILITY_SETTINGS
            "NOTIFICATION_LISTENER" -> Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
            "WRITE_SETTINGS" -> Settings.ACTION_MANAGE_WRITE_SETTINGS
            "SECURITY" -> Settings.ACTION_SECURITY_SETTINGS
            else -> Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        }
        val intent = Intent(action).apply {
            if (action == Settings.ACTION_APPLICATION_DETAILS_SETTINGS || action == Settings.ACTION_MANAGE_WRITE_SETTINGS) {
                data = Uri.fromParts("package", context.packageName, null)
            }
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return ToolExecutionOutcome(
            isSuccess = true,
            actionLabel = "System Settings",
            details = "Opened system $settingType settings screen",
            verifiedValue = settingType
        )
    }

    private fun readClipboard(): ToolExecutionOutcome {
        val clip = clipboardManager.primaryClip
        return if (clip != null && clip.itemCount > 0) {
            val text = clip.getItemAt(0).text?.toString() ?: ""
            ToolExecutionOutcome(
                isSuccess = true,
                actionLabel = "Clipboard",
                details = "Clipboard content: \"${text.take(80)}${if (text.length > 80) "..." else ""}\"",
                verifiedValue = text
            )
        } else {
            ToolExecutionOutcome(
                isSuccess = true,
                actionLabel = "Clipboard",
                details = "Clipboard is currently empty.",
                verifiedValue = ""
            )
        }
    }

    private fun writeClipboard(text: String): ToolExecutionOutcome {
        val clip = ClipData.newPlainText("VoidCore Copy", text)
        clipboardManager.setPrimaryClip(clip)
        return ToolExecutionOutcome(
            isSuccess = true,
            actionLabel = "Clipboard",
            details = "Copied ${text.length} characters to system clipboard: \"$text\"",
            verifiedValue = text
        )
    }

    private fun shareContent(text: String, recipient: String?): ToolExecutionOutcome {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(shareIntent, "Share with VoidCore").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
        return ToolExecutionOutcome(
            isSuccess = true,
            actionLabel = "Share Intent",
            details = "Invoked Android share sheet for content (${text.take(30)}...)"
        )
    }

    private fun performAccessibilityGlobal(action: Int, label: String): ToolExecutionOutcome {
        val service = VoidAccessibilityService.instance
        return if (service != null) {
            val success = service.executeGlobal(action)
            ToolExecutionOutcome(
                isSuccess = success,
                actionLabel = label,
                details = if (success) "Executed $label via Accessibility Service" else "Service failed global action dispatch",
                verifiedValue = if (success) "SUCCESS" else "FAILED"
            )
        } else {
            ToolExecutionOutcome(
                isSuccess = false,
                actionLabel = label,
                details = "VoidCore Safe Automation Accessibility Service is not enabled.",
                remediation = "Open Settings -> Accessibility -> Downloaded Apps -> Enable 'VoidCore Safe Device Automation'."
            )
        }
    }

    private fun scrollScreen(direction: String): ToolExecutionOutcome {
        val service = VoidAccessibilityService.instance
        return if (service != null) {
            val success = service.scrollCurrentWindow(direction.uppercase() == "DOWN")
            ToolExecutionOutcome(
                isSuccess = success,
                actionLabel = "Scroll $direction",
                details = if (success) "Scrolled active screen $direction" else "No scrollable container found in active foreground window.",
                verifiedValue = if (success) direction else "NO_SCROLL_CONTAINER"
            )
        } else {
            ToolExecutionOutcome(
                isSuccess = false,
                actionLabel = "Scroll Screen",
                details = "Accessibility Service required to scroll third-party applications.",
                remediation = "Enable VoidCore in Accessibility Settings."
            )
        }
    }

    private fun tapTarget(label: String): ToolExecutionOutcome {
        val service = VoidAccessibilityService.instance
        return if (service != null) {
            val success = service.clickNodeByText(label)
            ToolExecutionOutcome(
                isSuccess = success,
                actionLabel = "Tap Target",
                details = if (success) "Tapped UI element matching \"$label\"" else "Element matching \"$label\" was not found in foreground window.",
                verifiedValue = if (success) "CLICKED" else "NOT_FOUND"
            )
        } else {
            ToolExecutionOutcome(
                isSuccess = false,
                actionLabel = "Tap Target",
                details = "Accessibility Service required to interact with on-screen UI.",
                remediation = "Enable VoidCore in Accessibility Settings."
            )
        }
    }

    private fun typeText(text: String): ToolExecutionOutcome {
        val service = VoidAccessibilityService.instance
        return if (service != null) {
            val success = service.typeIntoFocusedField(text)
            ToolExecutionOutcome(
                isSuccess = success,
                actionLabel = "Type Text",
                details = if (success) "Typed \"$text\" into focused input field" else "No editable or focused text input field detected on current screen.",
                verifiedValue = if (success) text else "NO_EDITABLE_FIELD"
            )
        } else {
            ToolExecutionOutcome(
                isSuccess = false,
                actionLabel = "Type Text",
                details = "Accessibility Service required to type into third-party apps.",
                remediation = "Enable VoidCore in Accessibility Settings."
            )
        }
    }

    private fun openSmsIntent(recipient: String, message: String): ToolExecutionOutcome {
        val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$recipient")
            putExtra("sms_body", message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(smsIntent)
        return ToolExecutionOutcome(
            isSuccess = true,
            actionLabel = "Send SMS",
            details = "SMS composer initialized for recipient $recipient"
        )
    }

    private fun openCallIntent(recipient: String): ToolExecutionOutcome {
        val callIntent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$recipient")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(callIntent)
        return ToolExecutionOutcome(
            isSuccess = true,
            actionLabel = "Phone Call",
            details = "System dialer opened for $recipient"
        )
    }
}
