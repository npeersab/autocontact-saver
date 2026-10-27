package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver for telephony state events.
 * Handles ACTION_PHONE_STATE_CHANGED on all Android versions:
 * - On older versions / OEM devices: extracts number from EXTRA_INCOMING_NUMBER at RINGING.
 * - On Android 10+: detects RINGING and IDLE (call ended/missed), inspecting CallLog when number extra is withheld.
 */
class PhoneStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d(TAG, "onReceive action: $action")

        if (action == Intent.ACTION_BOOT_COMPLETED) {
            CallLogObserver.register(context)
            return
        }

        if (action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return

        val stateStr = intent.getStringExtra(TelephonyManager.EXTRA_STATE) ?: return
        Log.d(TAG, "PhoneState changed: $stateStr")

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (stateStr) {
                    TelephonyManager.EXTRA_STATE_RINGING -> {
                        // Attempt to extract incoming number from any available intent extra
                        val incomingNumber = extractIncomingNumber(intent)
                        Log.d(TAG, "RINGING event. incomingNumber: $incomingNumber")

                        if (!incomingNumber.isNullOrBlank()) {
                            CallProcessor.processIncomingCall(
                                context = context,
                                rawNumber = incomingNumber,
                                source = "PhoneStateReceiver (Ringing)"
                            )
                        } else {
                            Log.d(
                                TAG,
                                "RINGING received with null/withheld number (Android 10+ privacy protection). CallScreeningService or IDLE fallback will handle."
                            )
                        }
                    }

                    TelephonyManager.EXTRA_STATE_IDLE -> {
                        // Call ended, was answered & hung up, or was missed.
                        // On Android 10+, the call is now committed to the system CallLog provider.
                        Log.d(TAG, "IDLE event received. Checking CallLog for recent caller on Android 10+...")
                        delay(600) // Brief delay to ensure system CallLog entry has committed
                        CallProcessor.checkCallLogForRecentIncomingCall(context, lookbackMs = 60_000L)
                    }

                    TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                        Log.d(TAG, "OFFHOOK event received (call in progress).")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling PhoneState changed", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun extractIncomingNumber(intent: Intent): String? {
        val standard = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
        if (!standard.isNullOrBlank()) return standard

        // Some Android 10 / OEM custom ROMs place the number in alternate intent extras
        val extras = intent.extras ?: return null
        val candidates = listOf(
            "incoming_number",
            "phone_number",
            "phoneNumber",
            "android.telephony.extra.INCOMING_NUMBER"
        )
        for (key in candidates) {
            val value = extras.getString(key)
            if (!value.isNullOrBlank()) return value
        }
        return null
    }

    companion object {
        private const val TAG = "PhoneStateReceiver"
    }
}
