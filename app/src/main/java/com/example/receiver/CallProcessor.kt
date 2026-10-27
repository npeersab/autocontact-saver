package com.example.receiver

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CallLog
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.contact.ContactHelper
import com.example.data.db.AppDatabase
import com.example.data.model.CallActionStatus
import com.example.data.model.CallLogEntity
import com.example.data.preferences.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * Centralized, deduplicated engine for processing incoming calls and auto-saving contacts.
 * Used by IncomingCallScreeningService, PhoneStateReceiver, and CallLogObserver.
 */
object CallProcessor {
    private const val TAG = "CallProcessor"
    private const val DEDUP_WINDOW_MS = 35_000L // 35 seconds window to deduplicate同一 call

    // Cache of normalizedNumber -> timestamp of when it was processed
    private val recentProcessedCalls = ConcurrentHashMap<String, Long>()

    /**
     * Processes an incoming call number, checking existence in contacts and saving if unknown.
     * Returns true if a new contact was saved.
     */
    suspend fun processIncomingCall(
        context: Context,
        rawNumber: String?,
        source: String = "AutoCall"
    ): Boolean = withContext(Dispatchers.IO) {
        val appSettings = AppSettings.getInstance(context)
        val database = AppDatabase.getDatabase(context)
        val dao = database.callLogDao()

        // 1. Verify master auto-save switch
        if (!appSettings.isAutoSaveEnabled.value) {
            Log.d(TAG, "[$source] Auto-save disabled in settings. Skipping.")
            return@withContext false
        }

        // 2. Validate phone number
        val number = rawNumber?.trim()
        if (number.isNullOrBlank() || number.equals("Private", ignoreCase = true) || number.equals("Unknown", ignoreCase = true)) {
            if (appSettings.skipPrivateNumbers.value) {
                Log.d(TAG, "[$source] Private/Unknown caller. Skipped.")
                dao.insertLog(
                    CallLogEntity(
                        phoneNumber = rawNumber ?: "Private / Unknown",
                        contactName = "Private Number",
                        status = CallActionStatus.PRIVATE_NUMBER.name,
                        details = "Unknown or withheld caller ID. Skipped according to user settings."
                    )
                )
            }
            return@withContext false
        }

        val normalized = ContactHelper.normalizePhoneNumber(number)
        val now = System.currentTimeMillis()

        // 3. Deduplicate across CallScreeningService, PhoneStateReceiver, and CallLogObserver
        val lastProcessedTime = recentProcessedCalls[normalized]
        if (lastProcessedTime != null && (now - lastProcessedTime) < DEDUP_WINDOW_MS) {
            Log.d(TAG, "[$source] Number $normalized was already processed ${now - lastProcessedTime}ms ago. Skipping duplicate processing.")
            return@withContext false
        }
        recentProcessedCalls[normalized] = now

        // Clean up old entries from dedup map
        if (recentProcessedCalls.size > 50) {
            val cutoff = now - DEDUP_WINDOW_MS
            recentProcessedCalls.entries.removeIf { it.value < cutoff }
        }

        // 4. Verify contact existence in device ContactsContract
        val lookup = ContactHelper.findContactByNumber(context, number)

        if (lookup.exists) {
            val existingName = lookup.displayName ?: "Existing Contact"
            Log.d(TAG, "[$source] Contact for $number already exists as '$existingName'. Skipped.")
            dao.insertLog(
                CallLogEntity(
                    phoneNumber = number,
                    contactName = existingName,
                    status = CallActionStatus.ALREADY_EXISTS.name,
                    details = "Contact '$existingName' verified in device contacts ($source). No modifications made."
                )
            )
            return@withContext false
        }

        // 5. Contact is not in address book -> Auto-save as new contact
        val displayName = appSettings.generateDisplayName(number)
        Log.d(TAG, "[$source] Auto-saving new contact for $number as '$displayName'")

        val savedSuccessfully = ContactHelper.saveContact(context, number, displayName)

        if (savedSuccessfully) {
            Log.d(TAG, "[$source] Successfully saved $number as '$displayName'")
            dao.insertLog(
                CallLogEntity(
                    phoneNumber = number,
                    contactName = displayName,
                    status = CallActionStatus.SAVED.name,
                    details = "Automatically saved to phone contacts via $source."
                )
            )

            if (appSettings.notificationsEnabled.value) {
                NotificationHelper.showContactSavedNotification(context, number, displayName)
            }
            return@withContext true
        } else {
            Log.e(TAG, "[$source] Failed to save contact for $number")
            dao.insertLog(
                CallLogEntity(
                    phoneNumber = number,
                    contactName = displayName,
                    status = CallActionStatus.ERROR.name,
                    details = "Failed to write contact to address book. Please verify WRITE_CONTACTS permission."
                )
            )
            return@withContext false
        }
    }

    /**
     * Checks CallLog.Calls for recent incoming, missed, or rejected calls (e.g. from the last 60 seconds).
     * Essential on Android 10+ where broadcast receiver may have received null EXTRA_INCOMING_NUMBER.
     */
    suspend fun checkCallLogForRecentIncomingCall(context: Context, lookbackMs: Long = 60_000L) = withContext(Dispatchers.IO) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "READ_CALL_LOG permission not granted. Cannot query CallLog.")
            return@withContext
        }

        try {
            val cutoffTime = System.currentTimeMillis() - lookbackMs
            val projection = arrayOf(
                CallLog.Calls._ID,
                CallLog.Calls.NUMBER,
                CallLog.Calls.DATE,
                CallLog.Calls.TYPE
            )
            val selection = "${CallLog.Calls.DATE} >= ? AND (${CallLog.Calls.TYPE} = ? OR ${CallLog.Calls.TYPE} = ? OR ${CallLog.Calls.TYPE} = ?)"
            val selectionArgs = arrayOf(
                cutoffTime.toString(),
                CallLog.Calls.INCOMING_TYPE.toString(),
                CallLog.Calls.MISSED_TYPE.toString(),
                CallLog.Calls.REJECTED_TYPE.toString()
            )
            val sortOrder = "${CallLog.Calls.DATE} DESC LIMIT 1"

            context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val numIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER)
                    val dateIndex = cursor.getColumnIndex(CallLog.Calls.DATE)
                    val typeIndex = cursor.getColumnIndex(CallLog.Calls.TYPE)

                    val number = if (numIndex != -1) cursor.getString(numIndex) else null
                    val date = if (dateIndex != -1) cursor.getLong(dateIndex) else 0L
                    val type = if (typeIndex != -1) cursor.getInt(typeIndex) else -1

                    val typeStr = when (type) {
                        CallLog.Calls.INCOMING_TYPE -> "Incoming"
                        CallLog.Calls.MISSED_TYPE -> "Missed"
                        CallLog.Calls.REJECTED_TYPE -> "Rejected"
                        else -> "Call"
                    }

                    Log.d(TAG, "Found recent $typeStr call from CallLog: $number at $date")
                    if (!number.isNullOrBlank()) {
                        processIncomingCall(context, number, "CallLog ($typeStr)")
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException querying CallLog: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error querying CallLog for recent calls", e)
        }
    }
}
