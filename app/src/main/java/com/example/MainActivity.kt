package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agent.AgentBrain
import com.example.core.state.AssistantState
import com.example.core.state.AssistantStateMachine
import com.example.engine.voice.SpeechRecognitionManager
import com.example.engine.voice.VoiceEngine
import com.example.ui.screens.AIProvidersScreen
import com.example.ui.screens.ActivityScreen
import com.example.ui.screens.AgentWorkspaceScreen
import com.example.ui.screens.AudioLabScreen
import com.example.ui.screens.AutomationsScreen
import com.example.ui.screens.CallIntelligenceScreen
import com.example.ui.screens.ConnectionsScreen
import com.example.ui.screens.ConversationScreen
import com.example.ui.screens.DiagnosticsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MemoryVaultScreen
import com.example.ui.screens.MorningBriefingCallScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.OfflineScreen
import com.example.ui.screens.PerformanceScreen
import com.example.ui.screens.PrivacyDashboardScreen
import com.example.ui.screens.ProjectsScreen
import com.example.ui.screens.TrustSafetyScreen
import com.example.ui.screens.UniversalSearchScreen
import com.example.ui.screens.VisionScreen
import com.example.ui.screens.VisualLabScreen
import com.example.ui.screens.VoiceLabScreen
import com.example.ui.screens.YouHubScreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidBorderGlow
import com.example.ui.theme.VoidCoreTheme
import com.example.ui.theme.VoidCyanPrimary
import com.example.ui.theme.VoidDeepNavy
import com.example.ui.theme.VoidError
import com.example.ui.theme.VoidLavender
import com.example.ui.theme.VoidSurfaceCard
import com.example.ui.theme.VoidSurfaceCardElevated
import com.example.ui.theme.VoidSurfaceDark
import com.example.ui.theme.VoidVioletSecondary
import com.example.ui.theme.VoidWarning

enum class MainTab(val title: String, val icon: ImageVector) {
    HOME("Core", Icons.Default.Home),
    CONVERSATION("Dialogue", Icons.Default.ChatBubbleOutline),
    AUTOMATIONS("Routines", Icons.Default.AutoAwesome),
    ACTIVITY("Audit", Icons.Default.History),
    YOU("System", Icons.Default.PersonOutline)
}

class MainActivity : ComponentActivity() {

    private lateinit var stateMachine: AssistantStateMachine
    private lateinit var voiceEngine: VoiceEngine
    private lateinit var speechRecognizer: SpeechRecognitionManager
    private lateinit var agentBrain: AgentBrain

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        stateMachine = AssistantStateMachine.getInstance(this)
        voiceEngine = VoiceEngine.getInstance(this)
        speechRecognizer = SpeechRecognitionManager.getInstance(this)
        agentBrain = AgentBrain.getInstance(this)

        // Wire speech recognition results directly into AgentBrain
        speechRecognizer.onSpeechResult = { recognizedText ->
            agentBrain.handleUserInput(recognizedText)
        }

        // Voice interruption wiring: when user starts speaking while assistant is speaking
        speechRecognizer.onPartialResult = { partial ->
            if (partial.isNotBlank() && stateMachine.currentState.value == AssistantState.SPEAKING) {
                agentBrain.interruptAssistant()
            }
        }

