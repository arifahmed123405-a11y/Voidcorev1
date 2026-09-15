package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feature.call.CallIntelligenceManager
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

@Composable
fun CallIntelligenceScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val callManager = remember { CallIntelligenceManager.getInstance(context) }
    val activeCall by callManager.simulatedCall.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(VoidDeepNavy, VoidSurfaceDark, VoidDeepNavy)))
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
                IconButton(onClick = onBack, modifier = Modifier.testTag("call_intel_back_button")) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = TextSecondary)
                }
                Text(
                    text = "CALL SCREENING ASSISTANT",
                    color = VoidCyanPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Policy Guarantee Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = VoidSurfaceCardElevated),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(VoidCyanPrimary.copy(0.4f), VoidVioletSecondary.copy(0.4f))))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Assistant Transparency & Disclosure", color = VoidCyanPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "VoidCore always explicitly identifies itself to callers as an automated AI assistant and automatically schedules a 30-minute callback reminder.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (activeCall == null) {
                // Trigger Simulated Call Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = VoidSurfaceCard)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Simulate Incoming Call", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Test how VoidCore screens calls and creates callback alarms.", color = TextMuted, fontSize = 12.sp)

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(VoidCyanPrimary)
                                .clickable {
                                    callManager.triggerSimulatedCall("Ahmed (Project Lead)", "+1 (555) 019-2834")
                                }
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                                .testTag("simulate_incoming_call_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = VoidDeepNavy)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Trigger Call from Ahmed", color = VoidDeepNavy, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            } else {
                val call = activeCall!!
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = VoidSurfaceCardElevated),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(VoidCyanPrimary, VoidVioletSecondary)))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(call.callerName, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text(call.callerNumber, color = VoidLavender, fontSize = 12.sp)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(VoidSuccess.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(if (call.isScreeningActive) "SCREENING ACTIVE" else "RINGING", color = VoidSuccess, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (call.screenedTranscript.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Screening Transcript (Live Relay)", color = VoidCyanPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            for (line in call.screenedTranscript) {
                                Text(line, color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp, modifier = Modifier.padding(vertical = 2.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        if (!call.isScreeningActive) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(VoidCyanPrimary)
                                        .clickable { callManager.startScreening("I am in sprint planning. I will call you back in 30 minutes.") }
                                        .padding(horizontal = 16.dp, vertical = 10.dp)
                                        .testTag("start_screening_button")
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.PhoneInTalk, contentDescription = null, tint = VoidDeepNavy)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Screen with VoidCore", color = VoidDeepNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(VoidSurfaceCard)
                                        .clickable { callManager.dismissCall() }
                                        .padding(horizontal = 16.dp, vertical = 10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.CallEnd, contentDescription = null, tint = VoidError)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Decline", color = TextSecondary, fontSize = 12.sp)
                                    }
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("✓ 30m Callback Reminder Created", color = VoidSuccess, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(VoidSurfaceCard)
                                        .clickable { callManager.dismissCall() }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Finish", color = VoidCyanPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
