package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.NamingFormat
import com.example.ui.theme.ElegantDarkMutedText
import com.example.ui.theme.ElegantDarkOnPrimary
import com.example.ui.theme.ElegantDarkOutline
import com.example.ui.theme.ElegantDarkPrimary
import com.example.ui.theme.ElegantDarkSurfaceContainer
import com.example.ui.theme.ElegantDarkSurfaceContainerHigh

@Composable
fun StatusHeroCard(
    isEnabled: Boolean,
    isCoreAuthorized: Boolean,
    namingFormat: NamingFormat,
    customPrefix: String,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val isActive = isEnabled && isCoreAuthorized

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val containerBg by animateColorAsState(
        targetValue = if (isActive) ElegantDarkSurfaceContainerHigh
        else Color(0xFF2B2930),
        label = "bg_anim"
    )

    val accentColor = if (isActive) ElegantDarkPrimary else ElegantDarkMutedText

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("status_hero_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isActive) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(ElegantDarkPrimary.copy(alpha = 0.2f))
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isActive) ElegantDarkPrimary else Color(0xFF3E3C41)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isActive) Icons.Default.PersonAdd else Icons.Default.CallEnd,
                                contentDescription = null,
                                tint = if (isActive) ElegantDarkOnPrimary else Color(0xFFCAC4D0),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = if (isActive) "Auto-save active" else if (!isCoreAuthorized) "Setup Required" else "Auto-save paused",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 17.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isActive) "Monitoring incoming calls" else if (!isCoreAuthorized) "Grant permissions below" else "Tap switch to activate",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCAC4D0)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle,
                    modifier = Modifier.testTag("auto_save_switch"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ElegantDarkOnPrimary,
                        checkedTrackColor = ElegantDarkPrimary,
                        uncheckedThumbColor = ElegantDarkMutedText,
                        uncheckedTrackColor = Color(0xFF36343B),
                        uncheckedBorderColor = Color(0xFF49454F)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Smart rule card in Elegant Dark #211F26 style
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = ElegantDarkSurfaceContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ElegantDarkOutline, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = ElegantDarkPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Smart Protection Rule",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = ElegantDarkPrimary
                        )
                    }

                    Text(
                        text = "If an incoming caller is already in your contacts, nothing is changed. Only new unknown numbers are saved automatically.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCAC4D0),
                        lineHeight = 18.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Naming Pattern:",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElegantDarkMutedText
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF381E72))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (namingFormat == NamingFormat.PREFIX_NUMBER) "$customPrefix +123..." else namingFormat.example,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = ElegantDarkPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

