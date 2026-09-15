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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.state.AssistantStateMachine
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

data class CapabilityMatrixItem(
    val feature: String,
    val status: String, // IMPLEMENTED, LIMITED, NOT_AVAILABLE
    val description: String,
    val technicalBoundary: String
)

@Composable
fun DiagnosticsScreen(
    stateMachine: AssistantStateMachine,
    onBack: () -> Unit
) {
    val currentState by stateMachine.currentState.collectAsState()
    val currentForm by stateMachine.currentForm.collectAsState()
    val audioEnergy by stateMachine.audioEnergyLevel.collectAsState()

    val capabilities = listOf(
        CapabilityMatrixItem(
            feature = "Canonical 11-State State Engine",
            status = "IMPLEMENTED",
            description = "Single semantic state source across all 11 assistant states",
            technicalBoundary = "AssistantStateEngine with StateFlow reactive emissions"
        ),
        CapabilityMatrixItem(
            feature = "Gated Execution & Deny-by-Default",
            status = "IMPLEMENTED",
            description = "Security gate strictly blocks tool execution until native adapters register in Phase 1+",
            technicalBoundary = "Phase0SecurityGate + GatedExecutor with argument freeze & pre-dispatch audit"
        ),
        CapabilityMatrixItem(
            feature = "6 Voice Presets Architecture",
            status = "IMPLEMENTED",
            description = "Neutral Core, Void, Architect, Spectral, Titan, and synthetic non-human Omega",
            technicalBoundary = "VoicePresets registry with parametric resonance & cadence models"
        ),
        CapabilityMatrixItem(
            feature = "Provider-Neutral Contracts",
            status = "IMPLEMENTED",
            description = "Decoupled interfaces for generation, local model, tools, vision, and workflow planning",
            technicalBoundary = "Kotlin Coroutine Flow and Result-wrapped contracts"
        ),
        CapabilityMatrixItem(
            feature = "Repository Pattern & Persistence",
            status = "IMPLEMENTED",
            description = "Preferences, context, automations, and activity log abstractions. No plain DB API keys.",
            technicalBoundary = "Repository interfaces with clean architecture memory/Room data layers"
        ),
        CapabilityMatrixItem(
            feature = "Microphone & Speech Audio Input",
            status = "PHASE 0 BOUNDARY",
            description = "Audio recording and speech recognition engine",
            technicalBoundary = "Audio input is stubbed/neutral for Phase 0; active audio recognition joins in Phase 1."
        ),
        CapabilityMatrixItem(
            feature = "Live Cloud AI Network Provider",
            status = "PHASE 0 BOUNDARY",
            description = "Direct remote REST/gRPC inference calls",
            technicalBoundary = "Deactivated for Phase 0 to guarantee pure local architecture verification."
        ),
        CapabilityMatrixItem(
            feature = "System Accessibility & System Overlays",
            status = "PHASE 0 BOUNDARY",
            description = "Window overlays and global accessibility gestures",
            technicalBoundary = "Scoped to subsequent phases per Phase 0 isolation requirements."
        )
    )

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("diagnostics_back_button")) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = TextSecondary)
                }
                Text(
                    text = "SYSTEM DIAGNOSTICS",
                    color = VoidCyanPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Live State Inspection Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = VoidSurfaceCardElevated),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(VoidCyanPrimary.copy(0.4f), VoidVioletSecondary.copy(0.4f))))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("CANONICAL STATE MACHINE", color = VoidCyanPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            MetricRow("Current Semantic State", currentState.displayName)
                            MetricRow("Active Presence Form", currentForm.displayName)
                            MetricRow("Audio Energy Envelope", String.format("%.2f", audioEnergy))
                            MetricRow("Database Engine", "Room SQLite v1 (Encrypted DB)")
                        }
                    }
                }

                item {
                    Text("CAPABILITY & TECHNICAL BOUNDARY MATRIX", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }

                items(capabilities) { cap ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = VoidSurfaceCard),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(VoidBorderGlow, VoidBorderGlow)))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(cap.feature, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            when (cap.status) {
                                                "IMPLEMENTED" -> VoidSuccess.copy(alpha = 0.15f)
                                                "LIMITED" -> VoidWarning.copy(alpha = 0.15f)
                                                else -> VoidError.copy(alpha = 0.15f)
                                            }
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = cap.status,
                                        color = when (cap.status) {
                                            "IMPLEMENTED" -> VoidSuccess
                                            "LIMITED" -> VoidWarning
                                            else -> VoidError
                                        },
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(cap.description, color = TextSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Boundary: ${cap.technicalBoundary}", color = VoidLavender, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextSecondary, fontSize = 12.sp)
        Text(text = value, color = VoidCyanPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}