        setContent {
            VoidCoreTheme {
                VoidCoreAppRoot(
                    stateMachine = stateMachine,
                    voiceEngine = voiceEngine,
                    speechRecognizer = speechRecognizer,
                    agentBrain = agentBrain
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }
}

@Composable
fun VoidCoreAppRoot(
    stateMachine: AssistantStateMachine,
    voiceEngine: VoiceEngine,
    speechRecognizer: SpeechRecognitionManager,
    agentBrain: AgentBrain
) {
    var selectedTab by remember { mutableStateOf(MainTab.HOME) }
    var currentSubRoute by remember { mutableStateOf<String?>(null) }

    val pendingConfirmation by stateMachine.pendingConfirmation.collectAsState()

    // High Risk Security Confirmation Modal
    pendingConfirmation?.let { conf ->
        AlertDialog(
            onDismissRequest = { conf.onDeny() },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = VoidWarning,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = conf.title,
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = conf.description,
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(VoidWarning.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = conf.riskBadge,
                            color = VoidWarning,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
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
                    modifier = Modifier.testTag("deny_action_button")
                ) {
                    Text("Deny", color = VoidError)
                }
            },
            containerColor = VoidSurfaceCardElevated,
            shape = RoundedCornerShape(18.dp)
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = VoidDeepNavy,
        bottomBar = {
            if (currentSubRoute == null) {
                NavigationBar(
                    containerColor = VoidSurfaceDark,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .border(1.dp, VoidBorderGlow, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .testTag("main_bottom_nav_bar")
                ) {
                    MainTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    tint = if (isSelected) VoidCyanPrimary else TextMuted
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    color = if (isSelected) VoidCyanPrimary else TextMuted,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = VoidCyanPrimary.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (currentSubRoute == null) innerPadding.calculateBottomPadding() else 0.dp)
        ) {
            Crossfade(targetState = currentSubRoute ?: selectedTab.name, label = "ScreenTransition") { targetRoute ->
                when (targetRoute) {
                    // Core Tabs
                    MainTab.HOME.name -> HomeScreen(
                        stateMachine = stateMachine,
                        agentBrain = agentBrain,
                        voiceEngine = voiceEngine,
                        speechRecognizer = speechRecognizer,
                        onNavigate = { route -> currentSubRoute = route }
                    )
                    MainTab.CONVERSATION.name -> ConversationScreen(
                        stateMachine = stateMachine,
                        agentBrain = agentBrain,
                        voiceEngine = voiceEngine,
                        speechRecognizer = speechRecognizer
                    )
                    MainTab.AUTOMATIONS.name -> AutomationsScreen(
                        agentBrain = agentBrain,
                        onNavigate = { route -> currentSubRoute = route }
                    )
                    MainTab.ACTIVITY.name -> ActivityScreen(
                        onNavigate = { route -> currentSubRoute = route }
                    )
                    MainTab.YOU.name -> YouHubScreen(
                        stateMachine = stateMachine,
                        voiceEngine = voiceEngine,
                        onNavigate = { route -> currentSubRoute = route }
                    )

                    // Specialty Surfaces
                    "briefing_call" -> MorningBriefingCallScreen(
                        stateMachine = stateMachine,
                        voiceEngine = voiceEngine,
                        onBack = { currentSubRoute = null }
                    )
                    "voice_lab" -> VoiceLabScreen(
                        voiceEngine = voiceEngine,
                        onBack = { currentSubRoute = null }
                    )
                    "visual_lab" -> VisualLabScreen(
                        onBack = { currentSubRoute = null }
                    )
                    "audio_lab" -> AudioLabScreen(
                        onBack = { currentSubRoute = null }
                    )
                    "ai_providers" -> AIProvidersScreen(
                        onBack = { currentSubRoute = null }
                    )
                    "privacy_dashboard" -> PrivacyDashboardScreen(
                        onBack = { currentSubRoute = null }
                    )
                    "diagnostics" -> DiagnosticsScreen(
                        stateMachine = stateMachine,
                        onBack = { currentSubRoute = null }
                    )
                    "search" -> UniversalSearchScreen(
                        onBack = { currentSubRoute = null }
                    )
                    "vision" -> VisionScreen(
                        onBack = { currentSubRoute = null }
                    )
                    "memory_vault" -> MemoryVaultScreen(
                        onBack = { currentSubRoute = null }
                    )
                    "projects" -> ProjectsScreen(
                        onBack = { currentSubRoute = null }
                    )
                    "call_intelligence" -> CallIntelligenceScreen(
                        onBack = { currentSubRoute = null }
                    )
                    "notifications_hub" -> NotificationsScreen(
                        onBack = { currentSubRoute = null }
                    )
                    "workspace" -> AgentWorkspaceScreen(
                        onBack = { currentSubRoute = null }
                    )
                    "connections" -> ConnectionsScreen(
                        onBack = { currentSubRoute = null }
                    )
                    "trust_safety" -> TrustSafetyScreen(
                        onBack = { currentSubRoute = null }
                    )
                    "offline" -> OfflineScreen(
                        agentBrain = agentBrain,
                        onBack = { currentSubRoute = null }
                    )
                    "performance" -> PerformanceScreen(
                        onBack = { currentSubRoute = null }
                    )

                    else -> HomeScreen(
                        stateMachine = stateMachine,
                        agentBrain = agentBrain,
                        voiceEngine = voiceEngine,
                        speechRecognizer = speechRecognizer,
                        onNavigate = { route -> currentSubRoute = route }
                    )
                }
            }
        }
    }
}
