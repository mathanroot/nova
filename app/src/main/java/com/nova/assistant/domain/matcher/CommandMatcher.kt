package com.nova.assistant.domain.matcher

import com.nova.assistant.domain.entity.EntityExtractor
import com.nova.assistant.domain.entity.ExtractedEntities
import com.nova.assistant.domain.intent.IntentMatchResult
import com.nova.assistant.domain.intent.NovaIntent
import java.util.Locale

object CommandMatcher {

    fun match(input: String): IntentMatchResult {
        val text = cleanInput(input)
        if (text.isEmpty()) {
            return IntentMatchResult(NovaIntent.UNKNOWN, 0.0f, "")
        }

        // 1. Flashlight
        if (isFlashlightOn(text)) {
            return IntentMatchResult(NovaIntent.FLASHLIGHT_ON, 1.0f, text)
        }
        if (isFlashlightOff(text)) {
            return IntentMatchResult(NovaIntent.FLASHLIGHT_OFF, 1.0f, text)
        }
        if (isFlashlightToggle(text)) {
            return IntentMatchResult(NovaIntent.FLASHLIGHT_TOGGLE, 0.9f, text)
        }

        // 2. Battery & Charging
        if (isBatteryCheck(text)) {
            return IntentMatchResult(NovaIntent.BATTERY_CHECK, 1.0f, text)
        }
        if (isChargingCheck(text)) {
            return IntentMatchResult(NovaIntent.CHARGING_CHECK, 1.0f, text)
        }

        // 3. GPS & Location
        if (isLocationMaps(text)) {
            return IntentMatchResult(NovaIntent.LOCATION_MAPS, 1.0f, text)
        }
        if (isLocationGet(text)) {
            return IntentMatchResult(NovaIntent.LOCATION_GET, 1.0f, text)
        }

        // 4. Volume
        if (isVolumeMute(text)) {
            return IntentMatchResult(NovaIntent.VOLUME_MUTE, 1.0f, text)
        }
        if (isVolumeUnmute(text)) {
            return IntentMatchResult(NovaIntent.VOLUME_UNMUTE, 1.0f, text)
        }
        if (isVolumeSet(text)) {
            val pct = EntityExtractor.extractPercentage(text)
            return IntentMatchResult(
                NovaIntent.VOLUME_SET, 1.0f, text,
                ExtractedEntities(percentage = pct)
            )
        }
        if (isVolumeIncrease(text)) {
            return IntentMatchResult(NovaIntent.VOLUME_INCREASE, 1.0f, text)
        }
        if (isVolumeDecrease(text)) {
            return IntentMatchResult(NovaIntent.VOLUME_DECREASE, 1.0f, text)
        }

        // 5. Brightness
        if (isBrightnessSet(text)) {
            val pct = EntityExtractor.extractPercentage(text)
            return IntentMatchResult(
                NovaIntent.BRIGHTNESS_SET, 1.0f, text,
                ExtractedEntities(percentage = pct)
            )
        }
        if (isBrightnessIncrease(text)) {
            return IntentMatchResult(NovaIntent.BRIGHTNESS_INCREASE, 1.0f, text)
        }
        if (isBrightnessDecrease(text)) {
            return IntentMatchResult(NovaIntent.BRIGHTNESS_DECREASE, 1.0f, text)
        }

        // 6. Wi-Fi
        if (isWifiSettings(text)) {
            return IntentMatchResult(NovaIntent.WIFI_SETTINGS, 1.0f, text)
        }
        if (isWifiStatus(text)) {
            return IntentMatchResult(NovaIntent.WIFI_STATUS, 1.0f, text)
        }

        // 7. Bluetooth
        if (isBluetoothSettings(text)) {
            return IntentMatchResult(NovaIntent.BLUETOOTH_SETTINGS, 1.0f, text)
        }
        if (isBluetoothStatus(text)) {
            return IntentMatchResult(NovaIntent.BLUETOOTH_STATUS, 1.0f, text)
        }

        // 8. Storage
        if (isStorageCheck(text)) {
            return IntentMatchResult(NovaIntent.STORAGE_CHECK, 1.0f, text)
        }

        // 9. Network
        if (isNetworkCheck(text)) {
            return IntentMatchResult(NovaIntent.NETWORK_CHECK, 1.0f, text)
        }

        // 10. Device Info
        if (isDeviceInfo(text)) {
            return IntentMatchResult(NovaIntent.DEVICE_INFO, 1.0f, text)
        }

        // 11. Time & Date
        if (isTimeGet(text)) {
            return IntentMatchResult(NovaIntent.TIME_GET, 1.0f, text)
        }
        if (isDateGet(text)) {
            return IntentMatchResult(NovaIntent.DATE_GET, 1.0f, text)
        }

        // 12. Unit Conversion
        val unitResult = EntityExtractor.extractUnitConversion(text)
        if (unitResult != null) {
            return IntentMatchResult(NovaIntent.UNIT_CONVERT, 1.0f, text, unitResult)
        }

        // 13. Math & Calculation
        val mathResult = EntityExtractor.extractMath(text)
        if (mathResult != null) {
            return IntentMatchResult(
                NovaIntent.CALCULATE, 1.0f, text,
                ExtractedEntities(mathExpression = mathResult.first, mathResult = mathResult.second)
            )
        }

        // 14. Alarms & Timers
        val timerSec = EntityExtractor.extractTimerSeconds(text)
        if (timerSec != null) {
            return IntentMatchResult(
                NovaIntent.TIMER_SET, 1.0f, text,
                ExtractedEntities(durationSeconds = timerSec)
            )
        }
        val alarmTime = EntityExtractor.extractAlarmTime(text)
        if (alarmTime != null) {
            return IntentMatchResult(
                NovaIntent.ALARM_SET, 1.0f, text,
                ExtractedEntities(timeHour = alarmTime.first, timeMinute = alarmTime.second, isPm = alarmTime.third)
            )
        }
        if (isReminder(text)) {
            val reminderText = text.replace(Regex("^(remind me to|set a reminder to)\\s+", RegexOption.IGNORE_CASE), "").trim()
            return IntentMatchResult(
                NovaIntent.REMINDER_SET, 1.0f, text,
                ExtractedEntities(messageBody = reminderText)
            )
        }

        // 15. Camera & Gallery
        if (isCameraTakePhoto(text)) {
            return IntentMatchResult(NovaIntent.CAMERA_TAKE_PHOTO, 1.0f, text)
        }
        if (isCameraRecordVideo(text)) {
            return IntentMatchResult(NovaIntent.CAMERA_RECORD_VIDEO, 1.0f, text)
        }
        if (isCameraOpen(text)) {
            return IntentMatchResult(NovaIntent.CAMERA_OPEN, 1.0f, text)
        }
        if (isGalleryOpen(text)) {
            return IntentMatchResult(NovaIntent.GALLERY_OPEN, 1.0f, text)
        }

        // 16. Media Playback
        if (isMediaNext(text)) {
            return IntentMatchResult(NovaIntent.MEDIA_NEXT, 1.0f, text)
        }
        if (isMediaPrevious(text)) {
            return IntentMatchResult(NovaIntent.MEDIA_PREVIOUS, 1.0f, text)
        }
        if (isMediaPause(text)) {
            return IntentMatchResult(NovaIntent.MEDIA_PAUSE, 1.0f, text)
        }
        if (isMediaPlay(text)) {
            return IntentMatchResult(NovaIntent.MEDIA_PLAY, 1.0f, text)
        }

        // 17. Phone Call & SMS
        val phoneNum = EntityExtractor.extractPhoneNumber(text)
        if (phoneNum != null && (text.contains("dial") || text.contains("call"))) {
            return IntentMatchResult(
                NovaIntent.PHONE_DIAL, 1.0f, text,
                ExtractedEntities(phoneNumber = phoneNum)
            )
        }
        val (contact, msg) = EntityExtractor.extractContactAndMessage(text)
        if (text.startsWith("call ") || text.startsWith("dial ")) {
            return IntentMatchResult(
                NovaIntent.PHONE_CALL, 0.95f, text,
                ExtractedEntities(contactName = contact ?: text.removePrefix("call ").removePrefix("dial ").trim())
            )
        }
        if (text.startsWith("send ") || text.startsWith("message ") || text.startsWith("sms ")) {
            return IntentMatchResult(
                NovaIntent.SMS_SEND, 0.95f, text,
                ExtractedEntities(contactName = contact, messageBody = msg)
            )
        }

        // 18. Application Opening
        val appName = EntityExtractor.extractAppName(text)
        if (appName != null) {
            if (appName.equals("whatsapp", ignoreCase = true)) {
                return IntentMatchResult(NovaIntent.WHATSAPP_OPEN, 1.0f, text, ExtractedEntities(targetAppName = "whatsapp"))
            }
            return IntentMatchResult(NovaIntent.APP_OPEN, 0.9f, text, ExtractedEntities(targetAppName = appName))
        }

        return IntentMatchResult(NovaIntent.UNKNOWN, 0.0f, text)
    }

