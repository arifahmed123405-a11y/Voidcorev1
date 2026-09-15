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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.renderer.RendererQuality
import com.example.core.renderer.VoidCorePresenceView
import com.example.core.state.AssistantState
import com.example.core.state.AssistantStateMachine
import com.example.core.state.PresenceForm
import com.example.engine.voice.AudioCue
import com.example.engine.voice.AudioIdentityEngine
import com.example.engine.voice.VoiceEngine
import com.example.feature.briefing.BriefingBlock
import com.example.feature.briefing.BriefingDataBuilder
import com.example.feature.briefing.BriefingPreferences
import com.example.feature.briefing.MorningBriefingManager
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MorningBriefingCallScreen(
    stateMachine: AssistantStateMachine,
    voiceEngine: VoiceEngine,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val audioEngine = remember { AudioIdentityEngine.getInstance(context) }
    val briefingManager = remember { MorningBriefingManager(context) }
    val dataBuilder = remember { BriefingDataBuilder(context) }

    var callState by remember { mutableStateOf("INCOMING") } // INCOMING, ACTIVE_BRIEFING, CONFIGURATION
    var briefingPrefs by remember { mutableStateOf(BriefingPreferences()) }
    var currentBlockIndex by remember { mutableIntStateOf(0) }
    var isPaused by remember { mutableStateOf(false) }

    val currentState by stateMachine.currentState.collectAsState()
    val audioEnergy by stateMachine.audioEnergyLevel.collectAsState()

    val blocks = remember(briefingPrefs) { dataBuilder.buildBriefing(briefingPrefs) }

    fun speakNextBlock() {
        if (currentBlockIndex < blocks.size && !isPaused) {
            val block = blocks[currentBlockIndex]
            stateMachine.transitionTo(AssistantState.SPEAKING)
            voiceEngine.speak(block.text) {
                if (currentBlockIndex + 1 < blocks.size) {
                    currentBlockIndex++
                    speakNextBlock()
                } else {
                    stateMachine.transitionTo(AssistantState.SUCCESS)
                }
            }
        }
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Top navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        voiceEngine.cancelSpeech()
                        onBack()
                    },
                    modifier = Modifier.testTag("briefing_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = TextSecondary
                    )
                }

                Text(
                    text = "MORNING BRIEFING",
                    color = VoidCyanPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                IconButton(
                    onClick = {
                        callState = if (callState == "CONFIGURATION") "INCOMING" else "CONFIGURATION"
                    },
                    modifier = Modifier.testTag("briefing_settings_toggle")
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Schedule Settings",
                        tint = if (callState == "CONFIGURATION") VoidCyanPrimary else TextMuted
                    )
                }
            }

            if (callState == "INCOMING") {
                // Incoming Call Surface
                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = "VoidCore is Calling",
                    color = TextPrimary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "6:00 AM Wake Briefing Ready",
                    color = VoidCyanPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Orb in INVOKING / Call pulse
                VoidCorePresenceView(
                    state = AssistantState.INVOKING,
                    form = PresenceForm.FULL_PRESENCE,
                    audioEnergy = 0.6f,
                    quality = RendererQuality.CINEMATIC
                )

                Spacer(modifier = Modifier.weight(1f))

                // Answer / Snooze / Dismiss Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Snooze Button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = {
                                audioEngine.playCue(AudioCue.DISMISSAL)
                                briefingManager.scheduleDailyBriefing(6, 15) // Snooze 15 mins
                                onBack()
                            },
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(VoidSurfaceCardElevated)
                                .border(1.dp, VoidBorderGlow, CircleShape)
                                .testTag("briefing_snooze_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Snooze,
                                contentDescription = "Snooze",
                                tint = VoidWarning,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Snooze (15m)", color = TextSecondary, fontSize = 12.sp)
                    }

                    // Answer Button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = {
                                audioEngine.playCue(AudioCue.WAKE)
                                callState = "ACTIVE_BRIEFING"
                                currentBlockIndex = 0
                                scope.launch {
                                    stateMachine.transitionTo(AssistantState.INVOKING)
                                    delay(400)
                                    speakNextBlock()
                                }
                            },
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(VoidSuccess)
                                .testTag("briefing_answer_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Answer Briefing",
                                tint = VoidDeepNavy,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Answer", color = VoidSuccess, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    // Dismiss Button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = {
                                audioEngine.playCue(AudioCue.DISMISSAL)
                                onBack()
                            },
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(VoidSurfaceCardElevated)
                                .border(1.dp, VoidBorderGlow, CircleShape)
                                .testTag("briefing_dismiss_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallEnd,
                                contentDescription = "Dismiss",
                                tint = VoidError,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Dismiss", color = TextSecondary, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

            } else if (callState == "ACTIVE_BRIEFING") {
                // Active Spoken Walkthrough Surface
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    VoidCorePresenceView(
                        state = currentState,
                        form = PresenceForm.COMPACT,
                        audioEnergy = audioEnergy,
                        quality = RendererQuality.CINEMATIC
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Live Morning Briefing",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Block ${currentBlockIndex + 1} of ${blocks.size}",
                    color = VoidCyanPrimary,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Progress Indicator
                LinearProgressIndicator(
                    progress = { ((currentBlockIndex + 1).toFloat() / blocks.size).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = VoidCyanPrimary,
                    trackColor = VoidSurfaceCardElevated,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Spoken Block Card
                if (currentBlockIndex < blocks.size) {
                    val activeBlock = blocks[currentBlockIndex]
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = VoidSurfaceCardElevated),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(VoidCyanPrimary.copy(0.5f), VoidVioletSecondary.copy(0.5f))))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = activeBlock.title.uppercase(),
                                color = VoidCyanPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = activeBlock.text,
                                color = TextPrimary,
                                fontSize = 16.sp,
                                lineHeight = 24.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Briefing Controls (Pause / Skip / Stop)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            voiceEngine.cancelSpeech()
                            if (currentBlockIndex + 1 < blocks.size) {
                                currentBlockIndex++
                                speakNextBlock()
                            }
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(VoidSurfaceCard)
                            .testTag("briefing_skip_block_button")
                    ) {
                        Text("Skip", color = VoidCyanPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    IconButton(
                        onClick = {
                            if (isPaused) {
                                isPaused = false
                                speakNextBlock()
                            } else {
                                isPaused = true
                                voiceEngine.cancelSpeech()
                            }
                        },
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(VoidCyanPrimary)
                            .testTag("briefing_pause_resume_button")
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = "Pause",
                            tint = VoidDeepNavy,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            voiceEngine.cancelSpeech()
                            callState = "INCOMING"
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(VoidSurfaceCard)
                            .testTag("briefing_stop_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop",
                            tint = VoidError
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

            } else {
                // Configuration Surface
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Configure Daily Briefing",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Scheduled daily at 6:00 AM with AlarmManager",
                    color = TextMuted,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        BriefingToggleRow("Include Greeting & Date", briefingPrefs.includeGreeting) {
                            briefingPrefs = briefingPrefs.copy(includeGreeting = it)
                        }
                    }
                    item {
                        BriefingToggleRow("Weather & Severe Alerts", briefingPrefs.includeWeather) {
                            briefingPrefs = briefingPrefs.copy(includeWeather = it)
                        }
                    }
                    item {
                        BriefingToggleRow("Calendar & First Appointment", briefingPrefs.includeCalendar) {
                            briefingPrefs = briefingPrefs.copy(includeCalendar = it)
                        }
                    }
                    item {
                        BriefingToggleRow("Tasks & Reminders Due", briefingPrefs.includeTasks) {
                            briefingPrefs = briefingPrefs.copy(includeTasks = it)
                        }
                    }
                    item {
                        BriefingToggleRow("Overnight Priority Messages", briefingPrefs.includeNotifications) {
                            briefingPrefs = briefingPrefs.copy(includeNotifications = it)
                        }
                    }
                    item {
                        BriefingToggleRow("Battery Status & Device Health", briefingPrefs.includeBattery) {
                            briefingPrefs = briefingPrefs.copy(includeBattery = it)
                        }
                    }
                    item {
                        BriefingToggleRow("Selected News Topics", briefingPrefs.includeNews) {
                            briefingPrefs = briefingPrefs.copy(includeNews = it)
                        }
                    }
                    item {
                        BriefingToggleRow("Focus Attention Summary", briefingPrefs.includePrioritySummary) {
                            briefingPrefs = briefingPrefs.copy(includePrioritySummary = it)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(VoidCyanPrimary)
                        .clickable {
                            briefingManager.scheduleDailyBriefing(6, 0)
                            callState = "INCOMING"
                        }
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Save & Arm 6:00 AM Alarm", color = VoidDeepNavy, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun BriefingToggleRow(
    label: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(VoidSurfaceCard)
            .border(1.dp, VoidBorderGlow, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = VoidCyanPrimary,
                checkedTrackColor = VoidSurfaceCardElevated,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = VoidSurfaceDark
            )
        )
    }
}
