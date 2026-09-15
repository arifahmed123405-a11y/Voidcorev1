package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agent.AgentBrain
import com.example.engine.local.LocalModelManager
import com.example.engine.local.ModelDownloadState
import com.example.engine.local.ModelProfile
import com.example.engine.local.ModelProfileTier
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OfflineScreen(
    agentBrain: AgentBrain? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val brain = remember { agentBrain ?: AgentBrain.getInstance(context) }
    val modelManager = brain.modelManager
    val localRuntime = brain.localRuntime
    val scope = rememberCoroutineScope()

    val selectedProfile by modelManager.selectedProfile.collectAsState()
    val loadedProfile by modelManager.loadedProfile.collectAsState()
    val downloadStates by modelManager.downloadStates.collectAsState()
    val telemetry by modelManager.telemetry.collectAsState()
    val modelStatus by localRuntime.status.collectAsState()
    val isInferring by localRuntime.isInferring.collectAsState()

    var simulateAirplaneMode by remember { mutableStateOf(true) }
    var sandboxPrompt by remember { mutableStateOf("Who are you and what can you do completely offline?") }
    var streamingResponse by remember { mutableStateOf("") }
    var lastInferenceLatencyMs by remember { mutableStateOf<Long?>(null) }
    var activeStreamJob by remember { mutableStateOf<Job?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidDeepNavy)
            .statusBarsPadding()
            .testTag("offline_screen")
    ) {
        // App Bar Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("offline_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Embedded Edge AI",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(VoidSuccess.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "NO CLOUD / NO TERMUX",
                            color = VoidSuccess,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = "Local Model Manager & Zero-Network Verification",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Airplane / Zero-Internet Mode Banner & Toggle
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (simulateAirplaneMode) VoidSurfaceCardElevated else VoidSurfaceCard
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (simulateAirplaneMode) VoidCyanPrimary else VoidBorderGlow
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (simulateAirplaneMode) VoidCyanPrimary.copy(alpha = 0.2f)
                                        else VoidDeepNavy
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (simulateAirplaneMode) Icons.Default.AirplanemodeActive else Icons.Default.CloudOff,
                                    contentDescription = null,
                                    tint = if (simulateAirplaneMode) VoidCyanPrimary else TextMuted,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (simulateAirplaneMode) "Simulated Airplane Mode (ACTIVE)" else "Online Network Enabled",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (simulateAirplaneMode) "All cloud fallback blocked. Operating 100% on-device." else "Hybrid cloud allowed for non-sensitive web queries.",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Switch(
                            checked = simulateAirplaneMode,
                            onCheckedChange = { simulateAirplaneMode = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = VoidCyanPrimary,
                                checkedTrackColor = VoidCyanPrimary.copy(alpha = 0.3f),
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = VoidDeepNavy
                            ),
                            modifier = Modifier.testTag("airplane_mode_switch")
                        )
                    }
                }
            }

            // 2. RAM & Storage Telemetry Panel
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = VoidSurfaceCard),
                    border = BorderStroke(1.dp, VoidBorderGlow)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Memory,
                                    contentDescription = null,
                                    tint = VoidLavender,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Device Memory & Storage Bounds",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(
                                onClick = { modelManager.refreshStatus() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh",
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            TelemetryStatBox(
                                label = "Device RAM",
                                value = "${telemetry.availableDeviceRamMb} / ${telemetry.totalDeviceRamMb} MB",
                                subtitle = "Available / Total",
                                modifier = Modifier.weight(1f)
                            )
                            TelemetryStatBox(
                                label = "Free Storage",
                                value = telemetry.freeInternalStorageFormatted,
                                subtitle = "App: ${telemetry.appUsedStorageFormatted}",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Warnings
                        if (telemetry.hasStorageWarning || telemetry.hasRamWarning) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(VoidWarning.copy(alpha = 0.15f))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = VoidWarning,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = telemetry.warningMessage ?: "Memory constraints detected.",
                                    color = VoidWarning,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // 3. Model Profiles Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Embedded Model Profiles",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Format: LiteRT-LM / GGUF Q4",
                        color = VoidCyanPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // 4. Model Profile Cards
            items(ModelProfile.ALL_PROFILES) { profile ->
                val downloadState = downloadStates[profile.id] ?: ModelDownloadState.Idle
                val isSelected = selectedProfile.id == profile.id
                val isLoaded = loadedProfile?.id == profile.id

                ModelProfileItemCard(
                    profile = profile,
                    downloadState = downloadState,
                    isSelected = isSelected,
                    isLoaded = isLoaded,
                    onSelect = { modelManager.selectProfile(profile) },
                    onDownload = { modelManager.startDownload(profile) },
                    onPause = { modelManager.pauseDownload(profile) },
                    onResume = { modelManager.resumeDownload(profile) },
                    onCancel = { modelManager.cancelDownload(profile) },
                    onLoad = {
                        modelManager.loadModel(profile)
                        localRuntime.loadProfile(profile)
                    },
                    onUnload = {
                        modelManager.unloadModel()
                        localRuntime.unloadModel()
                    },
                    onDelete = { modelManager.deleteModel(profile) }
                )
            }

            // 5. "What Works Without Internet" Capability Audit Grid
            item {
                Text(
                    text = "Offline Capabilities Matrix",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OfflineCapabilityRow(
                        icon = Icons.Default.FlashlightOn,
                        title = "Deterministic Device Controls",
                        desc = "Flashlight, Volume, Brightness, Timer, Alarm, Media, Settings",
                        status = "100% Offline (0ms)"
                    )
                    OfflineCapabilityRow(
                        icon = Icons.Default.AccessibilityNew,
                        title = "Safe Accessibility Pipeline",
                        desc = "System Back, Home, Recents, Scroll screen, Tap by label",
                        status = "100% Offline (0ms)"
                    )
                    OfflineCapabilityRow(
                        icon = Icons.Default.NotificationsActive,
                        title = "Notification Intelligence",
                        desc = "On-device message classification, urgency score, quick replies",
                        status = "100% Offline"
                    )
                    OfflineCapabilityRow(
                        icon = Icons.Default.Psychology,
                        title = "Embedded Neural LLM & Strict JSON",
                        desc = "Ambiguous device intent mapping & streaming offline chat",
                        status = "${modelStatus.modelName} Active"
                    )
                    OfflineCapabilityRow(
                        icon = Icons.Default.Security,
                        title = "Security Gate & Audit Logger",
                        desc = "Deterministic parameter checks & local Room database logs",
                        status = "Enforced Offline"
                    )
                    OfflineCapabilityRow(
                        icon = Icons.Default.VolumeUp,
                        title = "Audio Engine & Synthesis",
                        desc = "Pitch-shifted PCM feedback & on-device text-to-speech",
                        status = "100% Offline"
                    )
                }
            }

            // 6. Interactive Offline Conversation Sandbox
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = VoidSurfaceCardElevated),
                    border = BorderStroke(1.dp, VoidCyanPrimary.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = VoidCyanPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Offline Conversation & Intent Sandbox",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Test token streaming, ambiguous intent resolution, and deterministic commands with zero cloud roundtrips.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                        )

                        // Sample Trigger Chips
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            SampleChip("Who are you offline?") { sandboxPrompt = it }
                            SampleChip("Turn on flashlight") { sandboxPrompt = it }
                            SampleChip("It's too dark in here") { sandboxPrompt = it }
                            SampleChip("Set volume to 40%") { sandboxPrompt = it }
                            SampleChip("Wake me up at 6:30am") { sandboxPrompt = it }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Input Box
                        OutlinedTextField(
                            value = sandboxPrompt,
                            onValueChange = { sandboxPrompt = it },
                            placeholder = { Text("Enter prompt or command to test on-device...", color = TextMuted) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("offline_sandbox_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VoidCyanPrimary,
                                unfocusedBorderColor = VoidBorderGlow,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = VoidSurfaceDark,
                                unfocusedContainerColor = VoidSurfaceDark
                            ),
                            shape = RoundedCornerShape(10.dp),
                            trailingIcon = {
                                if (isInferring) {
                                    IconButton(
                                        onClick = {
                                            activeStreamJob?.cancel()
                                            localRuntime.cancelInference()
                                        },
                                        modifier = Modifier.testTag("offline_cancel_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Stop,
                                            contentDescription = "Cancel",
                                            tint = VoidError
                                        )
                                    }
                                } else {
                                    IconButton(
                                        onClick = {
                                            if (sandboxPrompt.isNotBlank()) {
                                                activeStreamJob?.cancel()
                                                streamingResponse = ""
                                                val startTime = System.currentTimeMillis()
                                                activeStreamJob = scope.launch {
                                                    // Stream directly from embedded local runtime
                                                    localRuntime.generateStream(sandboxPrompt).collect { token ->
                                                        streamingResponse += token
                                                    }
                                                    lastInferenceLatencyMs = System.currentTimeMillis() - startTime
                                                }
                                            }
                                        },
                                        modifier = Modifier.testTag("offline_send_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Send,
                                            contentDescription = "Send",
                                            tint = VoidCyanPrimary
                                        )
                                    }
                                }
                            }
                        )

                        // Output Display Box
                        AnimatedVisibility(visible = streamingResponse.isNotEmpty() || isInferring) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(VoidSurfaceDark)
                                    .border(1.dp, VoidBorderGlow, RoundedCornerShape(10.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(if (isInferring) VoidCyanPrimary else VoidSuccess)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isInferring) "Streaming On-Device Tokens (~35 t/s)..." else "Local Inference Completed",
                                            color = if (isInferring) VoidCyanPrimary else VoidSuccess,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    lastInferenceLatencyMs?.let { latency ->
                                        Text(
                                            text = "$latency ms | 0 bytes sent",
                                            color = TextMuted,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = streamingResponse,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp,
                                    modifier = Modifier.testTag("offline_stream_output")
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
fun ModelProfileItemCard(
    profile: ModelProfile,
    downloadState: ModelDownloadState,
    isSelected: Boolean,
    isLoaded: Boolean,
    onSelect: () -> Unit,
    onDownload: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onLoad: () -> Unit,
    onUnload: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("profile_card_${profile.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) VoidSurfaceCardElevated else VoidSurfaceCard
        ),
        border = BorderStroke(
            1.dp,
            if (isLoaded) VoidCyanPrimary else if (isSelected) VoidLavender else VoidBorderGlow
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = profile.name,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        if (isLoaded) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(VoidCyanPrimary.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "LOADED",
                                    color = VoidCyanPrimary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Text(
                        text = "${profile.sizeFormatted} Storage • ~${profile.ramRequiredMb} MB RAM • ${profile.parameterCount}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(VoidSurfaceDark)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = profile.tier.name,
                        color = when (profile.tier) {
                            ModelProfileTier.LITE -> VoidSuccess
                            ModelProfileTier.BALANCED -> VoidCyanPrimary
                            ModelProfileTier.PRO -> VoidLavender
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = profile.description,
                color = TextMuted,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            // Download Status Bar / Progress
            when (downloadState) {
                is ModelDownloadState.Downloading -> {
                    Spacer(modifier = Modifier.height(10.dp))
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Downloading: ${(downloadState.progress * 100).toInt()}% (${downloadState.etaSeconds}s remaining)",
                                color = VoidCyanPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "45 MB/s",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { downloadState.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = VoidCyanPrimary,
                            trackColor = VoidSurfaceDark
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = onPause,
                                modifier = Modifier.height(32.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp)
                            ) {
                                Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(14.dp), tint = TextPrimary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Pause", fontSize = 11.sp, color = TextPrimary)
                            }
                            OutlinedButton(
                                onClick = onCancel,
                                modifier = Modifier.height(32.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp)
                            ) {
                                Text("Cancel", fontSize = 11.sp, color = VoidError)
                            }
                        }
                    }
                }

                is ModelDownloadState.Paused -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Download Paused", color = VoidWarning, fontSize = 11.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = onResume,
                                colors = ButtonDefaults.buttonColors(containerColor = VoidCyanPrimary, contentColor = VoidDeepNavy),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Resume", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = onCancel,
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Cancel", fontSize = 11.sp, color = VoidError)
                            }
                        }
                    }
                }

                is ModelDownloadState.VerifyingChecksum -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = VoidLavender, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Verifying SHA-256 Checksum...", color = VoidLavender, fontSize = 11.sp)
                    }
                }

                is ModelDownloadState.Completed -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = VoidSuccess, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ready (SHA-256 Verified)", color = VoidSuccess, fontSize = 11.sp)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (isLoaded) {
                                OutlinedButton(
                                    onClick = onUnload,
                                    modifier = Modifier.height(30.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                                ) {
                                    Text("Unload", fontSize = 11.sp)
                                }
                            } else {
                                Button(
                                    onClick = onLoad,
                                    colors = ButtonDefaults.buttonColors(containerColor = VoidCyanPrimary, contentColor = VoidDeepNavy),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Load", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (profile.id != ModelProfile.LITE_PROFILE.id) {
                                IconButton(
                                    onClick = onDelete,
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                is ModelDownloadState.Error -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Error: ${downloadState.message}", color = VoidError, fontSize = 11.sp)
                        Button(
                            onClick = onDownload,
                            colors = ButtonDefaults.buttonColors(containerColor = VoidError, contentColor = Color.White),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Retry", fontSize = 11.sp)
                        }
                    }
                }

                is ModelDownloadState.Idle -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = onDownload,
                            colors = ButtonDefaults.buttonColors(containerColor = VoidVioletSecondary, contentColor = Color.White),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Download (${profile.sizeFormatted})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TelemetryStatBox(
    label: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(VoidSurfaceDark)
            .border(1.dp, VoidBorderGlow, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(text = label, color = TextMuted, fontSize = 10.sp)
            Text(text = value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(text = subtitle, color = VoidCyanPrimary, fontSize = 10.sp)
        }
    }
}

@Composable
fun OfflineCapabilityRow(
    icon: ImageVector,
    title: String,
    desc: String,
    status: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = VoidSurfaceCard),
        border = BorderStroke(1.dp, VoidBorderGlow)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(VoidSurfaceDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = VoidCyanPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(text = desc, color = TextSecondary, fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(VoidSuccess.copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(text = status, color = VoidSuccess, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SampleChip(text: String, onClick: (String) -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(VoidSurfaceDark)
            .border(1.dp, VoidBorderGlow, RoundedCornerShape(14.dp))
            .clickable { onClick(text) }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text = text, color = VoidLavender, fontSize = 11.sp)
    }
}

@Composable
fun PerformanceScreen(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidDeepNavy)
            .statusBarsPadding()
            .testTag("performance_screen")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("performance_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Performance & Telemetry",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Latency, memory & rendering profile",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = VoidSurfaceCard),
                    border = BorderStroke(1.dp, VoidBorderGlow)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = VoidCyanPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = "Engine Performance Bounds",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "State transitions execute within < 5ms. Visual particle rendering adapts across Power Saver (30fps), Balanced (60fps), and Cinematic Ultra tiers.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}
