package com.joshsoftware.core.geofence

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.joshsoftware.core.R
import com.joshsoftware.core.service.CHANNEL_ID
import com.joshsoftware.core.util.createNotificationChannel
import kotlinx.coroutines.flow.combineTransform

class GeofenceBroadcastReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val geofenceIntent = GeofencingEvent.fromIntent(intent)
        if (context != null) {
            createNotificationChannel(context)
            val manager = context.getSystemService(NotificationManager::class.java)
            showNotification(manager, context)
        }
    }

    private fun showNotification(manager: NotificationManager, context: Context) {
        val notification: Notification = if (versionGreaterThanO()) {
            Notification.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .setContentTitle("Geofence triggered")
                    .setContentText("Your loved one's are being updated about your location")
                    //                .setContentIntent(pendingIntent)
                    .build()
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        manager.notify (1, notification)
    }

    private fun versionGreaterThanO() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
}