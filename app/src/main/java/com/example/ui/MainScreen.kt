package com.example.ui

import android.Manifest
import android.app.role.RoleManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CallHistoryItem
import com.example.ui.components.CallSimulatorCard
import com.example.ui.components.PermissionManagerCard
import com.example.ui.components.SettingsView
import com.example.ui.components.StatsCardRow
import com.example.ui.components.StatusHeroCard
import com.example.ui.theme.ElegantDarkBackground
import com.example.ui.theme.ElegantDarkMutedText
import com.example.ui.theme.ElegantDarkOnPrimary
import com.example.ui.theme.ElegantDarkOnSurface
import com.example.ui.theme.ElegantDarkOutline
import com.example.ui.theme.ElegantDarkPrimary
import com.example.ui.theme.ElegantDarkPrimaryContainer
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantDarkSurfaceContainer
import com.example.ui.theme.ElegantDarkSurfaceVariant

enum class MainTab(val title: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Speed),
    HISTORY("History", Icons.Default.History),
    SIMULATOR("Simulator", Icons.Default.PlayCircleOutline),
    SETTINGS("Settings", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: CallSaverViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var currentTab by remember { mutableIntStateOf(0) }

    // State observation
    val permissions by viewModel.permissions.collectAsStateWithLifecycle()
    val isAutoSaveEnabled by viewModel.isAutoSaveEnabled.collectAsStateWithLifecycle()
    val namingFormat by viewModel.namingFormat.collectAsStateWithLifecycle()
    val customPrefix by viewModel.customPrefix.collectAsStateWithLifecycle()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val skipPrivateNumbers by viewModel.skipPrivateNumbers.collectAsStateWithLifecycle()

    val totalCount by viewModel.totalCount.collectAsStateWithLifecycle()
    val savedCount by viewModel.savedCount.collectAsStateWithLifecycle()
    val existingCount by viewModel.existingCount.collectAsStateWithLifecycle()

    val callLogs by viewModel.callLogs.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val isSimulating by viewModel.isSimulating.collectAsStateWithLifecycle()
    val simulationResult by viewModel.simulationResult.collectAsStateWithLifecycle()

    // Permission launchers for runtime requests
    val contactsPermissions = remember {
        arrayOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS
        )
    }

    val phonePermissions = remember {
        arrayOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG
        )
    }

    val allPermissionsToRequest = remember {
        mutableListOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.toTypedArray()
    }

    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.checkPermissions()
    }

    val phonePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.checkPermissions()
    }

    val allPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.checkPermissions()
    }

    val callScreeningRoleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.checkPermissions()
    }

    // Refresh permissions on resume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ElegantDarkBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(ElegantDarkPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContactPhone,
                                contentDescription = null,
                                tint = ElegantDarkPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "Auto Contact Saver",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp,
                                letterSpacing = 0.2.sp
                            ),
                            color = ElegantDarkOnSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = ElegantDarkBackground,
                    titleContentColor = ElegantDarkOnSurface
                ),
                actions = {
                    if (!permissions.areCoreGranted) {
                        IconButton(
                            onClick = {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                                context.startActivity(intent)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "App Settings",
                                tint = Color(0xFFF2B8B5)
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = ElegantDarkSurfaceContainer,
                tonalElevation = 0.dp
            ) {
                MainTab.values().forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = currentTab == index,
                        onClick = { currentTab = index },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (currentTab == index) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElegantDarkPrimary,
                            selectedTextColor = ElegantDarkPrimary,
                            indicatorColor = ElegantDarkPrimaryContainer,
                            unselectedIconColor = ElegantDarkMutedText,
                            unselectedTextColor = ElegantDarkMutedText
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> {
                    // Dashboard View
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            StatusHeroCard(
                                isEnabled = isAutoSaveEnabled,
                                isCoreAuthorized = permissions.areCoreGranted,
                                namingFormat = namingFormat,
                                customPrefix = customPrefix,
                                onToggle = { viewModel.setAutoSaveEnabled(it) }
                            )
                        }

                        item {
                            StatsCardRow(
                                totalCalls = totalCount,
                                savedContacts = savedCount,
                                existingContacts = existingCount
                            )
                        }

                        item {
                            PermissionManagerCard(
                                permissions = permissions,
                                onRequestContactsPermissions = {
                                    contactsPermissionLauncher.launch(contactsPermissions)
                                },
                                onRequestPhonePermissions = {
                                    phonePermissionLauncher.launch(phonePermissions)
                                },
                                onRequestAllPermissions = {
                                    allPermissionsLauncher.launch(allPermissionsToRequest)
                                },
                                onRequestCallScreeningRole = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                        val roleManager = context.getSystemService(RoleManager::class.java)
                                        if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
                                            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                                            callScreeningRoleLauncher.launch(intent)
                                        }
                                    }
                                }
                            )
                        }

                        item {
                            CallSimulatorCard(
                                isSimulating = isSimulating,
                                simulationResult = simulationResult,
                                onSimulateCall = { viewModel.testIncomingCall(it) },
                                onVerifyOnly = { viewModel.verifyContactProviderOnly(it) },
                                onDismissResult = { viewModel.clearSimulationResult() }
                            )
                        }

                        // Recent Call Activity Header
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Recent Incoming Activity",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp
                                    ),
                                    color = ElegantDarkOnSurface
                                )
                                if (callLogs.isNotEmpty()) {
                                    Text(
                                        text = "View All",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = ElegantDarkPrimary,
                                        modifier = Modifier.clickable { currentTab = 1 }
                                    )
                                }
                            }
                        }

                        if (callLogs.isEmpty()) {
                            item {
                                EmptyLogsCard(onSimulateClick = { currentTab = 2 })
                            }
                        } else {
                            items(callLogs.take(3), key = { it.id }) { log ->
                                CallHistoryItem(
                                    log = log,
                                    onDelete = { viewModel.deleteLog(log.id) }
                                )
                            }
                        }
                    }
                }

                1 -> {
                    // History / Activity View
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))

                        // Search Bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("Search by phone number or name...", color = ElegantDarkMutedText) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = ElegantDarkPrimary
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = "Clear Search",
                                            tint = Color(0xFFCAC4D0)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("history_search_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = ElegantDarkSurfaceVariant,
                                unfocusedContainerColor = ElegantDarkSurfaceVariant,
                                focusedTextColor = ElegantDarkOnSurface,
                                unfocusedTextColor = ElegantDarkOnSurface,
                                focusedBorderColor = ElegantDarkPrimary,
                                unfocusedBorderColor = ElegantDarkOutline
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Filter Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LogFilter.values().forEach { filter ->
                                FilterChip(
                                    selected = selectedFilter == filter,
                                    onClick = { viewModel.setFilter(filter) },
                                    label = { Text(filter.label, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ElegantDarkPrimaryContainer,
                                        selectedLabelColor = ElegantDarkPrimary,
                                        containerColor = ElegantDarkSurfaceContainer,
                                        labelColor = Color(0xFFCAC4D0)
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selectedFilter == filter,
                                        borderColor = if (selectedFilter == filter) ElegantDarkPrimary else ElegantDarkOutline
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (callLogs.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                EmptyLogsCard(onSimulateClick = { currentTab = 2 })
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(callLogs, key = { it.id }) { log ->
                                    CallHistoryItem(
                                        log = log,
                                        onDelete = { viewModel.deleteLog(log.id) }
                                    )
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Dedicated Simulator View
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            CallSimulatorCard(
                                isSimulating = isSimulating,
                                simulationResult = simulationResult,
                                onSimulateCall = { viewModel.testIncomingCall(it) },
                                onVerifyOnly = { viewModel.verifyContactProviderOnly(it) },
                                onDismissResult = { viewModel.clearSimulationResult() }
                            )
                        }

                        item {
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
                                    Text(
                                        text = "How to Test on Real Device or Emulator",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 16.sp
                                        ),
                                        color = ElegantDarkOnSurface
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "• On Android 10+: Android 10 restricts reading caller numbers from background broadcasts while ringing. The app now supports Android 10+ Call Screening for instant real-time detection, plus an automatic CallLog fallback when calls finish or miss.\n• On Physical Phone: Tap 'Enable Android 10+ Call Detection' in Permissions to enable real-time screening as soon as the phone rings.\n• In Emulator: You can test via Extended Controls (...) -> Phone -> Call Device.\n• In-App: Use the simulator above anytime to test auto-saving or contacts verification!",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFCAC4D0),
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // Settings View
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
                    ) {
                        item {
                            SettingsView(
                                namingFormat = namingFormat,
                                customPrefix = customPrefix,
                                notificationsEnabled = notificationsEnabled,
                                skipPrivateNumbers = skipPrivateNumbers,
                                onNamingFormatChanged = { viewModel.setNamingFormat(it) },
                                onCustomPrefixChanged = { viewModel.setCustomPrefix(it) },
                                onNotificationsToggled = { viewModel.setNotificationsEnabled(it) },
                                onSkipPrivateNumbersToggled = { viewModel.setSkipPrivateNumbers(it) },
                                onClearAllLogs = { viewModel.clearAllLogs() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyLogsCard(
    onSimulateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, ElegantDarkOutline.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = ElegantDarkSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(ElegantDarkPrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ContactPhone,
                    contentDescription = null,
                    tint = ElegantDarkPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No Calls Recorded Yet",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                ),
                color = ElegantDarkOnSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "When you receive calls or run a test simulation, activity will appear here automatically.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFCAC4D0),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = ElegantDarkPrimary,
                modifier = Modifier.clickable { onSimulateClick() }
            ) {
                Text(
                    text = "Test with Simulator",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = ElegantDarkOnPrimary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}

