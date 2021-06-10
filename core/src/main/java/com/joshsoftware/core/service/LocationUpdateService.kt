package com.joshsoftware.core.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.content.IntentSender
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import com.google.android.gms.location.*
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.tasks.Task
import com.joshsoftware.core.AppSharedPreferences
import com.joshsoftware.core.R
import com.joshsoftware.core.repository.GroupRepository
import com.joshsoftware.core.ui.BaseLocationActivity
import dagger.android.AndroidInjection
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

const val CHANNEL_ID = "ReachedLocationService"
const val ACTION_LOCATION_RESOLUTION = "ACTION_LOCATION_RESOLUTION"
class LocationUpdateService: Service(), GoogleApiClient.ConnectionCallbacks {


    private lateinit var mGoogleApiClient: GoogleApiClient
    private lateinit var geoFenceClient: GeofencingClient
    private lateinit var mLocationRequest: LocationRequest
    private var listener: BaseLocationActivity.LocationChangeListener? = null

    @Inject
    lateinit var sharedPreferences: AppSharedPreferences

    @Inject
    lateinit var repository: GroupRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
        geoFenceClient = LocationServices.getGeofencingClient(this)
    }

    protected fun fetchLocation() {
        buildGoogleApiClient()
    }

    private fun buildGoogleApiClient() {
        synchronized(this) {
            mGoogleApiClient = GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addApi(LocationServices.API)
                    .build()
            mGoogleApiClient.connect()
        }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val input = intent!!.getStringExtra("inputExtra")
        createNotificationChannel()
        val notificationIntent = Intent()
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )

        val notification: Notification = Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle("Location service is running")
            .setContentText("Your loved one's are being updated about your location")
            .setContentIntent(pendingIntent)
            .build()
        startForeground(1, notification)

        fetchLocation()
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

    @SuppressLint("MissingPermission")
    override fun onConnected(p0: Bundle?) {
        Timber.i("Connected")
        mLocationRequest = LocationRequest().apply {
            interval = 1000 * 60 * 5
            fastestInterval = 1000 * 60 * 5
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }


        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest) {
            onLocationChanged(it)
        }

        val builder = LocationSettingsRequest.
        Builder()
        mLocationRequest.let {
            builder.addLocationRequest(it)
        }

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnFailureListener { exception ->
            if(exception is ResolvableApiException) {
                try {

                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error
                }
            }
        }
    }

    private fun onLocationChanged(location: Location) {
        Timber.i("Updated")

        val userId = sharedPreferences.userId
        sharedPreferences.userData?.groups?.forEach { (s, b) ->
            serviceScope.launch {
                repository.updateLocation(s, userId!!, location)
            }
        }
    }

    override fun onConnectionSuspended(p0: Int) {
        Toast.makeText(this, "Could not connect to google api", Toast.LENGTH_SHORT).show()
    }
}