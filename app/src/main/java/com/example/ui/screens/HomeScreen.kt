package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agent.AgentBrain
import com.example.core.renderer.RendererQuality
import com.example.core.renderer.VoidCorePresenceView
import com.example.core.state.AssistantState
import com.example.core.state.AssistantStateMachine
import com.example.engine.voice.SpeechRecognitionManager
import com.example.engine.voice.VoiceEngine
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidBorderGlow
import com.example.ui.theme.VoidCyanPrimary
import com.example.ui.theme.VoidDeepNavy
import com.example.ui.theme.VoidError
import com.example.ui.theme.VoidLavender
import com.example.ui.theme.VoidSurfaceCard
import com.example.ui.theme.VoidSurfaceCardElevated
import com.example.ui.theme.VoidSurfaceDark
import com.example.ui.theme.VoidVioletSecondary
import com.example.ui.theme.VoidWarning

@Composable
fun HomeScreen(
    stateMachine: AssistantStateMachine,
    agentBrain: AgentBrain,
    voiceEngine: VoiceEngine,
    speechRecognizer: SpeechRecognitionManager,
    onNavigate: (String) -> Unit
) {
    val currentState by stateMachine.currentState.collectAsState()
    val currentForm by stateMachine.currentForm.collectAsState()
    val activeTranscript by stateMachine.activeTranscript.collectAsState()
    val currentActionStatus by stateMachine.currentActionStatus.collectAsState()
    val pendingConfirmation by stateMachine.pendingConfirmation.collectAsState()
    val audioEnergy by stateMachine.audioEnergyLevel.collectAsState()
    val isListening by speechRecognizer.isListening.collectAsState()

    var textInput by remember { mutableStateOf("") }

    val quickActionChips = listOf(
        "Turn on flashlight" to "Flashlight",
        "Set volume to 75%" to "Volume",
        "Set alarm for 6:00 AM" to "Alarm",
        "Search files for notes" to "Files",
        "Read Ahmed's message" to "Message",
        "Start Morning Briefing" to "Briefing"
    )

    // Confirmation Dialog for High-Risk Actions
    pendingConfirmation?.let { conf ->
        AlertDialog(
            onDismissRequest = { conf.onDeny() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = VoidWarning,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = conf.title,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(VoidError.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = conf.riskBadge,
                            color = VoidError,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = conf.description,
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { conf.onConfirm() },
                    colors = ButtonDefaults.buttonColors(containerColor = VoidCyanPrimary, contentColor = VoidDeepNavy),
                    modifier = Modifier.testTag("confirm_action_button")
                ) {
                    Text("Authorize & Run", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { conf.onDeny() },
                    modifier = Modifier.testTag("cancel_action_button")
                ) {
                    Text("Deny", color = TextMuted)
                }
            },
            containerColor = VoidSurfaceCardElevated,
            shape = RoundedCornerShape(18.dp)
        )
    }

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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            // Header bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "V O I D C O R E",
                        color = VoidCyanPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp
                    )
                    Text(
                        text = "Living On-Device AI Presence",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                Row {
                    IconButton(
                        onClick = { onNavigate("briefing_call") },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(VoidSurfaceCard)
                            .testTag("morning_briefing_header_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = "Morning Briefing",
                            tint = VoidCyanPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { onNavigate("visual_lab") },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(VoidSurfaceCard)
                            .testTag("visual_lab_header_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Visual Lab",
                            tint = VoidCyanPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { onNavigate("voice_lab") },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(VoidSurfaceCard)
                            .testTag("voice_lab_header_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Voice Lab",
                            tint = VoidLavender,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // State Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(VoidSurfaceCardElevated.copy(alpha = 0.8f))
                    .border(1.dp, VoidBorderGlow, RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                when (currentState) {
                                    AssistantState.SLEEPING -> VoidLavender
                                    AssistantState.LISTENING -> VoidCyanPrimary
                                    AssistantState.THINKING, AssistantState.PLANNING -> VoidVioletSecondary
                                    AssistantState.ACTING -> VoidCyanPrimary
                                    AssistantState.ERROR_RECOVERY -> VoidError
                                    AssistantState.SUCCESS -> VoidCyanPrimary
                                    else -> VoidCyanPrimary
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = currentState.displayName.uppercase(),
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hero Void Core Presence (Glass sphere wrapped in plasma dust and orbitals)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                contentAlignment = Alignment.Center
            ) {
                VoidCorePresenceView(
                    state = currentState,
                    form = currentForm,
                    audioEnergy = audioEnergy,
                    quality = RendererQuality.CINEMATIC,
                    onClick = {
                        if (isListening) {
                            speechRecognizer.stopListening()
                        } else {
                            stateMachine.invokeAndListen()
                            speechRecognizer.startListening()
                        }
                    },
                    modifier = Modifier.testTag("void_core_presence_orb")
                )
            }

            // Live Transcript or Action Status
            AnimatedVisibility(
                visible = activeTranscript.isNotBlank() || currentActionStatus.isNotBlank(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = VoidSurfaceCardElevated.copy(alpha = 0.9f)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(VoidCyanPrimary.copy(0.4f), VoidVioletSecondary.copy(0.4f))))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        if (activeTranscript.isNotBlank()) {
                            Text(
                                text = "Transcript: \"$activeTranscript\"",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        if (currentActionStatus.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentActionStatus,
                                color = VoidCyanPrimary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Quick Tool Shortcuts (Row of fast actions)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickFeatureButton(
                    icon = Icons.Default.Visibility,
                    label = "Vision",
                    tint = VoidCyanPrimary,
                    onClick = { onNavigate("vision") }
                )
                QuickFeatureButton(
                    icon = Icons.Default.AutoAwesome,
                    label = "Automate",
                    tint = VoidLavender,
                    onClick = { onNavigate("automations_tab") }
                )
                QuickFeatureButton(
                    icon = Icons.Default.Search,
                    label = "Search",
                    tint = VoidCyanPrimary,
                    onClick = { onNavigate("universal_search") }
                )
                QuickFeatureButton(
                    icon = Icons.Default.Call,
                    label = "Screen Call",
                    tint = VoidLavender,
                    onClick = { onNavigate("call_intelligence") }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Suggestion Chips
            Text(
                text = "SUGGESTED ACTIONS",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(quickActionChips) { chip ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(VoidSurfaceCard)
                            .border(1.dp, VoidBorderGlow, RoundedCornerShape(12.dp))
                            .clickable {
                                textInput = chip.first
                                agentBrain.handleUserInput(chip.first)
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag("suggestion_chip_${chip.second.lowercase()}")
                    ) {
                        Text(
                            text = chip.first,
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Input Prompt Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(VoidSurfaceCard)
                    .border(1.dp, VoidBorderGlow, RoundedCornerShape(24.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        agentBrain.handleMicTap()
                    },
                    modifier = Modifier.testTag("home_voice_mic_button")
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = if (isListening) VoidError else VoidCyanPrimary
                    )
                }

                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("Command VoidCore or ask anything...", color = TextMuted, fontSize = 13.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("home_prompt_input_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    maxLines = 2
                )

                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            val prompt = textInput
                            textInput = ""
                            agentBrain.handleUserInput(prompt)
                        }
                    },
                    enabled = textInput.isNotBlank(),
                    modifier = Modifier.testTag("home_send_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Execute Command",
                        tint = if (textInput.isNotBlank()) VoidCyanPrimary else TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(90.dp)) // Space for bottom navigation bar
        }
    }
}

@Composable
fun QuickFeatureButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
            .testTag("quick_action_${label.lowercase()}")
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(VoidSurfaceCardElevated)
                .border(1.dp, VoidBorderGlow, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
