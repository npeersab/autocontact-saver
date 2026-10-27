package com.example.ui

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.AutoContactSaverApp
import com.example.contact.ContactHelper
import com.example.data.model.CallActionStatus
import com.example.data.model.CallLogEntity
import com.example.data.preferences.AppSettings
import com.example.data.preferences.NamingFormat
import com.example.data.repository.CallLogRepository
import com.example.receiver.CallLogObserver
import com.example.receiver.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class LogFilter(val label: String) {
    ALL("All Calls"),
    SAVED("Auto-Saved"),
    EXISTING("Already Exists"),
    OTHER("Ignored / Skipped")
}

data class PermissionStatuses(
    val hasPhoneState: Boolean = false,
    val hasCallLog: Boolean = false,
    val hasReadContacts: Boolean = false,
    val hasWriteContacts: Boolean = false,
    val hasNotification: Boolean = false,
    val hasCallScreeningRole: Boolean = false
) {
    val isAndroid10Plus: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    val areCoreGranted: Boolean
        get() = hasPhoneState && hasCallLog && hasReadContacts && hasWriteContacts && (!isAndroid10Plus || hasCallScreeningRole)

    val areBasicPermissionsGranted: Boolean
        get() = hasPhoneState && hasCallLog && hasReadContacts && hasWriteContacts
}

data class SimulationResult(
    val processed: Boolean = false,
    val isSuccess: Boolean = true,
    val title: String = "",
    val message: String = "",
    val actionTaken: CallActionStatus = CallActionStatus.SAVED
)

