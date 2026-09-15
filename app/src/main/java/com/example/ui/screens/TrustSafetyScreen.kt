package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidBorderGlow
import com.example.ui.theme.VoidCyanPrimary
import com.example.ui.theme.VoidDeepNavy
import com.example.ui.theme.VoidSurfaceCard
import com.example.ui.theme.VoidSurfaceDark
import com.example.ui.theme.VoidWarning

@Composable
fun TrustSafetyScreen(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidDeepNavy)
            .statusBarsPadding()
            .testTag("trust_safety_screen")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("trust_safety_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Trust & Safety Architecture",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Gated execution & deny-by-default policy",
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
                    border = androidx.compose.foundation.BorderStroke(1.dp, VoidBorderGlow)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = VoidCyanPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = "Deny-by-Default Foundation",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Phase 0 enforces an absolute zero-trust execution sandbox. Tools cannot be directly invoked by the model or UI without passing argument immutability freeze, pre-dispatch audit logging, and the SecurityGate.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            item {
                Text(
                    text = "FOUR PILLARS OF GATED SECURITY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMuted
                )
            }

            item {
                SecurityPillarItem(
                    title = "1. Argument Freeze",
                    desc = "Arguments are unmodifiable at construction before evaluation."
                )
                Spacer(modifier = Modifier.height(8.dp))
                SecurityPillarItem(
                    title = "2. Pre-Dispatch Audit",
                    desc = "Audit ledger registers the intent BEFORE any execution step starts."
                )
                Spacer(modifier = Modifier.height(8.dp))
                SecurityPillarItem(
                    title = "3. Security Gate Evaluation",
                    desc = "Tiered validation checks for user confirmation requirements or blocks."
                )
                Spacer(modifier = Modifier.height(8.dp))
                SecurityPillarItem(
                    title = "4. Adapter Registry",
                    desc = "Tools must have an explicitly registered native adapter to execute."
                )
            }
        }
    }
}

@Composable
private fun SecurityPillarItem(title: String, desc: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = VoidSurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, VoidBorderGlow)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = desc, color = TextSecondary, fontSize = 12.sp)
        }
    }
}
