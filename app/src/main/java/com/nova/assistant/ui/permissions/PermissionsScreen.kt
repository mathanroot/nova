package com.nova.assistant.ui.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.nova.assistant.ui.components.GlassmorphicCard
import com.nova.assistant.ui.theme.NovaCyan
import com.nova.assistant.ui.theme.NovaDeepBlack
import com.nova.assistant.ui.theme.NovaGlowError
import com.nova.assistant.ui.theme.NovaTextDimmed
import com.nova.assistant.ui.theme.NovaTextPrimary
import com.nova.assistant.ui.theme.NovaTextSecondary

data class PermissionItem(
    val title: String,
    val description: String,
    val permission: String,
    val isGranted: Boolean
)

@Composable
fun PermissionsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    val permissionsList = listOf(
        PermissionItem(
            title = "Microphone (Record Audio)",
            description = "Required for wake-word detection & voice recognition.",
            permission = Manifest.permission.RECORD_AUDIO,
            isGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        ),
        PermissionItem(
            title = "GPS & Location",
            description = "Required to answer 'Where am I?' and GPS coordinates.",
            permission = Manifest.permission.ACCESS_FINE_LOCATION,
            isGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ),
        PermissionItem(
            title = "Camera",
            description = "Required for flashlight control and camera actions.",
            permission = Manifest.permission.CAMERA,
            isGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        ),
        PermissionItem(
            title = "Contacts",
            description = "Required to search contacts for phone calls and SMS.",
            permission = Manifest.permission.READ_CONTACTS,
            isGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(NovaDeepBlack, Color(0xFF070B1E), NovaDeepBlack)
                )
            )
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = NovaTextPrimary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "PERMISSIONS & PRIVACY",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = NovaTextPrimary
                )
                Text(
                    text = "Zero Cloud. 100% On-Device Processing.",
                    fontSize = 12.sp,
                    color = NovaTextDimmed
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Privacy Guarantee Card
        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, null, tint = NovaCyan, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Privacy Guarantee",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = NovaTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "NOVA never uploads audio, commands, or personal data to any external cloud or server. All processing is strictly local.",
                        fontSize = 12.sp,
                        color = NovaTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        for (item in permissionsList) {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NovaTextPrimary
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = item.description,
                            fontSize = 12.sp,
                            color = NovaTextDimmed
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (item.isGranted) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (item.isGranted) NovaCyan else NovaGlowError,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (item.isGranted) "Granted" else "Needed",
                            fontSize = 12.sp,
                            color = if (item.isGranted) NovaCyan else NovaGlowError,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:" + context.packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0x3300BBF9),
                contentColor = NovaCyan
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Open System App Settings", fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
