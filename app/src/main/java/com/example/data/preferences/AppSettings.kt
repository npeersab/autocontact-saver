package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class NamingFormat(val id: String, val title: String, val example: String) {
    CALLER_NUMBER("caller_number", "Caller + Number", "Caller +123456789"),
    DATE_TIME("date_time", "Call + Timestamp", "Call 29 Aug 14:30"),
    PREFIX_NUMBER("prefix_number", "Custom Prefix + Number", "Lead: +123456789"),
    UNKNOWN_CALLER("unknown_caller", "Auto Contact + Timestamp", "Auto Contact (29 Aug 14:30)")
}

class AppSettings(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("auto_contact_saver_prefs", Context.MODE_PRIVATE)

    private val _isAutoSaveEnabled = MutableStateFlow(prefs.getBoolean(KEY_AUTO_SAVE_ENABLED, true))
    val isAutoSaveEnabled: StateFlow<Boolean> = _isAutoSaveEnabled.asStateFlow()

    private val _namingFormat = MutableStateFlow(
        NamingFormat.values().find { it.id == prefs.getString(KEY_NAMING_FORMAT, NamingFormat.CALLER_NUMBER.id) }
            ?: NamingFormat.CALLER_NUMBER
    )
    val namingFormat: StateFlow<NamingFormat> = _namingFormat.asStateFlow()

    private val _customPrefix = MutableStateFlow(prefs.getString(KEY_CUSTOM_PREFIX, "New Lead") ?: "New Lead")
    val customPrefix: StateFlow<String> = _customPrefix.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true))
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _skipPrivateNumbers = MutableStateFlow(prefs.getBoolean(KEY_SKIP_PRIVATE_NUMBERS, true))
    val skipPrivateNumbers: StateFlow<Boolean> = _skipPrivateNumbers.asStateFlow()

    fun setAutoSaveEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SAVE_ENABLED, enabled).apply()
        _isAutoSaveEnabled.value = enabled
    }

    fun setNamingFormat(format: NamingFormat) {
        prefs.edit().putString(KEY_NAMING_FORMAT, format.id).apply()
        _namingFormat.value = format
    }

    fun setCustomPrefix(prefix: String) {
        val clean = prefix.trim().ifEmpty { "Call" }
        prefs.edit().putString(KEY_CUSTOM_PREFIX, clean).apply()
        _customPrefix.value = clean
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
        _notificationsEnabled.value = enabled
    }

    fun setSkipPrivateNumbers(skip: Boolean) {
        prefs.edit().putBoolean(KEY_SKIP_PRIVATE_NUMBERS, skip).apply()
        _skipPrivateNumbers.value = skip
    }

    fun generateDisplayName(phoneNumber: String): String {
        val now = Date()
        return when (_namingFormat.value) {
            NamingFormat.CALLER_NUMBER -> "Caller $phoneNumber"
            NamingFormat.DATE_TIME -> {
                val formatter = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
                "Call ${formatter.format(now)}"
            }
            NamingFormat.PREFIX_NUMBER -> "${_customPrefix.value} $phoneNumber"
            NamingFormat.UNKNOWN_CALLER -> {
                val formatter = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
                "Auto Contact (${formatter.format(now)})"
            }
        }
    }

    companion object {
        private const val KEY_AUTO_SAVE_ENABLED = "auto_save_enabled"
        private const val KEY_NAMING_FORMAT = "naming_format"
        private const val KEY_CUSTOM_PREFIX = "custom_prefix"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_SKIP_PRIVATE_NUMBERS = "skip_private_numbers"

        @Volatile
        private var INSTANCE: AppSettings? = null

        fun getInstance(context: Context): AppSettings {
            return INSTANCE ?: synchronized(this) {
                val instance = AppSettings(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
