package com.example

import android.app.Application
import com.example.data.db.AppDatabase
import com.example.data.preferences.AppSettings
import com.example.data.repository.CallLogRepository
import com.example.receiver.NotificationHelper

class AutoContactSaverApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: CallLogRepository
        private set

    lateinit var appSettings: AppSettings
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.getDatabase(this)
        repository = CallLogRepository(database.callLogDao())
        appSettings = AppSettings.getInstance(this)
        NotificationHelper.createNotificationChannel(this)
        com.example.receiver.CallLogObserver.register(this)
    }

    companion object {
        lateinit var instance: AutoContactSaverApp
            private set
    }
}
