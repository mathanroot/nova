package com.nova.assistant.apps.communication

import android.content.Context
import android.content.Intent
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhoneMessagingHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun openDialer(phoneNumber: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun prepareSms(phoneNumber: String?, message: String?): Boolean {
        return try {
            val uri = if (!phoneNumber.isNullOrEmpty()) {
                Uri.parse("smsto:$phoneNumber")
            } else {
                Uri.parse("smsto:")
            }
            val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                if (!message.isNullOrEmpty()) {
                    putExtra("sms_body", message)
                }
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun openWhatsAppMessage(phoneNumber: String?, message: String?): Boolean {
        return try {
            val encodedMsg = if (message != null) URLEncoder.encode(message, "UTF-8") else ""
            val url = if (!phoneNumber.isNullOrEmpty()) {
                val cleanPhone = phoneNumber.replace("+", "").replace(" ", "").replace("-", "")
                "https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMsg"
            } else {
                "https://api.whatsapp.com/send?text=$encodedMsg"
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                setPackage("com.whatsapp")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }
}
