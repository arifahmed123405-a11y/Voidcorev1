package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CallMissed
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feature.notifications.ImportanceLevel
import com.example.feature.notifications.ImportanceScore
import com.example.feature.notifications.NotificationContextEngine
import com.example.feature.notifications.NotificationModel
import com.example.feature.notifications.NotificationType
import com.example.feature.notifications.VoidNotificationListener
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidBorderGlow
import com.example.ui.theme.VoidCyanPrimary
import com.example.ui.theme.VoidDeepNavy
import com.example.ui.theme.VoidError
import com.example.ui.theme.VoidLavender
import com.example.ui.theme.VoidSuccess
import com.example.ui.theme.VoidSurfaceCard
import com.example.ui.theme.VoidSurfaceCardElevated
import com.example.ui.theme.VoidSurfaceDark
import com.example.ui.theme.VoidVioletSecondary
import com.example.ui.theme.VoidWarning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NotificationsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val contextEngine = remember { NotificationContextEngine.getInstance(context) }
    val notifications by contextEngine.recentNotifications.collectAsState()
    val isServiceConnected by VoidNotificationListener.isConnected.collectAsState()

    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    var replyTextMap by remember { mutableStateOf(mapOf<String, String>()) }
    var sentStatusMap by remember { mutableStateOf(mapOf<String, String>()) }

    // Natural Follow-up Query State
    var naturalQueryInput by remember { mutableStateOf("") }
    var naturalQueryOutput by remember { mutableStateOf<String?>(null) }

    // Settings State
    var voiceAnnouncements by remember { mutableStateOf(contextEngine.voiceAnnouncementsEnabled) }
    var quietHours by remember { mutableStateOf(contextEngine.quietHoursEnabled) }
    var lockScreenPrivacy by remember { mutableStateOf(contextEngine.lockScreenPrivacyEnabled) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(VoidDeepNavy, VoidSurfaceDark, VoidDeepNavy)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("notifications_back_button")) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = TextSecondary)
                }
                Text(
                    text = "NOTIFICATION INTELLIGENCE",
                    color = VoidCyanPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                IconButton(
                    onClick = {
                        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.testTag("open_notification_settings_button")
                ) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Section 1: Service Listener Status Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = VoidSurfaceCardElevated),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(
                                listOf(
                                    if (isServiceConnected) VoidSuccess.copy(alpha = 0.5f) else VoidWarning.copy(alpha = 0.5f),
                                    VoidVioletSecondary.copy(alpha = 0.3f)
                                )
                            )
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isServiceConnected) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                        contentDescription = null,
                                        tint = if (isServiceConnected) VoidSuccess else VoidWarning,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isServiceConnected) "LISTENER SERVICE ACTIVE" else "LISTENER SERVICE STANDBY",
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isServiceConnected) VoidSuccess.copy(alpha = 0.15f) else VoidWarning.copy(alpha = 0.15f))
                                        .border(1.dp, if (isServiceConnected) VoidSuccess else VoidWarning, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isServiceConnected) "LIVE HOOK" else "SIMULATED / SANDBOX",
                                        color = if (isServiceConnected) VoidSuccess else VoidWarning,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (isServiceConnected) "VoidCore is actively intercepting notifications, classifying multimedia types, scoring urgency, and managing direct inline reply tokens."
                                else "Grant Notification Access in Android Settings to intercept real apps. You can also test with simulated incoming messages below.",
                                color = TextMuted,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )

                            if (!isServiceConnected) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(intent)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = VoidCyanPrimary, contentColor = VoidDeepNavy),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Enable Notification Access", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Section 2: Conversational Pronoun Follow-up Query Sandbox
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = VoidSurfaceCard),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(VoidCyanPrimary.copy(alpha = 0.4f), VoidVioletSecondary.copy(alpha = 0.4f))))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.QuestionAnswer, contentDescription = null, tint = VoidCyanPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("NATURAL PRONOUN FOLLOW-UP ENGINE", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Tap quick queries or type natural questions to test contextual pronoun and sender resolution:",
                                color = TextMuted,
                                fontSize = 11.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Quick sample chips
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                val querySamples = listOf(
                                    "Who messaged me?",
                                    "What did he say?",
                                    "Was it a voice note?",
                                    "Tell him I'll talk to you later."
                                )
                                querySamples.forEach { sample ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(VoidSurfaceCardElevated)
                                            .border(1.dp, VoidBorderGlow, RoundedCornerShape(6.dp))
                                            .clickable {
                                                naturalQueryInput = sample
                                                val res = contextEngine.processFollowUpQuery(sample)
                                                naturalQueryOutput = res.responseText
                                            }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = sample, color = VoidCyanPrimary, fontSize = 10.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Query Input Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = naturalQueryInput,
                                    onValueChange = { naturalQueryInput = it },
                                    placeholder = { Text("Ask about recent notifications...", color = TextMuted, fontSize = 11.sp) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("notification_query_input"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = VoidSurfaceCardElevated,
                                        unfocusedContainerColor = VoidSurfaceCardElevated,
                                        focusedBorderColor = VoidCyanPrimary,
                                        unfocusedBorderColor = VoidBorderGlow,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                Button(
                                    onClick = {
                                        if (naturalQueryInput.isNotBlank()) {
                                            val res = contextEngine.processFollowUpQuery(naturalQueryInput)
                                            naturalQueryOutput = res.responseText
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = VoidCyanPrimary, contentColor = VoidDeepNavy),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                    modifier = Modifier.testTag("notification_query_button")
                                ) {
                                    Text("Ask", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Query Output Banner
                            if (naturalQueryOutput != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(VoidSurfaceCardElevated)
                                        .border(1.dp, VoidCyanPrimary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = naturalQueryOutput ?: "",
                                        color = VoidCyanPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 3: Simulated Notification Injector (for rapid testing)
                item {
                    Text("INJECT SAMPLE TEST NOTIFICATIONS", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Button(
                            onClick = {
                                val now = System.currentTimeMillis()
                                contextEngine.recordNotification(
                                    NotificationModel(
                                        id = "sim_wa_${now}",
                                        packageName = "com.whatsapp",
                                        appName = "WhatsApp",
                                        sender = "Elena Rostova",
                                        title = "Elena Rostova",
                                        text = "Are the system diagnostics ready for review?",
                                        type = NotificationType.TEXT_MESSAGE,
                                        importance = ImportanceScore(80, ImportanceLevel.HIGH, listOf("Direct Personal Message")),
                                        timestamp = now,
                                        hasInlineReply = true,
                                        isSimulated = true
                                    )
                                )
                            },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VoidSurfaceCardElevated, contentColor = TextPrimary),
                            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(VoidBorderGlow, VoidBorderGlow))),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("+ WhatsApp Text", fontSize = 10.sp)
                        }

                        Button(
                            onClick = {
                                val now = System.currentTimeMillis()
                                contextEngine.recordNotification(
                                    NotificationModel(
                                        id = "sim_tg_${now}",
                                        packageName = "org.telegram.messenger",
                                        appName = "Telegram",
                                        sender = "Vikram Singh",
                                        title = "Vikram Singh",
                                        text = "🎤 Voice message (1:14)",
                                        type = NotificationType.VOICE_NOTE,
                                        importance = ImportanceScore(75, ImportanceLevel.HIGH, listOf("Audio Clip", "Direct Message")),
                                        timestamp = now,
                                        hasInlineReply = true,
                                        isSimulated = true
                                    )
                                )
                            },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VoidSurfaceCardElevated, contentColor = VoidCyanPrimary),
                            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(VoidCyanPrimary.copy(0.4f), VoidBorderGlow))),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("+ Telegram Voice Note", fontSize = 10.sp)
                        }

                        Button(
                            onClick = {
                                val now = System.currentTimeMillis()
                                contextEngine.recordNotification(
                                    NotificationModel(
                                        id = "sim_call_${now}",
                                        packageName = "com.google.android.dialer",
                                        appName = "Phone",
                                        sender = "Chief Engineer",
                                        title = "Missed Call",
                                        text = "Missed call from Chief Engineer (Urgent)",
                                        type = NotificationType.MISSED_CALL,
                                        importance = ImportanceScore(95, ImportanceLevel.CRITICAL, listOf("Missed Call", "Urgent Keyword")),
                                        timestamp = now,
                                        hasInlineReply = false,
                                        isSimulated = true
                                    )
                                )
                            },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VoidSurfaceCardElevated, contentColor = VoidError),
                            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(VoidError.copy(0.4f), VoidBorderGlow))),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("+ Missed Call", fontSize = 10.sp)
                        }
                    }
                }

                // Section 4: Recent Notifications Feed
                item {
                    Text("RECENT NOTIFICATIONS (${notifications.size})", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }

                items(notifications) { notif ->
                    NotificationCard(
                        notif = notif,
                        timeFormat = timeFormat,
                        replyText = replyTextMap[notif.id] ?: "",
                        sentStatus = sentStatusMap[notif.id],
                        onReplyTextChanged = { replyTextMap = replyTextMap + (notif.id to it) },
                        onSendReply = { text ->
                            val success = contextEngine.sendReply(notif.id, text)
                            sentStatusMap = sentStatusMap + (notif.id to if (success) "✓ Inline Reply Dispatched" else "Failed to send")
                        }
                    )
                }

                // Section 5: Voice Announcement Settings
                item {
                    Text("VOICE ANNOUNCEMENT & PRIVACY POLICY", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = VoidSurfaceCard),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(VoidBorderGlow, Color.Transparent)))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Toggle: Voice Announcements (Default OFF)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Voice Announcements", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("Speak high-urgency notifications through TTS (Default OFF)", color = TextMuted, fontSize = 10.sp)
                                }
                                Switch(
                                    checked = voiceAnnouncements,
                                    onCheckedChange = {
                                        voiceAnnouncements = it
                                        contextEngine.voiceAnnouncementsEnabled = it
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = VoidCyanPrimary,
                                        checkedTrackColor = VoidCyanPrimary.copy(alpha = 0.3f)
                                    )
                                )
                            }

                            // Toggle: Quiet Hours
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Quiet Hours (22:00 – 07:00)", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("Automatically suppress audible alerts during sleep cycles", color = TextMuted, fontSize = 10.sp)
                                }
                                Switch(
                                    checked = quietHours,
                                    onCheckedChange = {
                                        quietHours = it
                                        contextEngine.quietHoursEnabled = it
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = VoidCyanPrimary,
                                        checkedTrackColor = VoidCyanPrimary.copy(alpha = 0.3f)
                                    )
                                )
                            }

                            // Toggle: Lock Screen Privacy
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Lock Screen Privacy", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("Announce sender only on lock screen; mask message body", color = TextMuted, fontSize = 10.sp)
                                }
                                Switch(
                                    checked = lockScreenPrivacy,
                                    onCheckedChange = {
                                        lockScreenPrivacy = it
                                        contextEngine.lockScreenPrivacyEnabled = it
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = VoidCyanPrimary,
                                        checkedTrackColor = VoidCyanPrimary.copy(alpha = 0.3f)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notif: NotificationModel,
    timeFormat: SimpleDateFormat,
    replyText: String,
    sentStatus: String?,
    onReplyTextChanged: (String) -> Unit,
    onSendReply: (String) -> Unit
) {
    val (typeIcon, typeLabel, typeColor) = when (notif.type) {
        NotificationType.VOICE_NOTE -> Triple(Icons.Default.Mic, "VOICE NOTE", VoidCyanPrimary)
        NotificationType.IMAGE -> Triple(Icons.Default.Image, "IMAGE", VoidLavender)
        NotificationType.VIDEO -> Triple(Icons.Default.Videocam, "VIDEO", VoidVioletSecondary)
        NotificationType.DOCUMENT -> Triple(Icons.Default.Description, "DOCUMENT", VoidLavender)
        NotificationType.MISSED_CALL -> Triple(Icons.Default.CallMissed, "MISSED CALL", VoidError)
        NotificationType.GROUP_CHAT -> Triple(Icons.Default.Group, "GROUP", TextSecondary)
        NotificationType.TEXT_MESSAGE -> Triple(Icons.Default.Forum, "MESSAGE", VoidCyanPrimary)
        else -> Triple(Icons.Default.Notifications, "NOTIFICATION", TextMuted)
    }

    val (importanceBadgeColor, importanceText) = when (notif.importance.level) {
        ImportanceLevel.CRITICAL -> Pair(VoidError, "CRITICAL (${notif.importance.score})")
        ImportanceLevel.HIGH -> Pair(VoidWarning, "HIGH (${notif.importance.score})")
        ImportanceLevel.NORMAL -> Pair(VoidCyanPrimary, "NORMAL (${notif.importance.score})")
        ImportanceLevel.LOW -> Pair(Color.Gray, "LOW (${notif.importance.score})")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("notif_card_${notif.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = VoidSurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(typeColor.copy(alpha = 0.3f), VoidBorderGlow)
            )
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: App Name, Classification Pill, Importance Badge, Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = typeIcon, contentDescription = null, tint = typeColor, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(notif.appName, color = VoidLavender, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(typeColor.copy(alpha = 0.15f))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(typeLabel, color = typeColor, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(importanceBadgeColor.copy(alpha = 0.15f))
                            .border(1.dp, importanceBadgeColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(importanceText, color = importanceBadgeColor, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(timeFormat.format(Date(notif.timestamp)), color = TextMuted, fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Sender / Title
            Text(notif.sender, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))

            // Body
            Text(notif.text, color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)

            // Reason Pills
            if (notif.importance.reasons.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    notif.importance.reasons.forEach { r ->
                        Text(
                            text = "• $r",
                            color = TextMuted,
                            fontSize = 9.sp
                        )
                    }
                }
            }

            // Inline Reply Section
            if (notif.hasInlineReply) {
                Spacer(modifier = Modifier.height(10.dp))
                if (sentStatus != null) {
                    Text(sentStatus, color = VoidSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = replyText,
                            onValueChange = onReplyTextChanged,
                            placeholder = { Text("Direct reply...", color = TextMuted, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = VoidSurfaceCardElevated,
                                unfocusedContainerColor = VoidSurfaceCardElevated,
                                focusedBorderColor = VoidCyanPrimary,
                                unfocusedBorderColor = VoidBorderGlow,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                if (replyText.isNotBlank()) {
                                    onSendReply(replyText)
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(VoidCyanPrimary)
                        ) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = "Send Reply", tint = VoidDeepNavy, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
