package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.contact.ContactHelper

object NotificationHelper {
    const val CHANNEL_ID = "auto_contact_saver_alerts"
    private const val NOTIFICATION_ID_BASE = 1000

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_name)
            val descriptionText = context.getString(R.string.notification_channel_desc)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showContactSavedNotification(
        context: Context,
        phoneNumber: String,
        displayName: String
    ) {
        createNotificationChannel(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dialIntent = ContactHelper.getDialIntent(phoneNumber)
        val dialPendingIntent = PendingIntent.getActivity(
            context,
            1,
            dialIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_add)
            .setContentTitle("New Contact Auto-Saved")
            .setContentText("Saved $displayName ($phoneNumber) from incoming call")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Incoming caller $phoneNumber was automatically saved to your phone contacts as '$displayName'.")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(android.R.drawable.ic_menu_call, "Call", dialPendingIntent)
            .build()

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            val notificationId = NOTIFICATION_ID_BASE + (System.currentTimeMillis() % 10000).toInt()
            notificationManager.notify(notificationId, notification)
        } catch (e: SecurityException) {
            // Notification permission missing or revoked
        } catch (e: Exception) {
            // Notification dispatch error
        }
    }
}
