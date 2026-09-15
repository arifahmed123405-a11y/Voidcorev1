package com.example.engine.local

import com.example.engine.security.StructuredIntent

class DeterministicCommandParser {

    fun tryParse(rawInput: String): StructuredIntent? {
        val trimmedInput = rawInput.trim()
        val input = trimmedInput.lowercase()

        // 1. Flashlight / Torch
        if (input.contains("flashlight on") || input.contains("turn on flashlight") || input.contains("torch on") || input.contains("turn on torch") || input == "flashlight" || input == "torch") {
            return StructuredIntent.Flashlight(true)
        }
        if (input.contains("flashlight off") || input.contains("turn off flashlight") || input.contains("torch off") || input.contains("turn off torch")) {
            return StructuredIntent.Flashlight(false)
        }

        // 2. Volume Modulation & Mute
        val volumeMatch = Regex("""(?:set\s+)?volume\s+(?:to\s+)?(\d{1,3})%?""").find(input)
        if (volumeMatch != null) {
            val pct = volumeMatch.groupValues[1].toIntOrNull()?.coerceIn(0, 100) ?: 50
            return StructuredIntent.SetVolume(pct)
        }
        if (input == "mute" || input.contains("mute audio") || input.contains("mute volume") || input.contains("silence media")) {
            return StructuredIntent.MuteVolume(true)
        }
        if (input == "unmute" || input.contains("unmute audio") || input.contains("unmute volume")) {
            return StructuredIntent.MuteVolume(false)
        }
        if (input.contains("volume up") || input.contains("increase volume") || input.contains("turn up volume") || input.contains("louder")) {
            return StructuredIntent.SetVolume(80)
        }
        if (input.contains("volume down") || input.contains("lower volume") || input.contains("turn down volume") || input.contains("quieter")) {
            return StructuredIntent.SetVolume(30)
        }

        // 3. Brightness Modulation
        val brightnessMatch = Regex("""(?:set\s+)?brightness\s+(?:to\s+)?(\d{1,3})%?""").find(input)
        if (brightnessMatch != null) {
            val pct = brightnessMatch.groupValues[1].toIntOrNull()?.coerceIn(0, 100) ?: 50
            return StructuredIntent.SetBrightness(pct)
        }
        if (input.contains("brightness up") || input.contains("increase brightness") || input.contains("make screen brighter") || input.contains("brighter")) {
            return StructuredIntent.SetBrightness(85)
        }
        if (input.contains("brightness down") || input.contains("dim screen") || input.contains("dim display") || input.contains("lower brightness") || input.contains("dimmer")) {
            return StructuredIntent.SetBrightness(20)
        }

        // 4. Countdown Timer
        val timerMinMatch = Regex("""(?:set\s+(?:a\s+)?)?timer\s+(?:for\s+)?(\d+)\s*(?:min|minute|minutes)""").find(input)
        if (timerMinMatch != null) {
            val mins = timerMinMatch.groupValues[1].toIntOrNull() ?: 5
            return StructuredIntent.SetTimer(mins * 60, "Timer ($mins min)")
        }
        val timerSecMatch = Regex("""(?:set\s+(?:a\s+)?)?timer\s+(?:for\s+)?(\d+)\s*(?:sec|second|seconds)""").find(input)
        if (timerSecMatch != null) {
            val secs = timerSecMatch.groupValues[1].toIntOrNull() ?: 30
            return StructuredIntent.SetTimer(secs, "Timer ($secs sec)")
        }
        val timerHourMatch = Regex("""(?:set\s+(?:a\s+)?)?timer\s+(?:for\s+)?(\d+)\s*(?:hour|hours|hr|hrs)""").find(input)
        if (timerHourMatch != null) {
            val hrs = timerHourMatch.groupValues[1].toIntOrNull() ?: 1
            return StructuredIntent.SetTimer(hrs * 3600, "Timer ($hrs hr)")
        }

        // 5. Clock Alarm
        val alarmMatch = Regex("""(?:set\s+(?:an\s+)?)?alarm\s+(?:for\s+)?(\d{1,2})(?::(\d{2}))?\s*(am|pm)?""").find(input)
        if (alarmMatch != null) {
            var hour = alarmMatch.groupValues[1].toIntOrNull() ?: 6
            val min = alarmMatch.groupValues[2].toIntOrNull() ?: 0
            val ampm = alarmMatch.groupValues[3]
            if (ampm == "pm" && hour < 12) hour += 12
            if (ampm == "am" && hour == 12) hour = 0
            return StructuredIntent.SetAlarm(hour, min, "Morning Alarm")
        }

        // 6. Media Playback Controls
        if (input == "play" || input.contains("play music") || input.contains("resume playback") || input == "resume") {
            return StructuredIntent.MediaControl("PLAY")
        }
        if (input == "pause" || input.contains("pause music") || input.contains("pause playback") || input == "pause track") {
            return StructuredIntent.MediaControl("PAUSE")
        }
        if (input.contains("next song") || input.contains("skip track") || input.contains("next track") || input == "next" || input == "skip") {
            return StructuredIntent.MediaControl("NEXT")
        }
        if (input.contains("previous song") || input.contains("previous track") || input == "previous" || input == "prev") {
            return StructuredIntent.MediaControl("PREV")
        }
        if (input == "stop" || input.contains("stop music") || input.contains("stop playback")) {
            return StructuredIntent.MediaControl("STOP")
        }

        // 7. Clipboard Operations
        if (input.contains("read clipboard") || input.contains("what's in my clipboard") || input.contains("clipboard content") || input.contains("get clipboard")) {
            return StructuredIntent.ReadClipboard(previewOnly = true)
        }
        val copyMatch = Regex("""(?i)(?:copy|write\s+to\s+clipboard)\s+(.+)""").find(trimmedInput)
        if (copyMatch != null) {
            return StructuredIntent.WriteClipboard(copyMatch.groupValues[1].trim())
        }

        // 8. Navigation & Accessibility Controls
        if (input == "go back" || input == "back" || input == "navigate back") {
            return StructuredIntent.NavigateBack
        }
        if (input == "go home" || input == "home screen" || input == "open home" || input == "home") {
            return StructuredIntent.NavigateHome
        }
        if (input.contains("scroll down") || input == "scroll down") {
            return StructuredIntent.ScrollScreen("DOWN")
        }
        if (input.contains("scroll up") || input == "scroll up") {
            return StructuredIntent.ScrollScreen("UP")
        }
        val tapMatch = Regex("""(?i)(?:tap|click|press)\s+(?:on\s+)?(.+)""").find(trimmedInput)
        if (tapMatch != null) {
            val target = tapMatch.groupValues[1].trim()
            return StructuredIntent.TapTarget(target)
        }
        val typeMatch = Regex("""(?i)(?:type|enter\s+text|input)\s+(.+)""").find(trimmedInput)
        if (typeMatch != null) {
            return StructuredIntent.TypeText(typeMatch.groupValues[1].trim())
        }

        // 9. System Settings Navigation
        if (input.contains("accessibility settings") || input.contains("open accessibility")) {
            return StructuredIntent.OpenSettings("ACCESSIBILITY")
        }
        if (input.contains("notification settings") || input.contains("notification access") || input.contains("notification listener")) {
            return StructuredIntent.OpenSettings("NOTIFICATION_LISTENER")
        }
        if (input.contains("write settings") || input.contains("modify system settings")) {
            return StructuredIntent.OpenSettings("WRITE_SETTINGS")
        }
        if (input.contains("wifi settings") || input.contains("open wifi")) {
            return StructuredIntent.OpenSettings("WIFI")
        }
        if (input.contains("bluetooth settings") || input.contains("open bluetooth")) {
            return StructuredIntent.OpenSettings("BLUETOOTH")
        }
        if (input.contains("open settings") || input.contains("go to settings") || input.contains("app settings")) {
            return StructuredIntent.OpenSettings("APPLICATION_DETAILS")
        }

        // 10. App Launch
        val openAppMatch = Regex("""(?i)(?:open|launch|start)\s+([a-zA-Z0-9\s._]+)""").find(trimmedInput)
        if (openAppMatch != null) {
            val target = openAppMatch.groupValues[1].trim()
            if (!target.lowercase().contains("settings") && target.isNotEmpty()) {
                return StructuredIntent.LaunchApp(target)
            }
        }

        // 11. Search Files
        val fileMatch = Regex("""(?i)(?:search|find)\s+(?:files?|documents?|pdf)\s*(?:for\s+)?(.+)""").find(trimmedInput)
        if (fileMatch != null) {
            return StructuredIntent.SearchFiles(fileMatch.groupValues[1].trim())
        }

        return null
    }
}
