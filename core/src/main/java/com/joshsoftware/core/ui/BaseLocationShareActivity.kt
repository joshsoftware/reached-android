package com.joshsoftware.core.ui

import android.annotation.SuppressLint
import android.content.IntentSender
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.joshsoftware.core.PermissionActivity

private const val REQUEST_CHECK_SETTINGS: Int = 6
abstract class BaseLocationActivity: PermissionActivity(), GoogleApiClient.ConnectionCallbacks{
    private lateinit var mGoogleApiClient: GoogleApiClient
    private lateinit var mLocation: Location
    private lateinit var mLocationRequest: LocationRequest
    private var listener: LocationChangeListener? = null

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

    @SuppressLint("MissingPermission")
    override fun onConnected(p0: Bundle?) {
        mLocationRequest = LocationRequest().apply {
            interval = 10000
            fastestInterval = 10000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }


        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, com.google.android.gms.location.LocationListener {
            listener?.onLocationChange(it)
        })
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
                    exception.startResolutionForResult(this@BaseLocationActivity,
                        REQUEST_CHECK_SETTINGS)

                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error
                }
            }
        }
    }

    override fun onConnectionSuspended(p0: Int) {
        Toast.makeText(this, "Could not connect to google api", Toast.LENGTH_SHORT).show()
    }

    interface LocationChangeListener {
        fun onLocationChange(location: Location)
    }

    protected fun setLocationChangeListener(listener: LocationChangeListener) {
        this.listener = listener
    }
}