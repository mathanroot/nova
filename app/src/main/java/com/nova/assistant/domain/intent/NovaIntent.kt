package com.nova.assistant.domain.intent

/**
 * All local intents supported by NOVA's deterministic offline command engine.
 */
enum class NovaIntent(val description: String) {
    // Flashlight
    FLASHLIGHT_ON("Turn on torch/flashlight"),
    FLASHLIGHT_OFF("Turn off torch/flashlight"),
    FLASHLIGHT_TOGGLE("Toggle flashlight"),

    // Volume
    VOLUME_INCREASE("Increase audio volume"),
    VOLUME_DECREASE("Decrease audio volume"),
    VOLUME_SET("Set specific volume level"),
    VOLUME_MUTE("Mute device volume"),
    VOLUME_UNMUTE("Unmute device volume"),

    // Brightness
    BRIGHTNESS_INCREASE("Increase screen brightness"),
    BRIGHTNESS_DECREASE("Decrease screen brightness"),
    BRIGHTNESS_SET("Set specific brightness level"),

    // Battery & Hardware
    BATTERY_CHECK("Check battery percentage and health"),
    CHARGING_CHECK("Check if device is charging"),
    STORAGE_CHECK("Check remaining device storage"),
    DEVICE_INFO("Get phone model and Android version"),

    // Connectivity
    WIFI_STATUS("Check Wi-Fi connection status"),
    WIFI_SETTINGS("Open Wi-Fi settings"),
    BLUETOOTH_STATUS("Check Bluetooth status"),
    BLUETOOTH_SETTINGS("Open Bluetooth settings"),
    NETWORK_CHECK("Check internet connectivity"),

    // GPS & Location
    LOCATION_GET("Get current GPS coordinates"),
    LOCATION_MAPS("Open current location in Maps"),

    // Applications
    APP_OPEN("Open an installed application"),

    // Phone & Communication
    PHONE_CALL("Call a contact"),
    PHONE_DIAL("Open dialer with number"),
    SMS_SEND("Compose an SMS text message"),
    WHATSAPP_OPEN("Open WhatsApp application"),
    WHATSAPP_MESSAGE("Message someone on WhatsApp"),

    // Camera & Media
    CAMERA_OPEN("Open camera"),
    CAMERA_TAKE_PHOTO("Take a photo with camera"),
    CAMERA_RECORD_VIDEO("Record video"),
    GALLERY_OPEN("Open photos gallery"),
    MEDIA_PLAY("Resume media playback"),
    MEDIA_PAUSE("Pause media playback"),
    MEDIA_NEXT("Skip to next track"),
    MEDIA_PREVIOUS("Go to previous track"),

    // Alarms, Timers, Reminders
    ALARM_SET("Set a morning/evening alarm"),
    TIMER_SET("Set a countdown timer"),
    REMINDER_SET("Set a notification reminder"),

    // Utilities
    TIME_GET("Tell the current time"),
    DATE_GET("Tell today's date"),
    CALCULATE("Perform mathematical calculation"),
    UNIT_CONVERT("Convert units of measurement"),

    // Fallback
    UNKNOWN("Command not recognized")
}
