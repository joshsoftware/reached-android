package com.joshsoftware.reached.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.joshsoftware.core.AppSharedPreferences
import com.joshsoftware.core.BuildConfig
import com.joshsoftware.core.LoginRepository
import com.joshsoftware.core.model.Group
import com.joshsoftware.core.model.NotificationPayload
import com.joshsoftware.core.model.NotificationType
import com.joshsoftware.reached.R
import com.joshsoftware.reached.ui.GroupMemberActivity
import com.joshsoftware.reached.ui.INTENT_GROUP_ID
import com.joshsoftware.reached.ui.INTENT_MEMBER_ID
import com.joshsoftware.reached.ui.MapActivity
import dagger.android.AndroidInjection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import timber.log.Timber
import javax.inject.Inject

const val CHANNEL_ID = "CHANNEL1"

class ReachedWearFirebaseService: FirebaseMessagingService() {
    val scope = CoroutineScope(Dispatchers.IO)

    @Inject
    lateinit var preferences: AppSharedPreferences

    @Inject
    lateinit var repository: LoginRepository


    override fun onCreate() {
        super.onCreate()
        AndroidInjection.inject(this)
    }

    override fun onNewToken(fcmToken: String) {
        super.onNewToken(fcmToken)
        Timber.d("FCM Token: $fcmToken")
        if(preferences.userData != null) {
            preferences.userData?.let { user ->
                user.token.phone = fcmToken
                preferences.saveUserData(user)
                preferences.userId?.let {
                    repository.updateUserPhoneToken(it, fcmToken)
                }
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Timber.d( "From: ${remoteMessage.from}")
        createNotificationChannel()

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Timber.d( "Message data payload: ${remoteMessage.data}")

            val data = remoteMessage.data["payload"]
            val type = remoteMessage.data["type"]

            sendNotification(remoteMessage.notification?.title, remoteMessage.notification?.body, getPendingIntent(data, type))

        }
    }


    private fun sendNotification(message: String?, title: String?, pendingIntent: PendingIntent?) {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        val notificationManager = NotificationManagerCompat.from(this)

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(1, builder.build())
    }
    private fun getPendingIntent(data: String?, type: String?): PendingIntent? {
        return when(type) {
            NotificationType.JOIN_GROUP.key -> {
                getGroupJoinPendingIntent(data)
            } else -> {
                getSosIntent(data)
            }
        }

    }

    private fun getGroupJoinPendingIntent(data: String?): PendingIntent? {
        val intent = Intent(this, GroupMemberActivity::class.java)
        getNotificationPayload(data)?.let {
            intent.putExtra(INTENT_GROUP_ID, it.groupId)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        return PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT)
    }


    private fun getNotificationPayload(data: String?): NotificationPayload? {
        return Gson().fromJson(data, NotificationPayload::class.java)
    }


    private fun getSosIntent(data: String?): PendingIntent? {
        val intent = Intent(this, MapActivity::class.java)
        getNotificationPayload(data)?.let {
            intent.putExtra(INTENT_GROUP_ID, it.groupId)
            intent.putExtra(INTENT_MEMBER_ID, it.memberId)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        return PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "Reached"
            val description = "Location update notification"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager?.createNotificationChannel(channel)
        }
    }
}