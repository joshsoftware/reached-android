package com.joshsoftware.reached.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.joshsoftware.core.AppSharedPreferences
import com.joshsoftware.core.geofence.GeoConstants
import com.joshsoftware.core.geofence.GeofenceBroadcastReceiver
import com.joshsoftware.core.model.Address
import com.joshsoftware.core.model.Group
import com.joshsoftware.core.model.IntentConstant
import timber.log.Timber
import javax.inject.Inject

class GeofenceUtils @Inject constructor(var sharedPreferences: AppSharedPreferences,
                                        var geofencingClient: GeofencingClient,
                                        val application: Application
) {

    @SuppressLint("MissingPermission")
    fun addGeofences(list: MutableList<Group>, performPermissionCheck:  (() -> Unit) -> Unit) {
        sharedPreferences.userId?.let { userId ->

            val geofencingBuilder = GeofencingRequest.Builder()
            val addressList = mutableListOf<Address>()
            list.forEach { group ->
                group.members.forEach { (key, member) ->
                    if(key == userId) {
                        group.members[key]?.address?.forEach { (t, address) ->
                            geofencingClient.removeGeofences(mutableListOf(t))
                            val geofence = Geofence.Builder()
                                    .setRequestId(t)
                                    .setCircularRegion(
                                        address.lat,
                                        address.long,
                                        address.radius.toFloat())
                                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                                    .build()
                            geofencingBuilder.addGeofence(geofence)
                            val geofenceRequest = geofencingBuilder.build()
                            val intent = Intent(application.baseContext, GeofenceBroadcastReceiver::class.java)
                            intent.putExtra(IntentConstant.GROUP_ID.name, group.id)
                            intent.putExtra(IntentConstant.MEMBER_ID.name, key)
                            intent.action = GeoConstants.ACTION_GEO_FENCE
                            val pendingIntent = PendingIntent.getBroadcast(application.baseContext, 0, intent, PendingIntent.FLAG_ONE_SHOT)
                            performPermissionCheck {
                                geofencingClient.addGeofences(geofenceRequest, pendingIntent).addOnCompleteListener {
                                    if(it.isSuccessful) {
                                        Timber.e("Geofence added successfully")
                                    } else {
                                        Timber.e("Failed to add geofence")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}