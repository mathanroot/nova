package com.nova.assistant.domain.executor

import android.content.Context
import com.nova.assistant.apps.communication.PhoneMessagingHelper
import com.nova.assistant.apps.contacts.ContactsHelper
import com.nova.assistant.apps.launcher.AppLauncher
import com.nova.assistant.device.alarm.AlarmTimerController
import com.nova.assistant.device.battery.BatteryController
import com.nova.assistant.device.brightness.BrightnessController
import com.nova.assistant.device.connectivity.ConnectivityController
import com.nova.assistant.device.deviceinfo.DeviceInfoController
import com.nova.assistant.device.flashlight.FlashlightController
import com.nova.assistant.device.location.LocationController
import com.nova.assistant.device.media.MediaPlaybackController
import com.nova.assistant.device.storage.StorageController
import com.nova.assistant.device.volume.VolumeController
import com.nova.assistant.domain.context.ConversationContext
import com.nova.assistant.domain.intent.NovaIntent
import com.nova.assistant.domain.parser.ParsedCommand
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommandExecutor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val flashlightController: FlashlightController,
    private val volumeController: VolumeController,
    private val brightnessController: BrightnessController,
    private val batteryController: BatteryController,
    private val connectivityController: ConnectivityController,
    private val storageController: StorageController,
    private val deviceInfoController: DeviceInfoController,
    private val locationController: LocationController,
    private val mediaPlaybackController: MediaPlaybackController,
    private val alarmTimerController: AlarmTimerController,
    private val appLauncher: AppLauncher,
    private val contactsHelper: ContactsHelper,
    private val phoneMessagingHelper: PhoneMessagingHelper,
    private val conversationContext: ConversationContext
) {

    suspend fun executeCommands(commands: List<ParsedCommand>): Pair<Boolean, String> {
        if (commands.isEmpty()) {
            return Pair(false, "I didn't hear a command. Please try again.")
        }

        val results = mutableListOf<ExecutionResult>()
        for (cmd in commands) {
            val res = executeSingleCommand(cmd)
            results.add(res)

            // Update conversation context
            conversationContext.updateContext(
                intent = res.intent,
                query = cmd.rawText,
                response = res.spokenResponse,
                targetApp = cmd.matchResult.entities.targetAppName,
                targetContact = cmd.matchResult.entities.contactName
            )
        }

        val allSuccess = results.all { it.isSuccess }
        val combinedResponse = buildCombinedResponse(results)
        return Pair(allSuccess, combinedResponse)
    }

    private suspend fun executeSingleCommand(cmd: ParsedCommand): ExecutionResult {
        val intent = cmd.matchResult.intent
        val entities = cmd.matchResult.entities

        return when (intent) {
            NovaIntent.FLASHLIGHT_ON -> {
                val res = flashlightController.setFlashlight(true)
                if (res.isSuccess) {
                    ExecutionResult(intent, true, "Flashlight turned on.")
                } else {
                    ExecutionResult(intent, false, "Could not turn on flashlight: ${res.exceptionOrNull()?.message}")
                }
            }
            NovaIntent.FLASHLIGHT_OFF -> {
                val res = flashlightController.setFlashlight(false)
                if (res.isSuccess) {
                    ExecutionResult(intent, true, "Flashlight turned off.")
                } else {
                    ExecutionResult(intent, false, "Could not turn off flashlight: ${res.exceptionOrNull()?.message}")
                }
            }
            NovaIntent.FLASHLIGHT_TOGGLE -> {
                val res = flashlightController.toggleFlashlight()
                if (res.isSuccess) {
                    val state = if (res.getOrNull() == true) "on" else "off"
                    ExecutionResult(intent, true, "Flashlight switched $state.")
                } else {
                    ExecutionResult(intent, false, "Could not toggle flashlight.")
                }
            }

            NovaIntent.VOLUME_INCREASE -> {
                val newVol = volumeController.increaseVolume()
                ExecutionResult(intent, true, "Volume increased to $newVol percent.")
            }
            NovaIntent.VOLUME_DECREASE -> {
                val newVol = volumeController.decreaseVolume()
                ExecutionResult(intent, true, "Volume decreased to $newVol percent.")
            }
            NovaIntent.VOLUME_SET -> {
                val target = entities.percentage ?: 50
                val newVol = volumeController.setVolumePercent(target)
                ExecutionResult(intent, true, "Volume set to $newVol percent.")
            }
            NovaIntent.VOLUME_MUTE -> {
                volumeController.mute()
                ExecutionResult(intent, true, "Volume muted.")
            }
            NovaIntent.VOLUME_UNMUTE -> {
                volumeController.unmute()
                ExecutionResult(intent, true, "Volume unmuted.")
            }

            NovaIntent.BRIGHTNESS_INCREASE -> {
                val res = brightnessController.adjustBrightness(15)
                if (res.isSuccess) {
                    ExecutionResult(intent, true, "Brightness increased to ${res.getOrNull()} percent.")
                } else {
                    try {
                        context.startActivity(brightnessController.requestWriteSettingsIntent())
                        ExecutionResult(intent, false, "Please grant write settings permission to change brightness.")
                    } catch (_: Exception) {
                        ExecutionResult(intent, false, "Permission required to adjust brightness.")
                    }
                }
            }
            NovaIntent.BRIGHTNESS_DECREASE -> {
                val res = brightnessController.adjustBrightness(-15)
                if (res.isSuccess) {
                    ExecutionResult(intent, true, "Brightness decreased to ${res.getOrNull()} percent.")
                } else {
                    ExecutionResult(intent, false, "Permission required to adjust brightness.")
                }
            }
            NovaIntent.BRIGHTNESS_SET -> {
                val target = entities.percentage ?: 50
                val res = brightnessController.setBrightnessPercent(target)
                if (res.isSuccess) {
                    ExecutionResult(intent, true, "Brightness set to $target percent.")
                } else {
                    ExecutionResult(intent, false, "Permission required to adjust brightness.")
                }
            }

            NovaIntent.BATTERY_CHECK -> {
                val info = batteryController.getBatteryInfo()
                val chargingStr = if (info.isCharging) " and is currently charging via ${info.chargingSource}" else ""
                ExecutionResult(intent, true, "Your battery is at ${info.percentage} percent$chargingStr.")
            }
            NovaIntent.CHARGING_CHECK -> {
                val info = batteryController.getBatteryInfo()
                val resp = if (info.isCharging) {
                    "Yes, your device is charging via ${info.chargingSource}. Battery level is ${info.percentage} percent."
                } else {
                    "No, your device is not charging. Battery is at ${info.percentage} percent."
                }
                ExecutionResult(intent, true, resp)
            }

            NovaIntent.STORAGE_CHECK -> {
                val info = storageController.getInternalStorageInfo()
                val resp = "You have ${info.availableGb} GB of available storage out of ${info.totalGb} GB total."
                ExecutionResult(intent, true, resp)
            }

            NovaIntent.DEVICE_INFO -> {
                val details = deviceInfoController.getDeviceDetails()
                val resp = "This phone is a ${details.manufacturer} ${details.model} running Android ${details.androidVersion}."
                ExecutionResult(intent, true, resp)
            }

            NovaIntent.WIFI_STATUS -> {
                val connected = connectivityController.isWifiConnected()
                val ssid = connectivityController.getWifiSsid()
                val resp = if (connected) {
                    "Wi-Fi is connected${if (ssid != null) " to $ssid" else ""}."
                } else {
                    "Wi-Fi is currently disconnected."
                }
                ExecutionResult(intent, true, resp)
            }
            NovaIntent.WIFI_SETTINGS -> {
                context.startActivity(connectivityController.getWifiSettingsIntent())
                ExecutionResult(intent, true, "Opening Wi-Fi settings.")
            }

            NovaIntent.BLUETOOTH_STATUS -> {
                val enabled = connectivityController.isBluetoothEnabled()
                val resp = if (enabled) "Bluetooth is turned on." else "Bluetooth is turned off."
                ExecutionResult(intent, true, resp)
            }
            NovaIntent.BLUETOOTH_SETTINGS -> {
                context.startActivity(connectivityController.getBluetoothSettingsIntent())
                ExecutionResult(intent, true, "Opening Bluetooth settings.")
            }

            NovaIntent.NETWORK_CHECK -> {
                val connected = connectivityController.isInternetConnected()
                val resp = if (connected) "You are connected to the internet." else "No active internet connection detected."
                ExecutionResult(intent, true, resp)
            }

            NovaIntent.LOCATION_GET -> {
                val locResult = locationController.getCurrentLocation()
                if (locResult.isSuccess) {
                    val loc = locResult.getOrThrow()
                    val latStr = String.format(Locale.US, "%.4f", loc.latitude)
                    val lngStr = String.format(Locale.US, "%.4f", loc.longitude)
                    val accStr = if (loc.accuracyMeters != null) " Location accuracy is approximately ${loc.accuracyMeters.toInt()} meters." else ""
                    val resp = "Your current coordinates are latitude $latStr and longitude $lngStr.$accStr"
                    ExecutionResult(intent, true, resp)
                } else {
                    val msg = locResult.exceptionOrNull()?.message ?: "GPS signal unavailable."
                    ExecutionResult(intent, false, "Unable to get GPS location: $msg")
                }
            }
            NovaIntent.LOCATION_MAPS -> {
                val locResult = locationController.getCurrentLocation()
                if (locResult.isSuccess) {
                    val loc = locResult.getOrThrow()
                    context.startActivity(locationController.createMapsIntent(loc.latitude, loc.longitude))
                    ExecutionResult(intent, true, "Opening your coordinates in Maps.")
                } else {
                    ExecutionResult(intent, false, "Could not acquire GPS fix to open Maps.")
                }
            }

            NovaIntent.APP_OPEN -> {
                val appName = entities.targetAppName ?: "App"
                val res = appLauncher.launchAppByName(appName)
                if (res.isSuccess) {
                    ExecutionResult(intent, true, res.getOrThrow())
                } else {
                    ExecutionResult(intent, false, res.exceptionOrNull()?.message ?: "$appName is not installed.")
                }
            }

            NovaIntent.WHATSAPP_OPEN -> {
                val res = appLauncher.launchAppByName("whatsapp")
                if (res.isSuccess) {
                    ExecutionResult(intent, true, "Opening WhatsApp.")
                } else {
                    ExecutionResult(intent, false, "WhatsApp is not installed on this device.")
                }
            }
            NovaIntent.WHATSAPP_MESSAGE -> {
                val contact = entities.contactName
                val msg = entities.messageBody
                var phone: String? = null
                if (contact != null) {
                    val matches = contactsHelper.findPhoneNumberForContact(contact)
                    if (matches.isNotEmpty()) {
                        phone = matches.first().phoneNumber
                    }
                }
                phoneMessagingHelper.openWhatsAppMessage(phone, msg)
                ExecutionResult(intent, true, "Opening WhatsApp${if (contact != null) " for $contact" else ""}.")
            }

            NovaIntent.PHONE_CALL -> {
                val contact = entities.contactName ?: ""
                val matches = contactsHelper.findPhoneNumberForContact(contact)
                if (matches.isNotEmpty()) {
                    phoneMessagingHelper.openDialer(matches.first().phoneNumber)
                    ExecutionResult(intent, true, "Opening dialer for ${matches.first().name}.")
                } else {
                    phoneMessagingHelper.openDialer("")
                    ExecutionResult(intent, true, "Opening phone dialer.")
                }
            }
            NovaIntent.PHONE_DIAL -> {
                val num = entities.phoneNumber ?: ""
                phoneMessagingHelper.openDialer(num)
                ExecutionResult(intent, true, "Dialing $num.")
            }
            NovaIntent.SMS_SEND -> {
                val contact = entities.contactName
                var phone: String? = null
                if (contact != null) {
                    val matches = contactsHelper.findPhoneNumberForContact(contact)
                    if (matches.isNotEmpty()) {
                        phone = matches.first().phoneNumber
                    }
                }
                phoneMessagingHelper.prepareSms(phone, entities.messageBody)
                ExecutionResult(intent, true, "Preparing message${if (contact != null) " to $contact" else ""}.")
            }

            NovaIntent.CAMERA_OPEN -> {
                val res = appLauncher.launchAppByName("camera")
                ExecutionResult(intent, res.isSuccess, res.getOrElse { "Opening camera." })
            }
            NovaIntent.CAMERA_TAKE_PHOTO -> {
                val res = appLauncher.launchAppByName("camera")
                ExecutionResult(intent, res.isSuccess, "Opening camera to take a photo.")
            }
            NovaIntent.CAMERA_RECORD_VIDEO -> {
                val res = appLauncher.launchAppByName("camera")
                ExecutionResult(intent, res.isSuccess, "Opening video camera.")
            }
            NovaIntent.GALLERY_OPEN -> {
                val res = appLauncher.launchAppByName("gallery")
                ExecutionResult(intent, res.isSuccess, "Opening Photos.")
            }

            NovaIntent.MEDIA_PLAY -> {
                mediaPlaybackController.play()
                ExecutionResult(intent, true, "Resuming playback.")
            }
            NovaIntent.MEDIA_PAUSE -> {
                mediaPlaybackController.pause()
                ExecutionResult(intent, true, "Playback paused.")
            }
            NovaIntent.MEDIA_NEXT -> {
                mediaPlaybackController.next()
                ExecutionResult(intent, true, "Skipping to next track.")
            }
            NovaIntent.MEDIA_PREVIOUS -> {
                mediaPlaybackController.previous()
                ExecutionResult(intent, true, "Playing previous track.")
            }

            NovaIntent.ALARM_SET -> {
                val h = entities.timeHour ?: 7
                val m = entities.timeMinute ?: 0
                val intentAlarm = alarmTimerController.setAlarm(h, m)
                context.startActivity(intentAlarm)
                val amPm = if (h >= 12) "PM" else "AM"
                val displayHour = if (h == 0) 12 else if (h > 12) h - 12 else h
                val timeStr = String.format(Locale.US, "%d:%02d %s", displayHour, m, amPm)
                ExecutionResult(intent, true, "Setting alarm for $timeStr.")
            }
            NovaIntent.TIMER_SET -> {
                val sec = entities.durationSeconds ?: 300
                val intentTimer = alarmTimerController.setTimer(sec)
                context.startActivity(intentTimer)
                val min = sec / 60
                val durationStr = if (min > 0) "$min minutes" else "$sec seconds"
                ExecutionResult(intent, true, "Timer set for $durationStr.")
            }
            NovaIntent.REMINDER_SET -> {
                val msg = entities.messageBody ?: "drink water"
                alarmTimerController.scheduleLocalReminder("NOVA Reminder", msg, 300)
                ExecutionResult(intent, true, "Reminder scheduled for $msg.")
            }

            NovaIntent.TIME_GET -> {
                val time = SimpleDateFormat("h:mm a", Locale.US).format(Date())
                ExecutionResult(intent, true, "The current time is $time.")
            }
            NovaIntent.DATE_GET -> {
                val date = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US).format(Date())
                ExecutionResult(intent, true, "Today is $date.")
            }

            NovaIntent.CALCULATE -> {
                val expr = entities.mathExpression ?: "calculation"
                val res = entities.mathResult
                if (res != null) {
                    val formatted = if (res % 1.0 == 0.0) res.toLong().toString() else String.format(Locale.US, "%.2f", res)
                    ExecutionResult(intent, true, "$expr is $formatted.")
                } else {
                    ExecutionResult(intent, false, "Could not perform calculation.")
                }
            }

            NovaIntent.UNIT_CONVERT -> {
                val conv = entities.convertedValue
                if (conv != null) {
                    val formatted = if (conv % 1.0 == 0.0) conv.toLong().toString() else String.format(Locale.US, "%.2f", conv)
                    ExecutionResult(intent, true, "${entities.unitValue} ${entities.unitFrom} is $formatted ${entities.unitTo}.")
                } else {
                    ExecutionResult(intent, false, "I could not convert between those units.")
                }
            }

            NovaIntent.UNKNOWN -> {
                ExecutionResult(intent, false, "I didn't recognize that command. Try asking 'What can you do?' or 'Turn on flashlight'.")
            }
        }
    }

    private fun buildCombinedResponse(results: List<ExecutionResult>): String {
        if (results.size == 1) {
            return results.first().spokenResponse
        }
        val builder = StringBuilder()
        for (i in results.indices) {
            val r = results[i]
            if (i > 0) {
                builder.append(" ")
            }
            builder.append(r.spokenResponse)
        }
        return builder.toString()
    }
}
