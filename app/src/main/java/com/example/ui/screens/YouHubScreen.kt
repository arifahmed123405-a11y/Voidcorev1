package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.state.AssistantStateMachine
import com.example.engine.voice.VoiceEngine
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidBorderGlow
import com.example.ui.theme.VoidCyanPrimary
import com.example.ui.theme.VoidDeepNavy
import com.example.ui.theme.VoidLavender
import com.example.ui.theme.VoidSurfaceCard
import com.example.ui.theme.VoidSurfaceCardElevated
import com.example.ui.theme.VoidSurfaceDark
import com.example.ui.theme.VoidVioletSecondary

@Composable
fun YouHubScreen(
    stateMachine: AssistantStateMachine,
    voiceEngine: VoiceEngine,
    onNavigate: (String) -> Unit
) {
    val voiceParams by voiceEngine.voiceParams.collectAsState()
    val currentState by stateMachine.currentState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(VoidDeepNavy, VoidSurfaceDark, VoidDeepNavy)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            // Header
            Column {
                Text(
                    text = "Y O U  &  S Y S T E M",
                    color = VoidCyanPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp
                )
                Text(
                    text = "Voice lab, local intelligence, privacy & diagnostics",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Active Voice Summary Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigate("voice_lab") }
                            .testTag("hub_voice_summary_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = VoidSurfaceCardElevated),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(
                                listOf(VoidCyanPrimary.copy(alpha = 0.5f), VoidVioletSecondary.copy(alpha = 0.5f))
                            )
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(VoidCyanPrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Hearing,
                                        contentDescription = null,
                                        tint = VoidCyanPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = "Voice: ${voiceParams.preset.displayName}",
                                        color = TextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${voiceParams.preset.description} • Tap to tune",
                                        color = VoidLavender,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = TextMuted
                            )
                        }
                    }
                }

                item {
                    HubSectionHeader("VISUAL & VOICE PRESENCE")
                }

                item {
                    HubMenuTile(
                        icon = Icons.Default.AutoAwesome,
                        title = "Visual Presence Lab",
                        subtitle = "11-state visual choreography, morphing forms, phone simulator & GL backend",
                        tint = VoidCyanPrimary,
                        onClick = { onNavigate("visual_lab") }
                    )
                }

                item {
                    HubMenuTile(
                        icon = Icons.Default.Tune,
                        title = "Voice Identity Lab",
                        subtitle = "6 voice presets, pitch, speed, metallic resonance & timbre",
                        tint = VoidLavender,
                        onClick = { onNavigate("voice_lab") }
                    )
                }

                item {
                    HubMenuTile(
                        icon = Icons.Default.VolumeUp,
                        title = "Audio Identity & Sound Cues",
                        subtitle = "Synthesized tones for wake, listening, acting, error & haptics",
                        tint = VoidLavender,
                        onClick = { onNavigate("audio_lab") }
                    )
                }

                item {
                    HubSectionHeader("INTELLIGENCE & REASONING")
                }

                item {
                    HubMenuTile(
                        icon = Icons.Default.Memory,
                        title = "AI Providers & Local Model",
                        subtitle = "On-device MicroLLM-8B (620 MB RAM) & Gemini API fallback",
                        tint = VoidCyanPrimary,
                        onClick = { onNavigate("ai_providers") }
                    )
                }

                item {
                    HubMenuTile(
                        icon = Icons.Default.Psychology,
                        title = "Local Memory Vault",
                        subtitle = "Durable facts, preferences & learned user routines",
                        tint = VoidLavender,
                        onClick = { onNavigate("memory_vault") }
                    )
                }

                item {
                    HubSectionHeader("WORKSPACE & AUTOMATION")
                }

                item {
                    HubMenuTile(
                        icon = Icons.Default.FolderOpen,
                        title = "Agent Workspace",
                        subtitle = "Multi-step plan execution & tool checkpoints",
                        tint = VoidCyanPrimary,
                        onClick = { onNavigate("workspace") }
                    )
                }

                item {
                    HubMenuTile(
                        icon = Icons.Default.FolderOpen,
                        title = "Workspaces & Projects",
                        subtitle = "Autonomous goals, notes and ongoing tasks",
                        tint = VoidLavender,
                        onClick = { onNavigate("projects") }
                    )
                }

                item {
                    HubMenuTile(
                        icon = Icons.Default.Tune,
                        title = "Connections & Integrations",
                        subtitle = "External service hooks & device connectors",
                        tint = VoidCyanPrimary,
                        onClick = { onNavigate("connections") }
                    )
                }

                item {
                    HubSectionHeader("PERCEPTIONS & TELEMETRY")
                }

                item {
                    HubMenuTile(
                        icon = Icons.Default.Visibility,
                        title = "Vision & OCR Scanner",
                        subtitle = "Live camera text recognition & stability gating",
                        tint = VoidCyanPrimary,
                        onClick = { onNavigate("vision") }
                    )
                }

                item {
                    HubMenuTile(
                        icon = Icons.Default.Notifications,
                        title = "Notification Intelligence",
                        subtitle = "Categorized message cache & inline remote replies",
                        tint = VoidLavender,
                        onClick = { onNavigate("notifications_hub") }
                    )
                }

                item {
                    HubMenuTile(
                        icon = Icons.Default.Call,
                        title = "Call Screening Assistant",
                        subtitle = "Assistant disclosure, live relay & 30m callback reminders",
                        tint = VoidCyanPrimary,
                        onClick = { onNavigate("call_intelligence") }
                    )
                }

                item {
                    HubSectionHeader("SECURITY & DIAGNOSTICS")
                }

                item {
                    HubMenuTile(
                        icon = Icons.Default.Security,
                        title = "Trust & Safety Architecture",
                        subtitle = "Gated execution & deny-by-default policy",
                        tint = VoidCyanPrimary,
                        onClick = { onNavigate("trust_safety") }
                    )
                }

                item {
                    HubMenuTile(
                        icon = Icons.Default.Security,
                        title = "Privacy & Permission Matrix",
                        subtitle = "Exact OS permission status, reasons & system settings links",
                        tint = VoidLavender,
                        onClick = { onNavigate("privacy_dashboard") }
                    )
                }

                item {
                    HubMenuTile(
                        icon = Icons.Default.Tune,
                        title = "Offline & Edge Engine",
                        subtitle = "Zero-network autonomous capabilities",
                        tint = VoidCyanPrimary,
                        onClick = { onNavigate("offline") }
                    )
                }

                item {
                    HubMenuTile(
                        icon = Icons.Default.Speed,
                        title = "Performance & Telemetry",
                        subtitle = "Latency, memory & rendering profile",
                        tint = VoidLavender,
                        onClick = { onNavigate("performance") }
                    )
                }

                item {
                    HubMenuTile(
                        icon = Icons.Default.Speed,
                        title = "Diagnostics & Capability Matrix",
                        subtitle = "IMPLEMENTED / LIMITED / NOT AVAILABLE runtime matrix",
                        tint = VoidCyanPrimary,
                        onClick = { onNavigate("diagnostics") }
                    )
                }
            }
        }
    }
}

@Composable
fun HubSectionHeader(title: String) {
    Text(
        text = title,
        color = TextMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
    )
}

@Composable
fun HubMenuTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("hub_tile_${title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = VoidSurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(VoidBorderGlow, VoidBorderGlow)))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(VoidSurfaceCardElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted
            )
        }
    }
}
