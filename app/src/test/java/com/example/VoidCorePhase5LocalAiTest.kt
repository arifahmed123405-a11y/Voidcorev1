package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.engine.local.ILocalModelRuntime
import com.example.engine.local.LocalModelManager
import com.example.engine.local.LocalModelRuntime
import com.example.engine.local.ModelDownloadState
import com.example.engine.local.ModelProfile
import com.example.engine.local.ModelProfileTier
import com.example.engine.local.TaskRouteResult
import com.example.engine.local.TaskRouter
import com.example.engine.security.StructuredIntent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class VoidCorePhase5LocalAiTest {

    private lateinit var context: Context
    private lateinit var modelManager: LocalModelManager
    private lateinit var localRuntime: LocalModelRuntime
    private lateinit var taskRouter: TaskRouter

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        modelManager = LocalModelManager.getInstance(context)
        localRuntime = LocalModelRuntime(ModelProfile.LITE_PROFILE)
        taskRouter = TaskRouter(localRuntime = localRuntime)
    }

    @Test
    fun testModelProfilesConfiguration() {
        assertEquals(3, ModelProfile.ALL_PROFILES.size)
        
        val lite = ModelProfile.LITE_PROFILE
        assertEquals(ModelProfileTier.LITE, lite.tier)
        assertTrue(lite.ramRequiredMb <= 512)
        assertNotNull(lite.sha256Checksum)
        
        val balanced = ModelProfile.BALANCED_PROFILE
        assertEquals(ModelProfileTier.BALANCED, balanced.tier)
        assertTrue(balanced.ramRequiredMb in 513..1024)

        val pro = ModelProfile.PRO_PROFILE
        assertEquals(ModelProfileTier.PRO, pro.tier)
        assertTrue(pro.ramRequiredMb > 1000)
    }

    @Test
    fun testLocalModelManagerLifecycleAndStorageTelemetry() = runBlocking {
        val telemetry = modelManager.telemetry.value
        assertTrue(telemetry.totalDeviceRamMb > 0)
        assertTrue(telemetry.availableDeviceRamMb > 0)
        assertNotNull(telemetry.freeInternalStorageFormatted)

        // Select Balanced profile
        modelManager.selectProfile(ModelProfile.BALANCED_PROFILE)
        assertEquals(ModelProfile.BALANCED_PROFILE.id, modelManager.selectedProfile.value.id)

        // Test loading model
        modelManager.loadModel(ModelProfile.LITE_PROFILE)
        assertEquals(ModelProfile.LITE_PROFILE.id, modelManager.loadedProfile.value?.id)

        // Test unloading model
        modelManager.unloadModel()
        assertEquals(null, modelManager.loadedProfile.value)
    }

    @Test
    fun testLocalModelRuntimeStreamingAndContext() = runBlocking {
        val prompt = "Who are you and what is your design?"
        val history = listOf("user" to "Hello", "assistant" to "Greetings.")

        var tokenCount = 0
        var fullResponse = ""
        localRuntime.generateStream(prompt, history).collect { token ->
            tokenCount++
            fullResponse += token
        }

        assertTrue(tokenCount > 0)
        assertTrue(fullResponse.isNotBlank())
        assertTrue(fullResponse.contains("VoidCore", ignoreCase = true) || fullResponse.contains("offline", ignoreCase = true))

        // Profile loading check
        localRuntime.loadProfile(ModelProfile.BALANCED_PROFILE)
        assertEquals(ModelProfile.BALANCED_PROFILE.name, localRuntime.status.value.modelName)
        assertTrue(localRuntime.status.value.isLoaded)

        // Profile unloading check
        localRuntime.unloadModel()
        assertFalse(localRuntime.status.value.isLoaded)
    }

    @Test
    fun testStrictWhitelistJsonAmbiguousIntentResolution() = runBlocking {
        // "it's too dark in here" -> ambiguous -> Flashlight(enabled = true)
        val darkIntent = localRuntime.resolveAmbiguousIntent("it's too dark in here")
        assertTrue("Expected Flashlight intent, got $darkIntent", darkIntent is StructuredIntent.Flashlight)
        assertTrue((darkIntent as StructuredIntent.Flashlight).enabled)

        // "i can't hear anything" -> ambiguous -> SetVolume
        val soundIntent = localRuntime.resolveAmbiguousIntent("i can't hear anything")
        assertTrue("Expected SetVolume intent, got $soundIntent", soundIntent is StructuredIntent.SetVolume)
        assertTrue((soundIntent as StructuredIntent.SetVolume).percentage >= 70)

        // "wake me up at 7" -> ambiguous -> SetAlarm
        val alarmIntent = localRuntime.resolveAmbiguousIntent("wake me up at 7:30")
        assertTrue("Expected SetAlarm intent, got $alarmIntent", alarmIntent is StructuredIntent.SetAlarm)
    }

    @Test
    fun test4TierTaskRouterRoutingDiscipline() = runBlocking {
        // Tier 1: Obvious Android command -> deterministic parser -> tool (0ms, 0 tokens)
        val route1 = taskRouter.route("turn on flashlight")
        assertTrue("Expected DeterministicAction, got $route1", route1 is TaskRouteResult.DeterministicAction)
        assertEquals(StructuredIntent.Flashlight(true), (route1 as TaskRouteResult.DeterministicAction).intent)

        val route2 = taskRouter.route("set volume to 80%")
        assertTrue(route2 is TaskRouteResult.DeterministicAction)
        assertEquals(StructuredIntent.SetVolume(80), (route2 as TaskRouteResult.DeterministicAction).intent)

        // Tier 2: Ambiguous action phrase -> local model -> strict whitelist JSON intent
        val route3 = taskRouter.route("it's too bright in here")
        assertTrue("Expected AmbiguousActionCandidate, got $route3", route3 is TaskRouteResult.AmbiguousActionCandidate)

        // Tier 3: Normal private conversation -> local model
        val route4 = taskRouter.route("tell me how you protect my privacy offline")
        assertTrue("Expected LocalConversational, got $route4", route4 is TaskRouteResult.LocalConversational)

        // Tier 4: Complex/fresh query -> optional cloud fallback
        val route5 = taskRouter.route("who won the latest championship game?")
        assertTrue("Expected ComplexCloudQuery, got $route5", route5 is TaskRouteResult.ComplexCloudQuery)
    }

    @Test
    fun testCleanLocalModelRuntimeInterfacePolymorphism() {
        val customRuntime: ILocalModelRuntime = object : ILocalModelRuntime {
            override val status = localRuntime.status
            override val isInferring = localRuntime.isInferring
            override suspend fun generate(rawPrompt: String, contextHistory: List<Pair<String, String>>, timeoutMs: Long) = "Custom Embedded Runtime Response"
            override suspend fun generateStream(rawPrompt: String, contextHistory: List<Pair<String, String>>): Flow<String> = flowOf("Token1", "Token2")
            override suspend fun resolveAmbiguousIntent(rawPrompt: String, contextHistory: List<Pair<String, String>>) = StructuredIntent.Flashlight(true)
            override fun cancelInference() {}
            override fun loadProfile(profile: ModelProfile): Boolean = true
            override fun unloadModel() {}
            override fun reloadModel() {}
        }

        runBlocking {
            val response = customRuntime.generate("test prompt")
            assertEquals("Custom Embedded Runtime Response", response)
        }
    }
}
