package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.state.AssistantState
import com.example.core.state.AssistantStateMachine
import com.example.engine.voice.SpeechRecognitionManager
import com.example.engine.voice.VoiceEngine
import com.example.engine.voice.VoicePreset
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VoiceLabScreen(
    voiceEngine: VoiceEngine,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val speechRecognizer = remember { SpeechRecognitionManager.getInstance(context) }
    val stateMachine = remember { AssistantStateMachine.getInstance(context) }

    val voiceParams by voiceEngine.voiceParams.collectAsState()
    val isSpeaking by voiceEngine.isSpeaking.collectAsState()
    val availableVoices by voiceEngine.availableVoices.collectAsState()
    val isListening by speechRecognizer.isListening.collectAsState()
    val liveTranscript by speechRecognizer.liveTranscript.collectAsState()

    var selectedVoice by remember { mutableStateOf(availableVoices.firstOrNull() ?: "Default Voice") }

    val presets = VoicePreset.values()

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
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            // Screen Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("voice_lab_back_button")) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = TextSecondary)
                }
                Text(
                    text = "VOICE & SPEECH LAB",
                    color = VoidCyanPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Section 1: Interactive Mic & TTS Interruption Test Pad
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = VoidSurfaceCardElevated),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(
                                listOf(VoidCyanPrimary.copy(alpha = 0.5f), VoidVioletSecondary.copy(alpha = 0.5f))
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
                                        imageVector = Icons.Default.GraphicEq,
                                        contentDescription = null,
                                        tint = VoidCyanPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "MIC & INTERRUPTION TEST PAD",
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isSpeaking) VoidCyanPrimary.copy(alpha = 0.2f) else if (isListening) VoidSuccess.copy(alpha = 0.2f) else Color.DarkGray.copy(alpha = 0.3f))
                                        .border(1.dp, if (isSpeaking) VoidCyanPrimary else if (isListening) VoidSuccess else Color.Gray, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isSpeaking) "TTS SPEAKING" else if (isListening) "MIC LISTENING" else "IDLE",
                                        color = if (isSpeaking) VoidCyanPrimary else if (isListening) VoidSuccess else TextMuted,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = if (liveTranscript.isNotBlank()) "Live Transcript: \"$liveTranscript\""
                                else if (isSpeaking) "Speaking synthesis output with dynamic vocal envelope."
                                else "Tap Mic to record or test barge-in interruption while TTS speaks.",
                                color = if (liveTranscript.isNotBlank()) VoidCyanPrimary else TextMuted,
                                fontSize = 11.sp,
                                maxLines = 2
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Action buttons: Speak Test vs Mic Tap Interruption
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (isSpeaking) {
                                            voiceEngine.cancelSpeech()
                                        } else {
                                            voiceEngine.speak("VoidCore neural audio engine active. Tap the microphone button while I am speaking to test zero-latency speech interruption.")
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSpeaking) VoidError else VoidSurfaceCard,
                                        contentColor = if (isSpeaking) Color.White else VoidCyanPrimary
                                    ),
                                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(VoidCyanPrimary, VoidVioletSecondary)))
                                ) {
                                    Icon(
                                        imageVector = if (isSpeaking) Icons.Default.Stop else Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isSpeaking) "Stop TTS" else "Speak Test",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Button(
                                    onClick = {
                                        if (isSpeaking) {
                                            // Real interruption: Halts speech & immediately opens mic
                                            voiceEngine.interruptAndListen(speechRecognizer)
                                        } else if (isListening) {
                                            speechRecognizer.stopListening()
                                            stateMachine.transitionTo(AssistantState.SLEEPING)
                                        } else {
                                            speechRecognizer.startListening()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isListening) VoidSuccess else VoidCyanPrimary,
                                        contentColor = VoidDeepNavy
                                    )
                                ) {
                                    Icon(
                                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isSpeaking) "Interrupt" else if (isListening) "Stop Mic" else "Start Mic",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 2: Real Android TTS Voice Selection
                item {
                    Text("ANDROID TTS VOICE SELECTION", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        availableVoices.forEach { voiceName ->
                            val isSelected = selectedVoice == voiceName
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) VoidCyanPrimary.copy(alpha = 0.2f) else VoidSurfaceCard)
                                    .border(1.dp, if (isSelected) VoidCyanPrimary else VoidBorderGlow, RoundedCornerShape(6.dp))
                                    .clickable {
                                        selectedVoice = voiceName
                                        voiceEngine.selectVoiceByName(voiceName)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = voiceName.substringAfterLast("/").take(18),
                                    color = if (isSelected) VoidCyanPrimary else TextSecondary,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                // Section 3: Preset Archetypes
                item {
                    Text("VOICE TIMBRE ARCHETYPES", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(presets) { preset ->
                            val isSelected = voiceParams.preset == preset
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) VoidCyanPrimary.copy(alpha = 0.2f) else VoidSurfaceCard)
                                    .border(1.dp, if (isSelected) VoidCyanPrimary else VoidBorderGlow, RoundedCornerShape(10.dp))
                                    .clickable { voiceEngine.setPreset(preset) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("preset_button_${preset.id}")
                            ) {
                                Column {
                                    Text(
                                        text = preset.displayName,
                                        color = if (isSelected) VoidCyanPrimary else TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = preset.description.take(20) + "...",
                                        color = TextMuted,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 4: Live TTS Controls (Pitch & Speed)
                item {
                    Text("ACTIVE HARDWARE TTS CONTROLS (LIVE)", color = VoidCyanPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }

                item {
                    RealVoiceSliderRow("Pitch (Android TTS Engine)", voiceParams.pitch, 0.5f, 2.0f, "Live hardware pitch modulation") {
                        voiceEngine.setPitch(it)
                    }
                }

                item {
                    RealVoiceSliderRow("Speech Rate / Speed", voiceParams.speed, 0.5f, 2.0f, "Live hardware rate control") {
                        voiceEngine.setSpeed(it)
                    }
                }

                // Section 5: Future DSP Modeling Parameters (Honest Labeling)
                item {
                    Text("ADVANCED DSP PROFILE MODELING (FUTURE DSP)", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }

                item {
                    DspParamRow("Cadence Flow Modulation", voiceParams.cadence, "DSP Profile Attribute") {
                        voiceEngine.updateParameters(voiceParams.copy(cadence = it))
                    }
                }

                item {
                    DspParamRow("Metallic Harmonic Resonance", voiceParams.metallicResonance, "DSP Post-Filter") {
                        voiceEngine.updateParameters(voiceParams.copy(metallicResonance = it))
                    }
                }

                item {
                    DspParamRow("Synthetic Sub-Bass Depth", voiceParams.bass, "Phase 3 Equalizer") {
                        voiceEngine.updateParameters(voiceParams.copy(bass = it))
                    }
                }

                item {
                    DspParamRow("Glitch / Quantum Artifacts", voiceParams.glitchAmount, "Phase 3 Timbre Generator") {
                        voiceEngine.updateParameters(voiceParams.copy(glitchAmount = it))
                    }
                }
            }
        }
    }
}

@Composable
private fun RealVoiceSliderRow(
    name: String,
    value: Float,
    min: Float,
    max: Float,
    subtitle: String,
    onValueChange: (Float) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = VoidSurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(VoidCyanPrimary.copy(alpha = 0.4f), VoidVioletSecondary.copy(alpha = 0.4f)))
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(name, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(subtitle, color = VoidCyanPrimary, fontSize = 9.sp)
                }
                Text(String.format("%.2fx", value), color = VoidCyanPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = min..max,
                colors = SliderDefaults.colors(
                    thumbColor = VoidCyanPrimary,
                    activeTrackColor = VoidCyanPrimary,
                    inactiveTrackColor = VoidSurfaceCardElevated
                )
            )
        }
    }
}

@Composable
private fun DspParamRow(
    name: String,
    value: Float,
    badge: String,
    onValueChange: (Float) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = VoidSurfaceDark.copy(alpha = 0.6f)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(VoidBorderGlow, Color.Transparent))
        )
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(name, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.DarkGray.copy(alpha = 0.4f))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(badge, color = TextMuted, fontSize = 8.sp)
                    }
                }
                Text(String.format("%.2f", value), color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = VoidLavender,
                    activeTrackColor = VoidLavender.copy(alpha = 0.6f),
                    inactiveTrackColor = VoidSurfaceCardElevated
                )
            )
        }
    }
}
