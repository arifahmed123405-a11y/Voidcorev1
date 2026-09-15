package com.example

import android.app.Notification
import android.os.Bundle
import android.service.notification.StatusBarNotification
import androidx.test.core.app.ApplicationProvider
import com.example.engine.local.DeterministicCommandParser
import com.example.engine.local.TaskRouteResult
import com.example.engine.local.TaskRouter
import com.example.engine.security.RiskLevel
import com.example.engine.security.SecurityDecision
import com.example.engine.security.SecurityGate
import com.example.engine.security.StructuredIntent
import com.example.feature.notifications.ImportanceLevel
import com.example.feature.notifications.NotificationClassifier
import com.example.feature.notifications.NotificationContextEngine
import com.example.feature.notifications.NotificationModel
import com.example.feature.notifications.NotificationType
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
class VoidCorePhase3And4Test {

    private lateinit var parser: DeterministicCommandParser
    private lateinit var securityGate: SecurityGate
    private lateinit var taskRouter: TaskRouter
    private lateinit var notificationEngine: NotificationContextEngine

    @Before
    fun setUp() {
        parser = DeterministicCommandParser()
        securityGate = SecurityGate()
        taskRouter = TaskRouter(parser)
        notificationEngine = NotificationContextEngine.getInstance(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun testFlashlightCommands() {
        val onResult = parser.tryParse("turn on flashlight")
        assertTrue(onResult is StructuredIntent.Flashlight && onResult.enabled)

        val offResult = parser.tryParse("turn off torch")
        assertTrue(offResult is StructuredIntent.Flashlight && !offResult.enabled)

        val gateResult = securityGate.evaluate(onResult!!)
        assertEquals(SecurityDecision.APPROVED, gateResult.decision)
        assertEquals(RiskLevel.LOW, gateResult.riskLevel)
    }

    @Test
    fun testBrightnessAndVolumeCommands() {
        val bright = parser.tryParse("set brightness to 75%")
        assertTrue(bright is StructuredIntent.SetBrightness && bright.percentage == 75)

        val dim = parser.tryParse("dim screen")
        assertTrue(dim is StructuredIntent.SetBrightness && dim.percentage == 20)

        val vol = parser.tryParse("volume 60%")
        assertTrue(vol is StructuredIntent.SetVolume && vol.percentage == 60)

        val mute = parser.tryParse("mute audio")
        assertTrue(mute is StructuredIntent.MuteVolume && mute.muted)
    }

    @Test
    fun testTimerAndAlarmCommands() {
        val timer = parser.tryParse("set timer for 10 minutes")
        assertTrue(timer is StructuredIntent.SetTimer && timer.seconds == 600)

        val alarm = parser.tryParse("set alarm for 7:30 am")
        assertTrue(alarm is StructuredIntent.SetAlarm && alarm.hour == 7 && alarm.minute == 30)
    }

    @Test
    fun testMediaAndClipboardCommands() {
        val play = parser.tryParse("play music")
        assertTrue(play is StructuredIntent.MediaControl && play.command == "PLAY")

        val skip = parser.tryParse("skip track")
        assertTrue(skip is StructuredIntent.MediaControl && skip.command == "NEXT")

        val copy = parser.tryParse("copy meeting notes link")
        assertTrue(copy is StructuredIntent.WriteClipboard && copy.text == "meeting notes link")

        val read = parser.tryParse("what's in my clipboard")
        assertTrue(read is StructuredIntent.ReadClipboard)
    }

    @Test
    fun testAccessibilityAndSettingsCommands() {
        val back = parser.tryParse("go back")
        assertTrue(back is StructuredIntent.NavigateBack)

        val home = parser.tryParse("home screen")
        assertTrue(home is StructuredIntent.NavigateHome)

        val scroll = parser.tryParse("scroll down")
        assertTrue(scroll is StructuredIntent.ScrollScreen && scroll.direction == "DOWN")

        val tap = parser.tryParse("tap Submit Order")
        assertTrue(tap is StructuredIntent.TapTarget && tap.label == "Submit Order")

        val openSettings = parser.tryParse("open accessibility settings")
        assertTrue(openSettings is StructuredIntent.OpenSettings && openSettings.settingType == "ACCESSIBILITY")
    }

    @Test
    fun testSecurityGateProtectedDomains() {
        val normalTap = StructuredIntent.TapTarget("Submit Order")
        val normalEval = securityGate.evaluate(normalTap)
        assertEquals(SecurityDecision.APPROVED, normalEval.decision)

        val bankingTap = StructuredIntent.TapTarget("Transfer Money in Bank App")
        val bankingEval = securityGate.evaluate(bankingTap)
        assertEquals(SecurityDecision.CONFIRMATION_REQUIRED, bankingEval.decision)
        assertEquals(RiskLevel.HIGH, bankingEval.riskLevel)
    }

    @Test
    fun testTaskRouterDeterministicBypass() {
        val result = taskRouter.route("turn on flashlight")
        assertTrue(result is TaskRouteResult.DeterministicAction)

        val conversational = taskRouter.route("Explain quantum physics to me")
        assertTrue(conversational is TaskRouteResult.LocalConversational || conversational is TaskRouteResult.ComplexCloudQuery)
    }

    @Test
    fun testNotificationFollowUpEngine() {
        notificationEngine.loadDefaultSampleData()

        // Test "Who messaged me?"
        val whoResult = notificationEngine.processFollowUpQuery("Who messaged me?")
        assertTrue(whoResult.answered)
        assertTrue(whoResult.responseText.contains("Alex Chen") || whoResult.responseText.contains("WhatsApp"))

        // Test "What did he say?"
        val whatResult = notificationEngine.processFollowUpQuery("What did he say?")
        assertTrue(whatResult.answered)
        assertTrue(whatResult.responseText.contains("sprint planning"))

        // Test "Was it a voice note?" with voice sample
        val voiceQueryResult = notificationEngine.processFollowUpQuery("Was the message from Marcus a voice note?")
        assertTrue(voiceQueryResult.answered)
        assertTrue(voiceQueryResult.responseText.contains("Yes") || voiceQueryResult.responseText.contains("voice note"))

        // Test "Tell him I'll talk to you later"
        val replyResult = notificationEngine.processFollowUpQuery("Tell Alex I'll talk to you later")
        assertTrue(replyResult.answered)
        assertTrue(replyResult.responseText.contains("Sent reply"))
    }
}
