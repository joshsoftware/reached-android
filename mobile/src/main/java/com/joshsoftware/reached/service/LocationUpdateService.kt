package com.joshsoftware.reached.service

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import com.joshsoftware.core.AppSharedPreferences
import com.joshsoftware.reached.ui.LoginActivity
import dagger.android.AndroidInjection
import javax.inject.Inject

const val CHANNEL_ID = "ReachedLocationService"
class LocationUpdateService: Service() {

    @Inject
    lateinit var sharedPreferences: AppSharedPreferences

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val input = intent!!.getStringExtra("inputExtra")
        createNotificationChannel()
        val notificationIntent = Intent(this, LoginActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )

        val notification: Notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Reached")
            .setContentText(input)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(1, notification)

        return START_NOT_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Reached Location Service",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(serviceChannel)
        }
    }
}