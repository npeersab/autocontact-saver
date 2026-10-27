package com.example.receiver

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.CallLog
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ContentObserver that monitors CallLog.Calls.CONTENT_URI.
 * When an incoming call completes or is missed, this immediately detects the caller
 * and triggers CallProcessor to auto-save it if not already handled.
 */
class CallLogObserver(
    private val context: Context,
    handler: Handler = Handler(Looper.getMainLooper())
) : ContentObserver(handler) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var debounceJob: Job? = null

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        Log.d(TAG, "CallLog changed: $uri")

        // Debounce multiple rapid content changes from the system
        debounceJob?.cancel()
        debounceJob = scope.launch {
            delay(500) // Brief delay to ensure provider transaction commit is complete
            try {
                CallProcessor.checkCallLogForRecentIncomingCall(context, lookbackMs = 45_000L)
            } catch (e: Exception) {
                Log.e(TAG, "Error in CallLogObserver processing", e)
            }
        }
    }

    companion object {
        private const val TAG = "CallLogObserver"
        private var observerInstance: CallLogObserver? = null

        fun register(context: Context) {
            try {
                if (observerInstance == null) {
                    val observer = CallLogObserver(context.applicationContext)
                    context.contentResolver.registerContentObserver(
                        CallLog.Calls.CONTENT_URI,
                        true,
                        observer
                    )
                    observerInstance = observer
                    Log.d(TAG, "CallLogObserver registered successfully.")
                }
            } catch (e: SecurityException) {
                Log.w(TAG, "Cannot register CallLogObserver without READ_CALL_LOG permission: ${e.message}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to register CallLogObserver", e)
            }
        }

        fun unregister(context: Context) {
            observerInstance?.let {
                try {
                    context.contentResolver.unregisterContentObserver(it)
                    observerInstance = null
                    Log.d(TAG, "CallLogObserver unregistered.")
                } catch (e: Exception) {
                    Log.e(TAG, "Error unregistering CallLogObserver", e)
                }
            }
        }
    }
}
