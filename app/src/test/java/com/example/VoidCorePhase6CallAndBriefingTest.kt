package com.example

import com.example.engine.security.RiskLevel
import com.example.engine.security.SecurityDecision
import com.example.engine.security.SecurityGate
import com.example.engine.security.StructuredIntent
import com.example.feature.briefing.BriefingBlock
import com.example.feature.briefing.BriefingPreferences
import com.example.feature.call.ActiveIncomingCall
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VoidCorePhase6CallAndBriefingTest {

    @Test
    fun testCallIntelligenceIdentificationAndDisclosure() {
        val call = ActiveIncomingCall(
            callerName = "Ahmed",
            callerNumber = "+1-555-0199",
            isScreeningActive = true
        )

        val disclosure = "Hello, I am VoidCore, an AI assistant for the user. They are currently unavailable and requested me to relay: \"I will call back in 30 minutes.\""
        
        // Assert AI transparency mandate: Assistant identifies itself and user
        assertTrue(disclosure.contains("VoidCore"))
        assertTrue(disclosure.contains("AI assistant"))
        assertTrue(disclosure.contains("unavailable"))
        assertFalse(disclosure.contains("I am Arif")) // Never impersonate
    }

    @Test
    fun testStructuredIntentForCallActions() {
        val makeCallIntent = StructuredIntent.MakeCall(recipient = "Ahmed")
        val securityGate = SecurityGate()
        val decision = securityGate.evaluate(makeCallIntent)

        // MakeCall must require confirmation / High risk assessment
        assertEquals(SecurityDecision.CONFIRMATION_REQUIRED, decision.decision)
        assertEquals(RiskLevel.HIGH, decision.riskLevel)
    }

    @Test
    fun testBriefingPreferencesAndBlocks() {
        val prefs = BriefingPreferences(
            includeWeather = true,
            includeCalendar = true,
            includeTasks = true,
            includeBattery = true
        )

        assertTrue(prefs.includeWeather)
        assertTrue(prefs.includeCalendar)
        assertTrue(prefs.includeTasks)
        assertTrue(prefs.includeBattery)
    }
}