class CallSaverViewModel(
    private val repository: CallLogRepository,
    private val appSettings: AppSettings,
    private val appContext: Context
) : ViewModel() {

    // Permission States
    private val _permissions = MutableStateFlow(PermissionStatuses())
    val permissions: StateFlow<PermissionStatuses> = _permissions.asStateFlow()

    // Settings
    val isAutoSaveEnabled: StateFlow<Boolean> = appSettings.isAutoSaveEnabled
    val namingFormat: StateFlow<NamingFormat> = appSettings.namingFormat
    val customPrefix: StateFlow<String> = appSettings.customPrefix
    val notificationsEnabled: StateFlow<Boolean> = appSettings.notificationsEnabled
    val skipPrivateNumbers: StateFlow<Boolean> = appSettings.skipPrivateNumbers

    // Filter and Search
    private val _selectedFilter = MutableStateFlow(LogFilter.ALL)
    val selectedFilter: StateFlow<LogFilter> = _selectedFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Stats
    val totalCount: StateFlow<Int> = repository.totalCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val savedCount: StateFlow<Int> = repository.savedCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val existingCount: StateFlow<Int> = repository.existingCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Filtered Call Logs
    val callLogs: StateFlow<List<CallLogEntity>> = combine(
        repository.allLogs,
        _selectedFilter,
        _searchQuery
    ) { logs, filter, query ->
        logs.filter { log ->
            val matchesFilter = when (filter) {
                LogFilter.ALL -> true
                LogFilter.SAVED -> log.status == CallActionStatus.SAVED.name || log.status == CallActionStatus.MANUAL_SAVED.name
                LogFilter.EXISTING -> log.status == CallActionStatus.ALREADY_EXISTS.name
                LogFilter.OTHER -> log.status == CallActionStatus.PRIVATE_NUMBER.name || log.status == CallActionStatus.ERROR.name
            }
            val matchesQuery = query.isBlank() ||
                    log.phoneNumber.contains(query, ignoreCase = true) ||
                    log.contactName.contains(query, ignoreCase = true) ||
                    log.details.contains(query, ignoreCase = true)

            matchesFilter && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Simulator State
    private val _isSimulating = MutableStateFlow(false)
    val isSimulating: StateFlow<Boolean> = _isSimulating.asStateFlow()

    private val _simulationResult = MutableStateFlow<SimulationResult?>(null)
    val simulationResult: StateFlow<SimulationResult?> = _simulationResult.asStateFlow()

    init {
        checkPermissions()
    }

    fun checkPermissions() {
        val phoneState = ContextCompat.checkSelfPermission(appContext, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
        val callLog = ContextCompat.checkSelfPermission(appContext, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
        val readContacts = ContextCompat.checkSelfPermission(appContext, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        val writeContacts = ContextCompat.checkSelfPermission(appContext, Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED
        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        val callScreening = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val roleManager = appContext.getSystemService(RoleManager::class.java)
                roleManager?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) == true
            } catch (e: Exception) {
                false
            }
        } else {
            true // Prior to Android 10, CallScreeningService role was not required
        }

        if (callLog) {
            CallLogObserver.register(appContext)
        }

        _permissions.value = PermissionStatuses(
            hasPhoneState = phoneState,
            hasCallLog = callLog,
            hasReadContacts = readContacts,
            hasWriteContacts = writeContacts,
            hasNotification = notification,
            hasCallScreeningRole = callScreening
        )
    }

    fun setAutoSaveEnabled(enabled: Boolean) {
        appSettings.setAutoSaveEnabled(enabled)
    }

    fun setNamingFormat(format: NamingFormat) {
        appSettings.setNamingFormat(format)
    }

    fun setCustomPrefix(prefix: String) {
        appSettings.setCustomPrefix(prefix)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        appSettings.setNotificationsEnabled(enabled)
    }

    fun setSkipPrivateNumbers(skip: Boolean) {
        appSettings.setSkipPrivateNumbers(skip)
    }

    fun setFilter(filter: LogFilter) {
        _selectedFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun deleteLog(id: Long) {
        viewModelScope.launch {
            repository.deleteLog(id)
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    fun updateContactName(id: Long, newName: String) {
        viewModelScope.launch {
            repository.updateContactName(id, newName)
        }
    }

    fun clearSimulationResult() {
        _simulationResult.value = null
    }

    /**
     * Verifies if a phone number exists in device contacts provider without saving.
     */
    fun verifyContactProviderOnly(inputNumber: String) {
        val rawNumber = inputNumber.trim()
        if (rawNumber.isBlank()) {
            _simulationResult.value = SimulationResult(
                processed = true,
                isSuccess = false,
                title = "Invalid Number",
                message = "Please enter a phone number to verify in contacts.",
                actionTaken = CallActionStatus.ERROR
            )
            return
        }

        checkPermissions()
        if (!_permissions.value.hasReadContacts) {
            _simulationResult.value = SimulationResult(
                processed = true,
                isSuccess = false,
                title = "READ_CONTACTS Permission Required",
                message = "Contacts permission is needed to query the device contacts provider. Please grant permission.",
                actionTaken = CallActionStatus.ERROR
            )
            return
        }

        viewModelScope.launch {
            _isSimulating.value = true
            try {
                val lookup = ContactHelper.findContactByNumber(appContext, rawNumber)
                if (lookup.exists) {
                    val name = lookup.displayName ?: "Saved Contact"
                    _simulationResult.value = SimulationResult(
                        processed = true,
                        isSuccess = true,
                        title = "Contact Found in Address Book",
                        message = "Phone number is already saved as '$name' (ID: ${lookup.contactId ?: "N/A"}). Lookup: ${lookup.details ?: "ContactsContract"}",
                        actionTaken = CallActionStatus.ALREADY_EXISTS
                    )
                } else if (lookup.permissionMissing) {
                    _simulationResult.value = SimulationResult(
                        processed = true,
                        isSuccess = false,
                        title = "Permission Missing",
                        message = "READ_CONTACTS permission is required to read device address book.",
                        actionTaken = CallActionStatus.ERROR
                    )
                } else {
                    _simulationResult.value = SimulationResult(
                        processed = true,
                        isSuccess = true,
                        title = "Number Not In Contacts",
                        message = "No matching contact found in device contacts provider. When a call arrives, it will be automatically saved.",
                        actionTaken = CallActionStatus.SAVED
                    )
                }
            } catch (e: Exception) {
                _simulationResult.value = SimulationResult(
                    processed = true,
                    isSuccess = false,
                    title = "Lookup Error",
                    message = "Failed to query device contacts provider: ${e.localizedMessage}",
                    actionTaken = CallActionStatus.ERROR
                )
            } finally {
                _isSimulating.value = false
            }
        }
    }

    /**
     * Executes the contact check & auto-save pipeline for a test or incoming number.
     * Verifies existence in the device contacts provider prior to saving.
     */
    fun testIncomingCall(inputNumber: String, isSimulation: Boolean = true) {
        val rawNumber = inputNumber.trim()
        if (rawNumber.isBlank()) {
            _simulationResult.value = SimulationResult(
                processed = true,
                isSuccess = false,
                title = "Invalid Number",
                message = "Please enter a valid phone number to test.",
                actionTaken = CallActionStatus.ERROR
            )
            return
        }

        checkPermissions()
        if (!_permissions.value.hasReadContacts || !_permissions.value.hasWriteContacts) {
            _simulationResult.value = SimulationResult(
                processed = true,
                isSuccess = false,
                title = "Contacts Permissions Missing",
                message = "Both READ_CONTACTS and WRITE_CONTACTS permissions are required to check and auto-save contacts.",
                actionTaken = CallActionStatus.ERROR
            )
            return
        }

        viewModelScope.launch {
            _isSimulating.value = true
            try {
                // 1. Verify if contact already exists in device contacts provider
                val lookup = ContactHelper.findContactByNumber(appContext, rawNumber)

                if (lookup.exists) {
                    val existingName = lookup.displayName ?: "Existing Contact"
                    repository.insertLog(
                        CallLogEntity(
                            phoneNumber = rawNumber,
                            contactName = existingName,
                            status = CallActionStatus.ALREADY_EXISTS.name,
                            details = "Contact '$existingName' was found in your phone contacts. Verified before saving - skipped to prevent duplicate.",
                            isSimulated = isSimulation
                        )
                    )
                    _simulationResult.value = SimulationResult(
                        processed = true,
                        isSuccess = true,
                        title = "Contact Already Exists",
                        message = "Found in your phone contacts as '$existingName'. Verified before saving: no changes made.",
                        actionTaken = CallActionStatus.ALREADY_EXISTS
                    )
                } else if (lookup.permissionMissing) {
                    _simulationResult.value = SimulationResult(
                        processed = true,
                        isSuccess = false,
                        title = "Permission Required",
                        message = "Please grant READ_CONTACTS permission to allow phonebook verification.",
                        actionTaken = CallActionStatus.ERROR
                    )
                } else {
                    // 2. Contact does not exist -> save it
                    val generatedName = appSettings.generateDisplayName(rawNumber)
                    val saved = ContactHelper.saveContact(appContext, rawNumber, generatedName)

                    if (saved) {
                        repository.insertLog(
                            CallLogEntity(
                                phoneNumber = rawNumber,
                                contactName = generatedName,
                                status = if (isSimulation) CallActionStatus.SAVED.name else CallActionStatus.MANUAL_SAVED.name,
                                details = "Verified not in contacts. Successfully saved as '$generatedName' to device phonebook.",
                                isSimulated = isSimulation
                            )
                        )
                        if (appSettings.notificationsEnabled.value) {
                            NotificationHelper.showContactSavedNotification(appContext, rawNumber, generatedName)
                        }
                        _simulationResult.value = SimulationResult(
                            processed = true,
                            isSuccess = true,
                            title = "New Contact Saved!",
                            message = "Verified number was not in your contact list. Successfully saved as '$generatedName'.",
                            actionTaken = CallActionStatus.SAVED
                        )
                    } else {
                        repository.insertLog(
                            CallLogEntity(
                                phoneNumber = rawNumber,
                                contactName = generatedName,
                                status = CallActionStatus.ERROR.name,
                                details = "Failed to write contact to device contacts. Ensure WRITE_CONTACTS permission is granted.",
                                isSimulated = isSimulation
                            )
                        )
                        _simulationResult.value = SimulationResult(
                            processed = true,
                            isSuccess = false,
                            title = "Save Failed",
                            message = "Could not write contact to address book. Please ensure WRITE_CONTACTS permission is granted.",
                            actionTaken = CallActionStatus.ERROR
                        )
                    }
                }
            } catch (e: Exception) {
                _simulationResult.value = SimulationResult(
                    processed = true,
                    isSuccess = false,
                    title = "Error",
                    message = "An error occurred: ${e.localizedMessage}",
                    actionTaken = CallActionStatus.ERROR
                )
            } finally {
                _isSimulating.value = false
            }
        }
    }

    class Factory(
        private val repository: CallLogRepository,
        private val appSettings: AppSettings,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CallSaverViewModel(repository, appSettings, context) as T
        }
    }
}