    private fun cleanInput(input: String): String {
        return input.trim().lowercase(Locale.ROOT)
            .replace(Regex("^(hey|ok|okay|hi|hello)?\\s*nova[,\\s]*"), "")
            .replace(Regex("[?!.]+$"), "")
            .trim()
    }

    private fun isFlashlightOn(t: String) =
        t == "turn on flashlight" || t == "turn on the flashlight" ||
        t == "switch on flashlight" || t == "switch on the flashlight" ||
        t == "turn on torch" || t == "turn on the torch" ||
        t == "switch on torch" || t == "switch on the torch" ||
        t == "flashlight on" || t == "torch on"

    private fun isFlashlightOff(t: String) =
        t == "turn off flashlight" || t == "turn off the flashlight" ||
        t == "switch off flashlight" || t == "switch off the flashlight" ||
        t == "turn off torch" || t == "turn off the torch" ||
        t == "switch off torch" || t == "switch off the torch" ||
        t == "flashlight off" || t == "torch off"

    private fun isFlashlightToggle(t: String) =
        t == "toggle flashlight" || t == "toggle torch" || t == "flashlight" || t == "torch"

    private fun isBatteryCheck(t: String) =
        t.contains("battery") && (t.contains("percentage") || t.contains("how much") || t.contains("left") || t.contains("level") || t.contains("status") || t.contains("check"))

