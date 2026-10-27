package com.example.service

import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.receiver.CallProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Standard Android 10+ (API 29+) CallScreeningService.
 * This service is called directly by Android Telecom when incoming calls arrive,
 * providing the incoming phone number in real time without blocking the call.
 */
@RequiresApi(Build.VERSION_CODES.N)
class IncomingCallScreeningService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        val direction = callDetails.callDirection
        Log.d(TAG, "onScreenCall received. Direction: $direction")

        // Build response allowing the call normally (do not block, reject, or silence)
        val response = CallResponse.Builder()
            .setDisallowCall(false)
            .setRejectCall(false)
            .setSkipCallLog(false)
            .setSkipNotification(false)
            .build()
        respondToCall(callDetails, response)

        if (direction == Call.Details.DIRECTION_INCOMING) {
            val handle = callDetails.handle
            val rawNumber = handle?.schemeSpecificPart
                ?: handle?.toString()?.removePrefix("tel:")

            Log.d(TAG, "Incoming call detected via CallScreeningService: $rawNumber")

            if (!rawNumber.isNullOrBlank()) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        CallProcessor.processIncomingCall(
                            context = applicationContext,
                            rawNumber = rawNumber,
                            source = "CallScreeningService"
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in CallProcessor from CallScreeningService", e)
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "CallScreeningService"
    }
}
