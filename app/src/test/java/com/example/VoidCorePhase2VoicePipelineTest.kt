package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.context.ConversationContextManager
import com.example.core.provider.AIProviderRegistry
import com.example.core.provider.AIProviderType
import com.example.core.security.SecureKeyStorage
import com.example.engine.local.TaskRouteResult
import com.example.engine.local.TaskRouter
import com.example.engine.security.StructuredIntent
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class VoidCorePhase2VoicePipelineTest {

    private lateinit var context: Context
    private lateinit var taskRouter: TaskRouter
    private lateinit var contextManager: ConversationContextManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        taskRouter = TaskRouter()
        contextManager = ConversationContextManager(maxTurns = 5)
    }

    @Test
    fun testTaskRouterDeterministicLocalActions() {
        // Flashlight
        val flashlightOn = taskRouter.route("turn on flashlight")
        assertTrue(flashlightOn is TaskRouteResult.DeterministicAction)
        val flashAction = flashlightOn as TaskRouteResult.DeterministicAction
        assertTrue((flashAction.intent as StructuredIntent.Flashlight).enabled)

        val flashlightOff = taskRouter.route("turn off flashlight")
        assertTrue(flashlightOff is TaskRouteResult.DeterministicAction)
        assertTrue(!((flashlightOff as TaskRouteResult.DeterministicAction).intent as StructuredIntent.Flashlight).enabled)

        // Volume
        val setVol = taskRouter.route("set volume to 85%")
        assertTrue(setVol is TaskRouteResult.DeterministicAction)
        assertEquals(85, ((setVol as TaskRouteResult.DeterministicAction).intent as StructuredIntent.SetVolume).percentage)

        val mute = taskRouter.route("mute audio")
        assertTrue(mute is TaskRouteResult.DeterministicAction)
        assertTrue(((mute as TaskRouteResult.DeterministicAction).intent as StructuredIntent.MuteVolume).muted)

        // Timer
        val timer = taskRouter.route("set timer for 10 minutes")
        assertTrue(timer is TaskRouteResult.DeterministicAction)
        assertEquals(600, ((timer as TaskRouteResult.DeterministicAction).intent as StructuredIntent.SetTimer).seconds)
    }

    @Test
    fun testTaskRouterConversationalFallback() {
        val query = taskRouter.route("Explain quantum computing in simple terms")
        assertTrue(query is TaskRouteResult.LocalConversational || query is TaskRouteResult.ComplexCloudQuery)
    }

    @Test
    fun testSecureKeyStorageOperations() {
        val storage = SecureKeyStorage.getInstance(context)
        val testKey = "sk-test-key-123456-voidcore-secure"

        storage.storeApiKey("openai", testKey)
        val retrieved = storage.getApiKey("openai")
        assertEquals(testKey, retrieved)
        assertTrue(storage.hasApiKey("openai"))

        storage.clearApiKey("openai")
        val cleared = storage.getApiKey("openai")
        assertEquals("", cleared)
    }

    @Test
    fun testAIProviderRegistryFailover() = runBlocking {
        val registry = AIProviderRegistry.getInstance(context)
        assertNotNull(registry)

        // Ensure default providers exist
        val providers = registry.providers.value
        assertTrue(providers.isNotEmpty())
        assertTrue(providers.any { it.type == AIProviderType.GEMINI })
        assertTrue(providers.any { it.type == AIProviderType.GROQ })
        assertTrue(providers.any { it.type == AIProviderType.LOCAL_FALLBACK })

        // Test failover execution: with no cloud keys configured in test, seamlessly falls back to on-device engine
        val response = registry.generateWithFailover(
            prompt = "Status check",
            contextHistory = listOf("user" to "hello", "assistant" to "Ready.")
        )
        assertTrue(response.isSuccess)
        assertTrue(response.text.isNotBlank())
    }

    @Test
    fun testConversationContextManagerWindow() {
        contextManager.recordTurn("user", "Hello VoidCore")
        contextManager.recordTurn("assistant", "Greetings. Ready.")
        contextManager.recordTurn("user", "Check status")
        contextManager.recordTurn("assistant", "All systems nominal.")
        contextManager.recordTurn("user", "Dim brightness")
        contextManager.recordTurn("assistant", "Brightness adjusted.")

        val history = contextManager.getFormattedHistory()
        assertTrue(history.size <= 5) // Adheres to window constraint
        assertEquals("Brightness adjusted.", history.last().second)
    }
}
