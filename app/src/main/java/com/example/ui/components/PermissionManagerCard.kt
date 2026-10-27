package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.PermissionStatuses
import com.example.ui.theme.ElegantDarkError
import com.example.ui.theme.ElegantDarkMutedText
import com.example.ui.theme.ElegantDarkOnPrimary
import com.example.ui.theme.ElegantDarkOutline
import com.example.ui.theme.ElegantDarkPrimary
import com.example.ui.theme.ElegantDarkPrimaryContainer
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantDarkSurfaceVariant

@Composable
fun PermissionManagerCard(
    permissions: PermissionStatuses,
    onRequestAllPermissions: () -> Unit,
    onRequestContactsPermissions: () -> Unit,
    onRequestPhonePermissions: () -> Unit = onRequestAllPermissions,
    onRequestCallScreeningRole: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val contactsGranted = permissions.hasReadContacts && permissions.hasWriteContacts

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, ElegantDarkOutline.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .testTag("permission_manager_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = ElegantDarkSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (permissions.areCoreGranted) Color(0xFFEADDFF)
                                else Color(0xFF8C1D18).copy(alpha = 0.4f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (permissions.areCoreGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (permissions.areCoreGranted) Color(0xFF21005D) else ElegantDarkError,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (permissions.areCoreGranted) "Permissions Granted" else "Runtime Permissions Needed",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (permissions.areCoreGranted) "App is fully authorized to verify & save incoming callers"
                            else "Grant READ/WRITE contacts & phone access to enable saving",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCAC4D0)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Individual permission rows
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PermissionItemRow(
                    icon = Icons.Default.Contacts,
                    title = "Contacts Read & Write",
                    subtitle = "Verify existing contacts in phonebook & save new numbers",
                    isGranted = contactsGranted,
                    onClick = {
                        if (!contactsGranted) onRequestContactsPermissions()
                    },
                    actionText = if (!contactsGranted) "Grant" else null
                )
                PermissionItemRow(
                    icon = Icons.Default.PhoneInTalk,
                    title = "Phone State",
                    subtitle = "Detect incoming ring events in real-time",
                    isGranted = permissions.hasPhoneState,
                    onClick = {
                        if (!permissions.hasPhoneState) onRequestPhonePermissions()
                    },
                    actionText = if (!permissions.hasPhoneState) "Grant" else null
                )
                PermissionItemRow(
                    icon = Icons.Default.Call,
                    title = "Call Log Access",
                    subtitle = "Read incoming caller phone number on Android 9+",
                    isGranted = permissions.hasCallLog,
                    onClick = {
                        if (!permissions.hasCallLog) onRequestPhonePermissions()
                    },
                    actionText = if (!permissions.hasCallLog) "Grant" else null
                )
                PermissionItemRow(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    subtitle = "Receive alert when a new caller is automatically saved",
                    isGranted = permissions.hasNotification,
                    onClick = {
                        if (!permissions.hasNotification) onRequestAllPermissions()
                    },
                    actionText = if (!permissions.hasNotification) "Grant" else null
                )
                if (permissions.isAndroid10Plus) {
                    PermissionItemRow(
                        icon = Icons.Default.Security,
                        title = "Android 10+ Call Screening",
                        subtitle = if (permissions.hasCallScreeningRole)
                            "Active: Real-time caller number identification while phone rings"
                        else
                            "Recommended for Android 10+: Enables instant caller ID without waiting for call to end",
                        isGranted = permissions.hasCallScreeningRole,
                        onClick = {
                            if (!permissions.hasCallScreeningRole) onRequestCallScreeningRole()
                        },
                        actionText = if (!permissions.hasCallScreeningRole) "Enable" else null
                    )
                }
            }

            AnimatedVisibility(
                visible = !permissions.areCoreGranted,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))

                    if (permissions.isAndroid10Plus && !permissions.hasCallScreeningRole) {
                        Button(
                            onClick = onRequestCallScreeningRole,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("enable_call_screening_role_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD0BCFF),
                                contentColor = Color(0xFF381E72)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = Color(0xFF381E72),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Enable Android 10+ Call Detection",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF381E72)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    if (!contactsGranted) {
                        Button(
                            onClick = onRequestContactsPermissions,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("grant_contacts_permission_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElegantDarkPrimary,
                                contentColor = ElegantDarkOnPrimary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Contacts,
                                contentDescription = null,
                                tint = ElegantDarkOnPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Request READ & WRITE Contacts",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = ElegantDarkOnPrimary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    OutlinedButton(
                        onClick = onRequestAllPermissions,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("grant_all_permissions_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ElegantDarkPrimary
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(ElegantDarkOutline)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = ElegantDarkPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Grant All Permissions",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = ElegantDarkPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isGranted: Boolean,
    onClick: () -> Unit = {},
    actionText: String? = null
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isGranted) ElegantDarkSurface.copy(alpha = 0.85f)
        else Color(0xFF601410).copy(alpha = 0.25f),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ElegantDarkOutline.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .clickable(enabled = !isGranted) { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isGranted) ElegantDarkPrimary else ElegantDarkError,
                    modifier = Modifier.size(18.dp)
                )
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFCAC4D0)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (isGranted) Color(0xFFEADDFF)
                        else Color(0xFF8C1D18).copy(alpha = 0.5f)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isGranted) "Granted" else (actionText ?: "Missing"),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isGranted) Color(0xFF21005D) else Color(0xFFF9DEDC)
                )
            }
        }
    }
}

