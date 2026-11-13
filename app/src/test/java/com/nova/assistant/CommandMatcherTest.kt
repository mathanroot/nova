package com.nova.assistant

import com.nova.assistant.domain.intent.NovaIntent
import com.nova.assistant.domain.matcher.CommandMatcher
import org.junit.Assert.assertEquals
import org.junit.Test

class CommandMatcherTest {

    @Test
    fun testFlashlightCommands() {
        assertEquals(NovaIntent.FLASHLIGHT_ON, CommandMatcher.match("Hey Nova, turn on the flashlight").intent)
        assertEquals(NovaIntent.FLASHLIGHT_ON, CommandMatcher.match("switch on torch").intent)
        assertEquals(NovaIntent.FLASHLIGHT_OFF, CommandMatcher.match("turn off flashlight").intent)
        assertEquals(NovaIntent.FLASHLIGHT_OFF, CommandMatcher.match("switch off the torch").intent)
        assertEquals(NovaIntent.FLASHLIGHT_TOGGLE, CommandMatcher.match("toggle flashlight").intent)
    }

    @Test
    fun testBatteryCommands() {
        assertEquals(NovaIntent.BATTERY_CHECK, CommandMatcher.match("what is my battery percentage").intent)
        assertEquals(NovaIntent.BATTERY_CHECK, CommandMatcher.match("how much battery is left").intent)
        assertEquals(NovaIntent.CHARGING_CHECK, CommandMatcher.match("is my phone charging").intent)
    }

    @Test
    fun testLocationCommands() {
        assertEquals(NovaIntent.LOCATION_GET, CommandMatcher.match("where am i").intent)
        assertEquals(NovaIntent.LOCATION_GET, CommandMatcher.match("what are my coordinates").intent)
        assertEquals(NovaIntent.LOCATION_GET, CommandMatcher.match("get my gps location").intent)
        assertEquals(NovaIntent.LOCATION_MAPS, CommandMatcher.match("open in maps").intent)
    }

    @Test
    fun testVolumeCommands() {
        assertEquals(NovaIntent.VOLUME_INCREASE, CommandMatcher.match("increase volume").intent)
        assertEquals(NovaIntent.VOLUME_DECREASE, CommandMatcher.match("decrease volume").intent)
        assertEquals(NovaIntent.VOLUME_MUTE, CommandMatcher.match("mute volume").intent)
        assertEquals(NovaIntent.VOLUME_UNMUTE, CommandMatcher.match("unmute volume").intent)

        val setVol = CommandMatcher.match("set volume to 70 percent")
        assertEquals(NovaIntent.VOLUME_SET, setVol.intent)
        assertEquals(70, setVol.entities.percentage)
    }

    @Test
    fun testAppOpenCommands() {
        val whatsapp = CommandMatcher.match("open whatsapp")
        assertEquals(NovaIntent.WHATSAPP_OPEN, whatsapp.intent)

        val youtube = CommandMatcher.match("open youtube")
        assertEquals(NovaIntent.APP_OPEN, youtube.intent)
        assertEquals("youtube", youtube.entities.targetAppName)

        val chrome = CommandMatcher.match("open chrome")
        assertEquals(NovaIntent.APP_OPEN, chrome.intent)
        assertEquals("chrome", chrome.entities.targetAppName)
    }

    @Test
    fun testStorageAndDeviceInfo() {
        assertEquals(NovaIntent.STORAGE_CHECK, CommandMatcher.match("check available storage").intent)
        assertEquals(NovaIntent.STORAGE_CHECK, CommandMatcher.match("how much storage is remaining").intent)
        assertEquals(NovaIntent.DEVICE_INFO, CommandMatcher.match("what phone am i using").intent)
        assertEquals(NovaIntent.DEVICE_INFO, CommandMatcher.match("tell me my android version").intent)
    }

    @Test
    fun testConnectivityCommands() {
        assertEquals(NovaIntent.WIFI_STATUS, CommandMatcher.match("check wifi status").intent)
        assertEquals(NovaIntent.WIFI_SETTINGS, CommandMatcher.match("open wifi settings").intent)
        assertEquals(NovaIntent.BLUETOOTH_STATUS, CommandMatcher.match("check bluetooth status").intent)
        assertEquals(NovaIntent.BLUETOOTH_SETTINGS, CommandMatcher.match("open bluetooth settings").intent)
        assertEquals(NovaIntent.NETWORK_CHECK, CommandMatcher.match("am i connected to the internet").intent)
    }

    @Test
    fun testUnknownCommands() {
        assertEquals(NovaIntent.UNKNOWN, CommandMatcher.match("make me a sandwich").intent)
    }
}
