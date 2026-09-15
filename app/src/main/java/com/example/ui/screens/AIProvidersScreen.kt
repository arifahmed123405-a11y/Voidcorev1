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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.provider.AIProvider
import com.example.core.provider.AIProviderRegistry
import com.example.core.provider.AIProviderType
import com.example.core.security.SecureKeyStorage
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidBorderGlow
import com.example.ui.theme.VoidCyanPrimary
import com.example.ui.theme.VoidDeepNavy
import com.example.ui.theme.VoidError
import com.example.ui.theme.VoidSuccess
import com.example.ui.theme.VoidSurfaceCard
import com.example.ui.theme.VoidSurfaceCardElevated
import com.example.ui.theme.VoidSurfaceDark
import com.example.ui.theme.VoidVioletSecondary
import com.example.ui.theme.VoidWarning
import kotlinx.coroutines.launch

@Composable
fun AIProvidersScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val registry = remember { AIProviderRegistry.getInstance(context) }
    val keyStorage = remember { SecureKeyStorage.getInstance(context) }
    val scope = rememberCoroutineScope()

    val providers by registry.providers.collectAsState()
    val failoverHistory by registry.failoverHistory.collectAsState()

    // Test results cache: providerId -> (isTesting, isSuccess, message)
    val testStatuses = remember { mutableStateMapOf<String, Triple<Boolean, Boolean?, String>>() }

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
                IconButton(onClick = onBack, modifier = Modifier.testTag("ai_providers_back_button")) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = TextSecondary)
                }
                Text(
                    text = "AI PROVIDERS & FAILOVER",
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
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Active Failover Pipeline Card
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
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = VoidCyanPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "FAILOVER PRIORITY CHAIN",
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }
                                Text(
                                    text = "Auto-Failover Active",
                                    color = VoidSuccess,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "If the primary provider encounters a rate limit, network timeout, or missing key, VoidCore instantly cascades through your configured providers down to the deterministic on-device engine.",
                                color = TextMuted,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Chain Pills
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val enabledProviders = providers.filter { it.config.isEnabled }
                                enabledProviders.forEachIndexed { idx, p ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(VoidSurfaceCard)
                                            .border(1.dp, VoidBorderGlow, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "${idx + 1}. ${p.type.displayName.split(" ").first()}",
                                            color = if (idx == 0) VoidCyanPrimary else TextSecondary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    if (idx < enabledProviders.size - 1) {
                                        Text("➔", color = TextMuted, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 2: Provider List
                item {
                    Text(
                        text = "CONFIGURED PROVIDERS & SECURE CREDENTIALS",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                itemsIndexed(providers) { index, provider ->
                    ProviderConfigCard(
                        provider = provider,
                        index = index,
                        totalProviders = providers.size,
                        keyStorage = keyStorage,
                        testStatus = testStatuses[provider.id],
                        onModelChanged = { newModel ->
                            registry.updateProviderModel(provider.type, newModel)
                        },
                        onEnabledChanged = { isEnabled ->
                            registry.setProviderEnabled(provider.type, isEnabled)
                        },
                        onMoveUp = {
                            if (index > 0) registry.reorderPriority(index, index - 1)
                        },
                        onMoveDown = {
                            if (index < providers.size - 1) registry.reorderPriority(index, index + 1)
                        },
                        onTestConnectivity = {
                            scope.launch {
                                testStatuses[provider.id] = Triple(true, null, "Testing endpoint...")
                                val (success, message) = provider.testConnectivity()
                                testStatuses[provider.id] = Triple(false, success, message)
                            }
                        }
                    )
                }

                // Section 3: Live Failover Logs
                if (failoverHistory.isNotEmpty()) {
                    item {
                        Text(
                            text = "RECENT FAILOVER TELEMETRY",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = VoidSurfaceCard),
                            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(VoidWarning.copy(alpha = 0.4f), Color.Transparent)))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                failoverHistory.take(5).forEach { event ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${event.attemptedProvider} (${event.model})",
                                                color = VoidWarning,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = event.error,
                                                color = TextMuted,
                                                fontSize = 10.sp,
                                                maxLines = 1
                                            )
                                        }
                                        if (event.nextProvider != null) {
                                            Text(
                                                text = "➔ ${event.nextProvider}",
                                                color = VoidCyanPrimary,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
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
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProviderConfigCard(
    provider: AIProvider,
    index: Int,
    totalProviders: Int,
    keyStorage: SecureKeyStorage,
    testStatus: Triple<Boolean, Boolean?, String>?,
    onModelChanged: (String) -> Unit,
    onEnabledChanged: (Boolean) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onTestConnectivity: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var currentModelText by remember(provider.config.modelName) { mutableStateOf(provider.config.modelName) }
    var apiKeyText by remember { mutableStateOf(keyStorage.getApiKey(provider.id)) }
    var isKeyObscured by remember { mutableStateOf(true) }

    val hasKey = keyStorage.hasApiKey(provider.id) || provider.type == AIProviderType.LOCAL_FALLBACK

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("provider_card_${provider.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (provider.config.isEnabled) VoidSurfaceCard else VoidSurfaceDark.copy(alpha = 0.5f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                if (provider.config.isEnabled) listOf(VoidBorderGlow, VoidVioletSecondary.copy(alpha = 0.3f))
                else listOf(Color.DarkGray.copy(alpha = 0.2f), Color.Transparent)
            )
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Priority, Icon, Name, Enable Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Priority Reordering Arrows
                    if (provider.type != AIProviderType.LOCAL_FALLBACK) {
                        Column {
                            IconButton(
                                onClick = onMoveUp,
                                enabled = index > 0,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = "Move Up",
                                    tint = if (index > 0) VoidCyanPrimary else Color.DarkGray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = onMoveDown,
                                enabled = index < totalProviders - 2,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = "Move Down",
                                    tint = if (index < totalProviders - 2) VoidCyanPrimary else Color.DarkGray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = provider.type.displayName,
                                color = if (provider.config.isEnabled) TextPrimary else TextMuted,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (provider.type.hasFreeTier) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(VoidSuccess.copy(alpha = 0.15f))
                                        .border(1.dp, VoidSuccess.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "Free Tier",
                                        color = VoidSuccess,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Text(
                            text = provider.type.description,
                            color = TextMuted,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }
                }

                // Switch
                Switch(
                    checked = provider.config.isEnabled,
                    onCheckedChange = { onEnabledChanged(it) },
                    enabled = provider.type != AIProviderType.LOCAL_FALLBACK,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = VoidCyanPrimary,
                        checkedTrackColor = VoidCyanPrimary.copy(alpha = 0.3f),
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.DarkGray.copy(alpha = 0.3f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Model Name Configuration Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedTextField(
                    value = currentModelText,
                    onValueChange = {
                        currentModelText = it
                        onModelChanged(it)
                    },
                    label = { Text("Model Identifier", color = TextMuted, fontSize = 10.sp) },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = VoidCyanPrimary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("model_input_${provider.id}"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VoidCyanPrimary,
                        unfocusedBorderColor = VoidBorderGlow,
                        cursorColor = VoidCyanPrimary
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() })
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Test Connectivity Button
                Button(
                    onClick = onTestConnectivity,
                    enabled = (testStatus?.first != true) && provider.config.isEnabled,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VoidSurfaceCardElevated,
                        contentColor = VoidCyanPrimary
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(VoidCyanPrimary, VoidVioletSecondary))),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    if (testStatus?.first == true) {
                        CircularProgressIndicator(
                            color = VoidCyanPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(14.dp)
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Quick Model Chips
            if (provider.type.availableModels.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    provider.type.availableModels.forEach { m ->
                        val isSelected = currentModelText.equals(m, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) VoidCyanPrimary.copy(alpha = 0.2f) else VoidSurfaceCardElevated)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) VoidCyanPrimary else VoidBorderGlow,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .clickable {
                                    currentModelText = m
                                    onModelChanged(m)
                                }
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = m,
                                color = if (isSelected) VoidCyanPrimary else TextSecondary,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // API Key Section (for cloud providers)
            if (provider.type != AIProviderType.LOCAL_FALLBACK) {
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = apiKeyText,
                    onValueChange = { apiKeyText = it },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Key, contentDescription = null, modifier = Modifier.size(12.dp), tint = TextMuted)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Keystore-Protected API Key", color = TextMuted, fontSize = 10.sp)
                        }
                    },
                    visualTransformation = if (isKeyObscured) PasswordVisualTransformation() else VisualTransformation.None,
                    trailingIcon = {
                        IconButton(onClick = { isKeyObscured = !isKeyObscured }) {
                            Icon(
                                imageVector = if (isKeyObscured) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle Visibility",
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("api_key_input_${provider.id}"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VoidCyanPrimary,
                        unfocusedBorderColor = VoidBorderGlow,
                        cursorColor = VoidCyanPrimary
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (hasKey) "✓ AES-GCM Encrypted" else "Key not configured",
                        color = if (hasKey) VoidSuccess else VoidWarning,
                        fontSize = 10.sp
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (apiKeyText.isNotBlank()) {
                            Button(
                                onClick = {
                                    apiKeyText = ""
                                    keyStorage.clearApiKey(provider.id)
                                },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.DarkGray.copy(alpha = 0.4f),
                                    contentColor = VoidError
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Clear", fontSize = 10.sp)
                            }
                        }

                        Button(
                            onClick = {
                                keyStorage.storeApiKey(provider.id, apiKeyText)
                                keyboardController?.hide()
                            },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VoidCyanPrimary,
                                contentColor = VoidDeepNavy
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                        ) {
                            Text("Save Key", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Test Result Banner
            if (testStatus != null && !testStatus.first) {
                Spacer(modifier = Modifier.height(8.dp))
                val isSuccess = testStatus.second == true
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSuccess) VoidSuccess.copy(alpha = 0.12f) else VoidError.copy(alpha = 0.12f))
                        .border(
                            1.dp,
                            if (isSuccess) VoidSuccess.copy(alpha = 0.3f) else VoidError.copy(alpha = 0.3f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isSuccess) VoidSuccess else VoidError,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = testStatus.third,
                            color = if (isSuccess) VoidSuccess else VoidError,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
