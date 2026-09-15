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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.AuditLogEntity
import com.example.core.database.VoidCoreRepository
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
import com.example.ui.theme.VoidWarning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ActivityScreen(onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    val repo = remember { VoidCoreRepository.getInstance(context) }
    val logs by repo.auditLogs.collectAsState(initial = emptyList())

    var selectedFilter by remember { mutableStateOf("ALL") }
    val timeFormat = remember { SimpleDateFormat("MMM d, h:mm:ss a", Locale.getDefault()) }

    val filterChips = listOf("ALL", "HIGH RISK", "USER CONFIRMED", "BLOCKED", "SUCCESS")

    val filteredLogs = logs.filter { item ->
        when (selectedFilter) {
            "HIGH RISK" -> item.riskLevel.equals("HIGH", ignoreCase = true)
            "USER CONFIRMED" -> item.securityDecision.contains("CONFIRM", ignoreCase = true)
            "BLOCKED" -> item.securityDecision.equals("BLOCKED", ignoreCase = true) || item.outcomeStatus.equals("BLOCKED", ignoreCase = true)
            "SUCCESS" -> item.outcomeStatus.equals("SUCCESS", ignoreCase = true)
            else -> true
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
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "A U D I T  L E D G E R",
                        color = VoidCyanPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 3.sp
                    )
                    Text(
                        text = "Immutable on-device security & execution history",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(VoidSurfaceCard)
                        .border(1.dp, VoidBorderGlow, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = VoidSuccess,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Active Gate", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Filter Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterChips) { chip ->
                    val isSelected = selectedFilter == chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) VoidCyanPrimary.copy(alpha = 0.2f) else VoidSurfaceCard)
                            .border(
                                1.dp,
                                if (isSelected) VoidCyanPrimary else VoidBorderGlow,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedFilter = chip }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("filter_chip_${chip.lowercase().replace(" ", "_")}")
                    ) {
                        Text(
                            text = chip,
                            color = if (isSelected) VoidCyanPrimary else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Audit Logs List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filteredLogs.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No audit records matching filter.",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                items(filteredLogs) { log ->
                    AuditLogCard(log, timeFormat)
                }
            }
        }
    }
}

@Composable
fun AuditLogCard(log: AuditLogEntity, timeFormat: SimpleDateFormat) {
    val isHighRisk = log.riskLevel.equals("HIGH", ignoreCase = true)
    val isSuccess = log.outcomeStatus.equals("SUCCESS", ignoreCase = true)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("audit_card_${log.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = VoidSurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(
                    if (isHighRisk) VoidWarning.copy(alpha = 0.6f) else VoidBorderGlow,
                    VoidBorderGlow
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
                        imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = null,
                        tint = if (isSuccess) VoidSuccess else VoidError,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = log.actionName,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (log.riskLevel.uppercase()) {
                                "HIGH" -> VoidError.copy(alpha = 0.15f)
                                "MEDIUM" -> VoidWarning.copy(alpha = 0.15f)
                                else -> VoidSuccess.copy(alpha = 0.15f)
                            }
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = log.riskLevel.uppercase(),
                        color = when (log.riskLevel.uppercase()) {
                            "HIGH" -> VoidError
                            "MEDIUM" -> VoidWarning
                            else -> VoidSuccess
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Target: \"${log.target}\"",
                color = VoidLavender,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = log.details,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gate: ${log.securityDecision} • Status: ${log.outcomeStatus}",
                    color = TextMuted,
                    fontSize = 10.sp
                )
                Text(
                    text = timeFormat.format(Date(log.timestamp)),
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}
