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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agent.AgentBrain
import com.example.core.database.AutomationEntity
import com.example.core.database.VoidCoreRepository
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
import kotlinx.coroutines.launch

@Composable
fun AutomationsScreen(
    agentBrain: AgentBrain,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val repo = remember { VoidCoreRepository.getInstance(context) }
    val scope = rememberCoroutineScope()
    val automations by repo.automations.collectAsState(initial = emptyList())

    var showCreateDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newTriggerConfig by remember { mutableStateOf("") }
    var newActionSummary by remember { mutableStateOf("") }
    var selectedTriggerType by remember { mutableStateOf("TIME_SCHEDULE") }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = {
                Text(
                    text = "New On-Device Routine",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Routine Title (e.g. Study Mode)") },
                        modifier = Modifier.fillMaxWidth().testTag("automation_title_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )
                    OutlinedTextField(
                        value = newTriggerConfig,
                        onValueChange = { newTriggerConfig = it },
                        label = { Text("Trigger (e.g. Daily at 8:00 PM)") },
                        modifier = Modifier.fillMaxWidth().testTag("automation_trigger_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )
                    OutlinedTextField(
                        value = newActionSummary,
                        onValueChange = { newActionSummary = it },
                        label = { Text("Action Summary") },
                        modifier = Modifier.fillMaxWidth().testTag("automation_action_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isNotBlank()) {
                            scope.launch {
                                repo.saveAutomation(
                                    AutomationEntity(
                                        title = newTitle,
                                        triggerType = selectedTriggerType,
                                        triggerConfig = newTriggerConfig.ifBlank { "Custom Trigger" },
                                        actionSummary = newActionSummary.ifBlank { "Execute sequence" },
                                        isEnabled = true
                                    )
                                )
                                showCreateDialog = false
                                newTitle = ""
                                newTriggerConfig = ""
                                newActionSummary = ""
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VoidCyanPrimary, contentColor = VoidDeepNavy),
                    modifier = Modifier.testTag("save_automation_button")
                ) {
                    Text("Create Routine", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = TextMuted)
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
                        text = "A U T O M A T I O N S",
                        color = VoidCyanPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 3.sp
                    )
                    Text(
                        text = "Autonomous triggers, briefings & background routines",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                IconButton(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(VoidSurfaceCard)
                        .border(1.dp, VoidBorderGlow, CircleShape)
                        .testTag("add_automation_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Automation",
                        tint = VoidCyanPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Automation List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(automations) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("automation_card_${item.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = VoidSurfaceCard),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(
                                if (item.isEnabled) listOf(VoidCyanPrimary.copy(0.4f), VoidVioletSecondary.copy(0.4f))
                                else listOf(VoidBorderGlow, VoidBorderGlow)
                            )
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(VoidSurfaceCardElevated),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when (item.triggerType) {
                                                "TIME_SCHEDULE" -> Icons.Default.Schedule
                                                "CALL_FOLLOWUP" -> Icons.Default.Call
                                                "HEADPHONE_CONNECT" -> Icons.Default.Headphones
                                                else -> Icons.Default.AutoAwesome
                                            },
                                            contentDescription = null,
                                            tint = if (item.isEnabled) VoidCyanPrimary else TextMuted,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = item.title,
                                            color = TextPrimary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = item.triggerConfig,
                                            color = VoidLavender,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                Switch(
                                    checked = item.isEnabled,
                                    onCheckedChange = { isChecked ->
                                        scope.launch { repo.toggleAutomation(item, isChecked) }
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = VoidCyanPrimary,
                                        checkedTrackColor = VoidSurfaceCardElevated,
                                        uncheckedThumbColor = TextMuted,
                                        uncheckedTrackColor = VoidSurfaceDark
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = item.actionSummary,
                                color = TextSecondary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (item.title.contains("Briefing")) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(VoidSurfaceCardElevated)
                                            .clickable { onNavigate("briefing_call") }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text("Open Call Surface", color = VoidCyanPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(VoidCyanPrimary.copy(alpha = 0.15f))
                                        .border(1.dp, VoidCyanPrimary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .clickable {
                                            agentBrain.handleUserInput("Run routine: ${item.title}")
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                        .testTag("run_now_button_${item.id}")
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Run Now",
                                            tint = VoidCyanPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Run Now", color = VoidCyanPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
