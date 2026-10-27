package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PhoneDisabled
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.contact.ContactHelper
import com.example.data.model.CallActionStatus
import com.example.data.model.CallLogEntity
import com.example.ui.theme.ElegantDarkMutedText
import com.example.ui.theme.ElegantDarkOutline
import com.example.ui.theme.ElegantDarkPrimary
import com.example.ui.theme.ElegantDarkSurfaceVariant
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CallHistoryItem(
    log: CallLogEntity,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val (statusLabel, statusColor, statusBg, statusIcon) = when (log.status) {
        CallActionStatus.SAVED.name -> StatusUiConfig(
            label = "Saved to Contacts",
            color = Color(0xFF21005D),
            bgColor = Color(0xFFEADDFF),
            icon = Icons.Default.PersonAdd
        )
        CallActionStatus.MANUAL_SAVED.name -> StatusUiConfig(
            label = "Manual Saved",
            color = Color(0xFF21005D),
            bgColor = Color(0xFFEADDFF),
            icon = Icons.Default.Check
        )
        CallActionStatus.ALREADY_EXISTS.name -> StatusUiConfig(
            label = "Already Exists (Preserved)",
            color = Color(0xFFE6E1E5),
            bgColor = Color(0xFF49454F),
            icon = Icons.Default.Person
        )
        CallActionStatus.PRIVATE_NUMBER.name -> StatusUiConfig(
            label = "Private Number",
            color = Color(0xFFD0BCFF),
            bgColor = Color(0xFF381E72),
            icon = Icons.Default.PhoneDisabled
        )
        else -> StatusUiConfig(
            label = "Failed to Save",
            color = Color(0xFFF9DEDC),
            bgColor = Color(0xFF8C1D18),
            icon = Icons.Default.Info
        )
    }

    val formattedTime = formatTimestamp(log.timestamp)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, ElegantDarkOutline.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
            .testTag("call_history_item_${log.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = ElegantDarkSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(statusBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = log.contactName.ifBlank { "Unknown Caller" },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            if (log.isSimulated) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF4F378B)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Science,
                                            contentDescription = null,
                                            tint = ElegantDarkPrimary,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "Test",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            color = ElegantDarkPrimary
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = log.phoneNumber,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = ElegantDarkPrimary
                        )
                    }
                }

                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = Color(0xFFCAC4D0)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Status chip & Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = statusColor
                    )
                }

                // Action buttons
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    // Call button
                    IconButton(
                        onClick = {
                            val intent = ContactHelper.getDialIntent(log.phoneNumber)
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Could not open dialer", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call Number",
                            tint = ElegantDarkPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Copy Number
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Phone Number", log.phoneNumber))
                            Toast.makeText(context, "Copied ${log.phoneNumber}", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Number",
                            tint = Color(0xFFCAC4D0),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Delete log
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Log",
                            tint = Color(0xFFCAC4D0),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (log.details.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = log.details,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = ElegantDarkMutedText
                )
            }
        }
    }
}

private data class StatusUiConfig(
    val label: String,
    val color: Color,
    val bgColor: Color,
    val icon: ImageVector
)

private fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val now = Date()
    val diff = now.time - timestamp

    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> {
            val format = SimpleDateFormat("h:mm a", Locale.getDefault())
            format.format(date)
        }
        else -> {
            val format = SimpleDateFormat("dd MMM, h:mm a", Locale.getDefault())
            format.format(date)
        }
    }
}

