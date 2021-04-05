package com.joshsoftware.core

import android.annotation.SuppressLint
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment

private const val DEFAULT_ZOOM: Float = 13.0f

@SuppressLint("Registered")
abstract class BaseMapActivity:  PermissionActivity(), OnMapReadyCallback {

    //region Map properties
    protected var map: GoogleMap? = null
    protected var listener: OnBaseMapActivityReadyListener? = null

    private lateinit var mapFragment: SupportMapFragment
    private var zoomLevel = DEFAULT_ZOOM

    //endregion Map properties

    /**
     * Without map fragment there cannot be any map, hence we get the [mapFragment] by
     * creating our own implementation of setMapFragment
     */
    fun setMapFragment(mapFragment: SupportMapFragment) {
        this.mapFragment = mapFragment
        this.mapFragment.getMapAsync(this)
    }

    fun setZoom(zoom: Float) {
        zoomLevel = zoom
    }

    protected fun animateCameraZoom() {
        map?.animateCamera(CameraUpdateFactory.zoomTo(zoomLevel))
    }

    protected fun maplistener(listener: OnBaseMapActivityReadyListener) {
        this.listener = listener
    }

    override fun onMapReady(p0: GoogleMap?) {
        map = p0
        listener?.mapReady()
    }

    interface OnBaseMapActivityReadyListener {
        fun mapReady()
    }

}