    private fun isChargingCheck(t: String) =
        t.contains("charging") || (t.contains("phone") && t.contains("plugged in"))

    private fun isLocationMaps(t: String) =
        t.contains("map") || t.contains("open in maps") || t.contains("show on map")

    private fun isLocationGet(t: String) =
        t == "where am i" || t.contains("my location") || t.contains("current location") ||
        t.contains("coordinates") || t.contains("gps location") || t.contains("my coordinates")

    private fun isVolumeIncrease(t: String) =
        t.contains("increase volume") || t.contains("raise volume") || t.contains("volume up") || t == "louder"

    private fun isVolumeDecrease(t: String) =
        t.contains("decrease volume") || t.contains("lower volume") || t.contains("volume down") || t == "quieter"

    private fun isVolumeSet(t: String) =
        t.startsWith("set volume") || (t.contains("volume") && (t.contains("percent") || t.contains("%")))

    private fun isVolumeMute(t: String) =
        t == "mute" || t == "mute volume" || t == "mute audio" || t == "silence phone"

    private fun isVolumeUnmute(t: String) =
        t == "unmute" || t == "unmute volume" || t == "unmute audio"

    private fun isBrightnessIncrease(t: String) =
        t.contains("increase brightness") || t.contains("raise brightness") || t.contains("brighter")

    private fun isBrightnessDecrease(t: String) =
        t.contains("decrease brightness") || t.contains("lower brightness") || t.contains("dimmer")

    private fun isBrightnessSet(t: String) =
        t.startsWith("set brightness") || (t.contains("brightness") && (t.contains("percent") || t.contains("%")))

    private fun isWifiSettings(t: String) =
        t.contains("open wifi settings") || t.contains("open wi-fi settings") || t == "wifi settings"

    private fun isWifiStatus(t: String) =
        t.contains("wifi") || t.contains("wi-fi")

    private fun isBluetoothSettings(t: String) =
        t.contains("open bluetooth settings") || t == "bluetooth settings"

    private fun isBluetoothStatus(t: String) =
        t.contains("bluetooth")

    private fun isStorageCheck(t: String) =
        t.contains("storage") || t.contains("disk space") || t.contains("memory left")

    private fun isNetworkCheck(t: String) =
        t.contains("connected to the internet") || t.contains("network status") || t.contains("internet connection") || t == "am i online"

    private fun isDeviceInfo(t: String) =
        t.contains("what phone") || t.contains("device info") || t.contains("device information") || t.contains("android version")

    private fun isTimeGet(t: String) =
        t == "what time is it" || t == "tell me the time" || t == "time" || t == "current time"

    private fun isDateGet(t: String) =
        t == "what is today's date" || t == "what is the date" || t == "today's date" || t == "what date is it" || t == "tell me the date"

    private fun isReminder(t: String) =
        t.startsWith("remind me") || t.startsWith("set a reminder")

    private fun isCameraTakePhoto(t: String) =
        t.contains("take a photo") || t.contains("take photo") || t.contains("capture photo") || t.contains("take picture")

    private fun isCameraRecordVideo(t: String) =
        t.contains("record video") || t.contains("start video recording")

    private fun isCameraOpen(t: String) =
        t == "open camera" || t == "start camera" || t == "launch camera"

    private fun isGalleryOpen(t: String) =
        t == "open gallery" || t == "open photos" || t == "view photos"

    private fun isMediaPlay(t: String) =
        t == "play music" || t == "play" || t == "resume music" || t == "resume playback"

    private fun isMediaPause(t: String) =
        t == "pause music" || t == "pause" || t == "stop music"

    private fun isMediaNext(t: String) =
        t == "next song" || t == "skip song" || t == "next track"

    private fun isMediaPrevious(t: String) =
        t == "previous song" || t == "previous track"
}
