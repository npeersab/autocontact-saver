package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.NamingFormat
import com.example.ui.theme.ElegantDarkError
import com.example.ui.theme.ElegantDarkMutedText
import com.example.ui.theme.ElegantDarkOnPrimary
import com.example.ui.theme.ElegantDarkOutline
import com.example.ui.theme.ElegantDarkPrimary
import com.example.ui.theme.ElegantDarkPrimaryContainer
import com.example.ui.theme.ElegantDarkSecondaryContainer
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantDarkSurfaceContainer
import com.example.ui.theme.ElegantDarkSurfaceVariant

@Composable
fun SettingsView(
    namingFormat: NamingFormat,
    customPrefix: String,
    notificationsEnabled: Boolean,
    skipPrivateNumbers: Boolean,
    onNamingFormatChanged: (NamingFormat) -> Unit,
    onCustomPrefixChanged: (String) -> Unit,
    onNotificationsToggled: (Boolean) -> Unit,
    onSkipPrivateNumbersToggled: (Boolean) -> Unit,
    onClearAllLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Naming Rules Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ElegantDarkOutline.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = ElegantDarkSurfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(ElegantDarkPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = null,
                            tint = ElegantDarkPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Contact Name Format",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Choose how newly saved contacts will be titled",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCAC4D0)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                NamingFormat.values().forEach { format ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (namingFormat == format) ElegantDarkSurfaceContainer
                        else Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNamingFormatChanged(format) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (namingFormat == format),
                                onClick = { onNamingFormatChanged(format) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = ElegantDarkPrimary,
                                    unselectedColor = ElegantDarkMutedText
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = format.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Preview: ${format.example}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElegantDarkPrimary
                                )
                            }
                        }
                    }
                }

                AnimatedVisibility(visible = namingFormat == NamingFormat.PREFIX_NUMBER) {
                    Column(modifier = Modifier.padding(top = 10.dp, start = 8.dp, end = 8.dp)) {
                        OutlinedTextField(
                            value = customPrefix,
                            onValueChange = onCustomPrefixChanged,
                            label = { Text("Custom Prefix Text", color = Color(0xFFCAC4D0)) },
                            placeholder = { Text("e.g. Lead, Inbound, Client", color = ElegantDarkMutedText) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_prefix_input"),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = ElegantDarkSurface,
                                unfocusedContainerColor = ElegantDarkSurface,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedBorderColor = ElegantDarkPrimary,
                                unfocusedBorderColor = ElegantDarkOutline
                            )
                        )
                    }
                }
            }
        }

        // Notification & Security Toggles Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ElegantDarkOutline.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = ElegantDarkSurfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(ElegantDarkSecondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = Color(0xFFCCC2DC),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Preferences",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Notification and privacy filters",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCAC4D0)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Notifications switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = ElegantDarkPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Save Alerts",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Post notification when a caller is auto-saved",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFCAC4D0)
                            )
                        }
                    }

                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = onNotificationsToggled,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ElegantDarkOnPrimary,
                            checkedTrackColor = ElegantDarkPrimary,
                            uncheckedThumbColor = ElegantDarkMutedText,
                            uncheckedTrackColor = Color(0xFF36343B),
                            uncheckedBorderColor = Color(0xFF49454F)
                        )
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = ElegantDarkOutline.copy(alpha = 0.4f)
                )

                // Skip private numbers switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = ElegantDarkPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Skip Private / Withheld Numbers",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Do not attempt to save unknown or masked caller IDs",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFCAC4D0)
                            )
                        }
                    }

                    Switch(
                        checked = skipPrivateNumbers,
                        onSkipPrivateNumbersToggled,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ElegantDarkOnPrimary,
                            checkedTrackColor = ElegantDarkPrimary,
                            uncheckedThumbColor = ElegantDarkMutedText,
                            uncheckedTrackColor = Color(0xFF36343B),
                            uncheckedBorderColor = Color(0xFF49454F)
                        )
                    )
                }
            }
        }

        // How it Works & Clear History Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ElegantDarkOutline.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = ElegantDarkSurfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = null,
                        tint = ElegantDarkPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "How Auto-Saving Works",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "1. Incoming call rings -> system catches caller number.\n2. Queries device contact list -> checks if number already exists.\n3. If contact exists -> No action taken (preserved).\n4. If contact does not exist -> Automatically written to your phone address book.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFCAC4D0),
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF601410).copy(alpha = 0.4f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF8C1D18), RoundedCornerShape(12.dp))
                        .clickable { showClearConfirmDialog = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = null,
                            tint = ElegantDarkError,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Clear Activity History Log",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = ElegantDarkError
                        )
                    }
                }
            }
        }
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            containerColor = ElegantDarkSurfaceVariant,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = Color(0xFFCAC4D0),
            title = { Text("Clear All History?") },
            text = { Text("This will delete all activity logs from this app. Your actual saved contacts in your phonebook will NOT be deleted.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAllLogs()
                        showClearConfirmDialog = false
                    }
                ) {
                    Text("Clear", color = ElegantDarkError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel", color = Color(0xFFCAC4D0))
                }
            }
        )
    }
}

