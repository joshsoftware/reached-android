package com.joshsoftware.reached.ui.activity

import android.content.Intent
import android.os.Bundle
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.joshsoftware.core.BaseMapActivity
import com.joshsoftware.core.model.Address
import com.joshsoftware.core.model.IntentConstant
import com.joshsoftware.reached.R
import kotlinx.android.synthetic.main.activity_groups.titleTextView
import kotlinx.android.synthetic.main.activity_pick_location.*
import kotlinx.android.synthetic.main.layout_save_location_header.*
import timber.log.Timber

class PickLocationActivity : BaseMapActivity(), BaseMapActivity.OnBaseMapActivityReadyListener {

    private var address: Address? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick_location)
        Places.initialize(this, getString(R.string.places_api_key))
        setupMap()
        setupListeners()
        titleTextView.setText(getString(R.string.choose_location_on_map_title))
    }

    private fun setupListeners() {
        btnNext.setOnClickListener {
            startSaveLocationActivity(address)
        }
        txtBack.setOnClickListener {
            finish()
        }
        imgBack.setOnClickListener {
            finish()
        }
    }

    private fun startSaveLocationActivity(address: Address?) {
        val intent = Intent(this, SavePickedLocationActivity::class.java)
        intent.putExtra(IntentConstant.ADDRESS.name, address)
        startActivity(intent)
    }

    private fun setupMap() {
        listener = this
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.let {
            setMapFragment(it)
        }
        initializePlaces()
    }

    override fun initializeViewModel() {

    }

    override fun mapReady() {
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            if(location != null) {
                updateMapLocation(LatLng(location.latitude, location.longitude))
            }
        }
    }

    private fun initializePlaces() {
        val autocompleteFragment: AutocompleteSupportFragment = supportFragmentManager.findFragmentById(R.id.autocompleteFragment) as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(mutableListOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
        autocompleteFragment.setOnPlaceSelectedListener(object: PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // As we have specified the fields we can use !!
                address = Address(null, "", place.name!!, "enter", place.latLng?.latitude!!, place.latLng?.longitude!!)
                updateMapLocation(place.latLng)
            }

            override fun onError(p0: com.google.android.gms.common.api.Status) {
                Timber.e("Error with places result, status: $p0")
            }

        })
    }

    private fun updateMapLocation(latLng: LatLng?) {
        val cu = CameraUpdateFactory.newLatLng(latLng)
        map?.animateCamera(cu);
    }
}