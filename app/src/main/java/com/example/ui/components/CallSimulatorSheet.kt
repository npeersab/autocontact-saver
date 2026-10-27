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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CallActionStatus
import com.example.ui.SimulationResult
import com.example.ui.theme.ElegantDarkMutedText
import com.example.ui.theme.ElegantDarkOnPrimary
import com.example.ui.theme.ElegantDarkOutline
import com.example.ui.theme.ElegantDarkPrimary
import com.example.ui.theme.ElegantDarkPrimaryContainer
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantDarkSurfaceContainer
import com.example.ui.theme.ElegantDarkSurfaceVariant

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CallSimulatorCard(
    isSimulating: Boolean,
    simulationResult: SimulationResult?,
    onSimulateCall: (String) -> Unit,
    onVerifyOnly: (String) -> Unit = {},
    onDismissResult: () -> Unit,
    modifier: Modifier = Modifier
) {
    var phoneNumberInput by remember { mutableStateOf("+1 555-0198") }
    val keyboardController = LocalSoftwareKeyboardController.current

    val sampleNumbers = listOf(
        "+1 (555) 234-8901",
        "+44 7700 900543",
        "+91 98765 43210",
        "+61 491 570 156"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, ElegantDarkOutline.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .testTag("call_simulator_card"),
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
                        imageVector = Icons.Default.Science,
                        contentDescription = null,
                        tint = ElegantDarkPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = "Incoming Call Simulator",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Test contact verification & auto-save engine immediately",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCAC4D0)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = phoneNumberInput,
                onValueChange = { phoneNumberInput = it },
                label = { Text("Phone Number to Test", color = Color(0xFFCAC4D0)) },
                placeholder = { Text("+1 (555) 000-0000", color = ElegantDarkMutedText) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.PhoneInTalk,
                        contentDescription = null,
                        tint = ElegantDarkPrimary
                    )
                },
                trailingIcon = {
                    if (phoneNumberInput.isNotEmpty()) {
                        IconButton(onClick = { phoneNumberInput = "" }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear input",
                                tint = Color(0xFFCAC4D0)
                            )
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                        if (phoneNumberInput.isNotBlank() && !isSimulating) {
                            onSimulateCall(phoneNumberInput)
                        }
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("simulator_phone_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = ElegantDarkSurface,
                    unfocusedContainerColor = ElegantDarkSurface,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedBorderColor = ElegantDarkPrimary,
                    unfocusedBorderColor = ElegantDarkOutline
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Quick preset chips
            Text(
                text = "Quick Sample Numbers:",
                style = MaterialTheme.typography.labelSmall,
                color = ElegantDarkMutedText
            )

            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                sampleNumbers.forEach { sample ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ElegantDarkSurfaceContainer,
                        modifier = Modifier
                            .border(1.dp, ElegantDarkOutline, RoundedCornerShape(8.dp))
                            .clickable {
                                phoneNumberInput = sample
                            }
                    ) {
                        Text(
                            text = sample,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = ElegantDarkPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    keyboardController?.hide()
                    onSimulateCall(phoneNumberInput)
                },
                enabled = phoneNumberInput.isNotBlank() && !isSimulating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("simulate_call_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElegantDarkPrimary,
                    contentColor = ElegantDarkOnPrimary,
                    disabledContainerColor = Color(0xFF49454F),
                    disabledContentColor = ElegantDarkMutedText
                )
            ) {
                if (isSimulating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = ElegantDarkOnPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Processing Call...", fontWeight = FontWeight.SemiBold, color = ElegantDarkOnPrimary)
                } else {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = ElegantDarkOnPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Simulate Incoming Call & Save",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = ElegantDarkOnPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    keyboardController?.hide()
                    onVerifyOnly(phoneNumberInput)
                },
                enabled = phoneNumberInput.isNotBlank() && !isSimulating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("verify_contact_provider_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = ElegantDarkPrimary,
                    disabledContentColor = ElegantDarkMutedText
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(ElegantDarkOutline)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = if (phoneNumberInput.isNotBlank() && !isSimulating) ElegantDarkPrimary else ElegantDarkMutedText,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Verify in Contacts Provider Only",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }

            // Live simulation result feedback card
            AnimatedVisibility(
                visible = simulationResult != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                simulationResult?.let { result ->
                    Spacer(modifier = Modifier.height(12.dp))
                    val (bannerBg, bannerFg) = when (result.actionTaken) {
                        CallActionStatus.SAVED -> Pair(Color(0xFFEADDFF), Color(0xFF21005D))
                        CallActionStatus.ALREADY_EXISTS -> Pair(Color(0xFF49454F), Color(0xFFE6E1E5))
                        else -> Pair(Color(0xFF8C1D18), Color(0xFFF9DEDC))
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = bannerBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = when (result.actionTaken) {
                                        CallActionStatus.SAVED -> Icons.Default.CheckCircle
                                        CallActionStatus.ALREADY_EXISTS -> Icons.Default.Info
                                        else -> Icons.Default.Warning
                                    },
                                    contentDescription = null,
                                    tint = bannerFg,
                                    modifier = Modifier.size(20.dp)
                                )

                                Column {
                                    Text(
                                        text = result.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = bannerFg
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = result.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = bannerFg.copy(alpha = 0.9f),
                                        lineHeight = 16.sp
                                    )
                                }
                            }

                            IconButton(
                                onClick = onDismissResult,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = bannerFg,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

