package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.renderer.DefaultCoreRendererBackend
import com.example.core.renderer.DockingSide
import com.example.core.renderer.OpenGLES3CoreRendererBackend
import com.example.core.renderer.RendererQuality
import com.example.core.renderer.VoidCorePresenceView
import com.example.core.state.AssistantState
import com.example.core.state.PresenceForm
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

enum class MockPhoneSurface {
    NONE,
    VIRTUAL_KEYBOARD,
    FULLSCREEN_APP,
    LOCK_SCREEN,
    ACTION_TARGET_INSPECT
}

@Composable
fun VisualLabScreen(
    onBack: () -> Unit
) {
    var selectedState by remember { mutableStateOf(AssistantState.LISTENING) }
    var selectedForm by remember { mutableStateOf(PresenceForm.FULL_PRESENCE) }
    var selectedQuality by remember { mutableStateOf(RendererQuality.CINEMATIC) }
    var audioEnergy by remember { mutableFloatStateOf(0.4f) }
    var isSimulatingAudio by remember { mutableStateOf(false) }
    var reduceMotion by remember { mutableStateOf(false) }
    var reduceTransparency by remember { mutableStateOf(false) }
    var dockingSide by remember { mutableStateOf(DockingSide.NONE) }
    var useGlBackend by remember { mutableStateOf(false) }
    var activeSurface by remember { mutableStateOf(MockPhoneSurface.NONE) }

    val activeBackend = remember(useGlBackend) {
        if (useGlBackend) OpenGLES3CoreRendererBackend() else DefaultCoreRendererBackend()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidDeepNavy)
            .statusBarsPadding()
            .testTag("visual_lab_screen")
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("visual_lab_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Visual Presence Lab",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "11-state choreography, forms & surface previews",
                    fontSize = 12.sp,
                    color = VoidCyanPrimary
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Preview Canvas Box
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = VoidSurfaceCardElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VoidBorderGlow)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CORE STAGE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(VoidCyanPrimary.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = activeBackend.backendName,
                                    color = VoidCyanPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Stage Container with phone preview overlay
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(VoidSurfaceDark, Color(0xFF02040A)),
                                        radius = 450f
                                    )
                                )
                                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp)),
                            contentAlignment = when (dockingSide) {
                                DockingSide.LEFT -> Alignment.CenterStart
                                DockingSide.RIGHT -> Alignment.CenterEnd
                                DockingSide.NONE -> Alignment.Center
                            }
                        ) {
                            // Surface Preview background layer if selected
                            SurfacePreviewOverlay(surface = activeSurface)

                            // Living Core Visual View
                            Box(modifier = Modifier.padding(16.dp)) {
                                VoidCorePresenceView(
                                    state = selectedState,
                                    form = selectedForm,
                                    audioEnergy = if (isSimulatingAudio) audioEnergy else 0.05f,
                                    quality = selectedQuality,
                                    reduceMotion = reduceMotion,
                                    reduceTransparency = reduceTransparency,
                                    dockingSide = dockingSide,
                                    backend = activeBackend,
                                    modifier = Modifier.testTag("visual_lab_core_view")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // State & Form indicators
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "State: ${selectedState.name}",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Form: ${selectedForm.name}",
                                color = VoidLavender,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // 11 Canonical States Selector
            item {
                Text(
                    text = "CANONICAL 11-STATE VISUAL CHOREOGRAPHY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(AssistantState.values()) { state ->
                        val isSelected = selectedState == state
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) VoidCyanPrimary.copy(alpha = 0.2f)
                                    else VoidSurfaceCard
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) VoidCyanPrimary else VoidBorderGlow,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedState = state }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = state.name,
                                color = if (isSelected) VoidCyanPrimary else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // 4 Presence Forms Selector
            item {
                Text(
                    text = "PRESENCE FORMS (MORPHING)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PresenceForm.values().forEach { form ->
                        val isSelected = selectedForm == form
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) VoidVioletSecondary.copy(alpha = 0.25f)
                                    else VoidSurfaceCard
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) VoidVioletSecondary else VoidBorderGlow,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedForm = form }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = form.name.replace("_", " "),
                                color = if (isSelected) TextPrimary else TextMuted,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Mock Phone Surfaces Preview Selector
            item {
                Text(
                    text = "FAKE PHONE SURFACE SIMULATOR",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(MockPhoneSurface.values()) { surface ->
                        val isSelected = activeSurface == surface
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) Color(0xFFFFB74D).copy(alpha = 0.2f)
                                    else VoidSurfaceCard
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) Color(0xFFFFB74D) else VoidBorderGlow,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { activeSurface = surface }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = surface.name.replace("_", " "),
                                color = if (isSelected) Color(0xFFFFB74D) else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Docking, Accessibility & Backend Controls
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = VoidSurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VoidBorderGlow)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "DOCKING & ACCESSIBILITY ENVELOPES",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Docking side
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Docking Anchor", color = TextSecondary, fontSize = 13.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                DockingSide.values().forEach { side ->
                                    val isSelected = dockingSide == side
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) VoidCyanPrimary else VoidSurfaceDark)
                                            .clickable { dockingSide = side }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = side.name,
                                            color = if (isSelected) VoidDeepNavy else TextMuted,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Reduce Motion Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Reduce Motion", color = TextPrimary, fontSize = 13.sp)
                                Text(text = "Disables harmonic ribbons and orbits", color = TextMuted, fontSize = 11.sp)
                            }
                            Switch(
                                checked = reduceMotion,
                                onCheckedChange = { reduceMotion = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = VoidCyanPrimary,
                                    checkedTrackColor = VoidCyanPrimary.copy(alpha = 0.3f)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Reduce Transparency Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Reduce Transparency", color = TextPrimary, fontSize = 13.sp)
                                Text(text = "Solid contrast fills for particles & halo", color = TextMuted, fontSize = 11.sp)
                            }
                            Switch(
                                checked = reduceTransparency,
                                onCheckedChange = { reduceTransparency = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = VoidCyanPrimary,
                                    checkedTrackColor = VoidCyanPrimary.copy(alpha = 0.3f)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // OpenGL ES 3.0 Backend Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "OpenGL ES 3.0 Shader Pipeline", color = TextPrimary, fontSize = 13.sp)
                                Text(text = "Decoupled RendererBackend shader bridge", color = TextMuted, fontSize = 11.sp)
                            }
                            Switch(
                                checked = useGlBackend,
                                onCheckedChange = { useGlBackend = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = VoidVioletSecondary,
                                    checkedTrackColor = VoidVioletSecondary.copy(alpha = 0.3f)
                                )
                            )
                        }
                    }
                }
            }

            // Quality Tier Selector
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = VoidSurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VoidBorderGlow)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "RENDERER QUALITY TIERS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        RendererQuality.values().forEach { q ->
                            val isSelected = selectedQuality == q
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) VoidSurfaceDark else Color.Transparent)
                                    .clickable { selectedQuality = q }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) VoidCyanPrimary else TextMuted)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "${q.displayName} (${q.particleCount} particles)",
                                        color = if (isSelected) VoidCyanPrimary else TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(text = q.description, color = TextMuted, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Audio Modulation Simulator
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = VoidSurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VoidBorderGlow)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "AUDIO ENERGY MODULATION",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Switch(
                                checked = isSimulatingAudio,
                                onCheckedChange = { isSimulatingAudio = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = VoidCyanPrimary,
                                    checkedTrackColor = VoidCyanPrimary.copy(alpha = 0.3f)
                                )
                            )
                        }

                        if (isSimulatingAudio) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Slider(
                                value = audioEnergy,
                                onValueChange = { audioEnergy = it },
                                valueRange = 0f..1f,
                                colors = SliderDefaults.colors(
                                    thumbColor = VoidCyanPrimary,
                                    activeTrackColor = VoidCyanPrimary
                                )
                            )
                            Text(
                                text = "Simulated Energy: ${(audioEnergy * 100).toInt()}%",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SurfacePreviewOverlay(surface: MockPhoneSurface) {
    when (surface) {
        MockPhoneSurface.NONE -> Unit
        MockPhoneSurface.VIRTUAL_KEYBOARD -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E293B).copy(alpha = 0.85f))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "VIRTUAL KEYBOARD OCCLUSION AREA\n(Safe Area Boundary Test)",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
        MockPhoneSurface.FULLSCREEN_APP -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = "TARGET APP: Maps / Reader (Edge Inset Inspection)",
                    color = Color(0xFF38BDF8).copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        MockPhoneSurface.LOCK_SCREEN -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "06:00",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Thin
                )
                Text(
                    text = "Tuesday, September 15 • Alarm Surface",
                    color = Color.White.copy(alpha = 0.3f),
                    fontSize = 11.sp
                )
            }
        }
        MockPhoneSurface.ACTION_TARGET_INSPECT -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .border(1.dp, Color(0xFFF43F5E).copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.BottomCenter
            ) {
                Text(
                    text = "ACTION TARGET BOUNDS [x:40, y:120, w:300, h:80]",
                    color = Color(0xFFF43F5E),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
        }
    }
}
