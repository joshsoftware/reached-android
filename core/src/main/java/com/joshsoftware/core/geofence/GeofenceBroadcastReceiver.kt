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
import com.joshsoftware.core.model.IntentConstant
import com.joshsoftware.core.service.CHANNEL_ID
import com.joshsoftware.core.util.FirebaseDatabaseKey
import com.joshsoftware.core.util.FirebaseRealtimeDbManager
import com.joshsoftware.core.util.createNotificationChannel
import dagger.android.AndroidInjection
import kotlinx.coroutines.flow.combineTransform
import javax.inject.Inject

class GeofenceBroadcastReceiver(): BroadcastReceiver() {
    @Inject
    lateinit var dbManager: FirebaseRealtimeDbManager

    override fun onReceive(context: Context?, intent: Intent?) {
        AndroidInjection.inject(this, context);
        val geofenceIntent = GeofencingEvent.fromIntent(intent)
        val gId = intent?.extras?.getString(IntentConstant.GROUP_ID.name)
        val mId = intent?.extras?.getString(IntentConstant.MEMBER_ID.name)
        val address = intent?.extras?.getString(IntentConstant.ADDRESS.name)
        if(gId != null && mId != null) {
            if(geofenceIntent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                dbManager.groupReference.child(gId).child(FirebaseDatabaseKey.MEMBERS.key)
                    .child(mId)
                    .child(FirebaseDatabaseKey.ADDRESS.key)
                    .child(geofenceIntent.triggeringGeofences[0].requestId)
                        .child(FirebaseDatabaseKey.ENTERED.key).setValue(true)

                dbManager.groupReference.child(gId).child(FirebaseDatabaseKey.MEMBERS.key)
                    .child(mId)
                    .child(FirebaseDatabaseKey.LASTKNOWNADDRESS.key)
                    .setValue(address)
            } else {
                dbManager.groupReference.child(gId).child(FirebaseDatabaseKey.MEMBERS.key)
                        .child(mId)
                        .child(FirebaseDatabaseKey.ADDRESS.key)
                        .child(geofenceIntent.triggeringGeofences[0].requestId)
                        .child(FirebaseDatabaseKey.ENTERED.key).setValue(true)
            }
        }
    }
}