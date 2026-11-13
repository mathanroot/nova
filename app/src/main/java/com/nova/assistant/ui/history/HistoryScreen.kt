package com.nova.assistant.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nova.assistant.ui.components.GlassmorphicCard
import com.nova.assistant.ui.theme.NovaCyan
import com.nova.assistant.ui.theme.NovaDeepBlack
import com.nova.assistant.ui.theme.NovaGlowError
import com.nova.assistant.ui.theme.NovaMagenta
import com.nova.assistant.ui.theme.NovaTextDimmed
import com.nova.assistant.ui.theme.NovaTextPrimary
import com.nova.assistant.ui.theme.NovaTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel
) {
    val conversations by viewModel.conversations.collectAsState()
    val timeFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(NovaDeepBlack, Color(0xFF070B1E), NovaDeepBlack)
                )
            )
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CONVERSATION HISTORY",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = NovaTextPrimary
                )
                Text(
                    text = "Stored locally on device • Privacy-First",
                    fontSize = 12.sp,
                    color = NovaTextDimmed
                )
            }
            if (conversations.isNotEmpty()) {
                IconButton(
                    onClick = { viewModel.clearHistory() },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0x221E293B))
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear History",
                        tint = NovaGlowError
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (conversations.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = NovaTextDimmed,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No recorded commands yet",
                        fontSize = 16.sp,
                        color = NovaTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Spoken and executed commands will appear here.",
                        fontSize = 13.sp,
                        color = NovaTextDimmed
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(conversations, key = { it.id }) { item ->
                    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (item.isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                        contentDescription = null,
                                        tint = if (item.isSuccess) NovaCyan else NovaGlowError,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = item.intentId,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = NovaTextDimmed
                                    )
                                }
                                Text(
                                    text = timeFormat.format(Date(item.timestamp)),
                                    fontSize = 11.sp,
                                    color = NovaTextDimmed
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "\"${item.userCommand}\"",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = NovaTextPrimary
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = item.assistantResponse,
                                fontSize = 14.sp,
                                color = NovaTextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
