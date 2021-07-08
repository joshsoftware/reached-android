package com.joshsoftware.reached.ui.activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.joshsoftware.core.AppSharedPreferences
import com.joshsoftware.core.BaseMapActivity
import com.joshsoftware.core.BuildConfig
import com.joshsoftware.core.model.Address
import com.joshsoftware.core.model.IntentConstant
import com.joshsoftware.core.model.RequestCodes
import com.joshsoftware.reached.R
import com.joshsoftware.reached.ui.SosMapActivity
import com.joshsoftware.reached.viewmodel.SosViewModel
import kotlinx.android.synthetic.main.activity_groups.titleTextView
import kotlinx.android.synthetic.main.activity_pick_location.*
import kotlinx.android.synthetic.main.layout_save_location_header.*
import timber.log.Timber
import javax.inject.Inject

class PickLocationActivity : SosMapActivity(), BaseMapActivity.OnBaseMapActivityReadyListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var sharedPreferences: AppSharedPreferences
    private var address: Address? = null
    lateinit var memberId: String
    lateinit var groupId: String
    var locationMarker: Marker? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick_location)
        if(BuildConfig.PLACES_ENABLED) {
            Places.initialize(this, getString(R.string.places_api_key))
            textViewLocationDragMessage.visibility= View.GONE
        } else {
            txtSave.visibility = View.VISIBLE
            textViewLocationDragMessage.visibility= View.VISIBLE
        }
        setupMap()
        handleIntent()
        setupListeners()
        titleTextView.setText(getString(R.string.choose_location_on_map_title))
    }

    private fun handleIntent() {
        intent.extras?.getString(IntentConstant.MEMBER_ID.name)?.let {
            memberId = it
        } ?: kotlin.run { throw Exception("Member id required for saving location") }

        intent.extras?.getString(IntentConstant.GROUP_ID.name)?.let {
            groupId = it
        } ?: kotlin.run { throw Exception("Group id required for saving location") }

    }

    private fun setupListeners() {
        addSosListener(txtSos, sharedPreferences)
        btnNext.setOnClickListener {
            startSaveLocationActivity(address)
        }
        txtSave.setOnClickListener {
            if(!BuildConfig.PLACES_ENABLED) {
                address = Address(null, "", "", "enter", locationMarker?.position?.latitude ?: 0.0, locationMarker?.position?.longitude ?: 0.0)
                startSaveLocationActivity(address)
            }
        }
        imgBack.setOnClickListener {
            finish()
        }
    }

    private fun startSaveLocationActivity(address: Address?) {
        val intent = Intent(this, SavePickedLocationActivity::class.java)
        intent.putExtra(IntentConstant.ADDRESS.name, address)
        intent.putExtra(IntentConstant.MEMBER_ID.name, memberId)
        intent.putExtra(IntentConstant.GROUP_ID.name, groupId)
        startActivityForResult(intent, RequestCodes.PICK_LOCATION.code)
    }

    private fun setupMap() {
        listener = this
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.let {
            setMapFragment(it)
        }
        if(BuildConfig.PLACES_ENABLED) {
            placesConstraintLayout.visibility = View.VISIBLE
            initializePlaces()
        }else {
            placesConstraintLayout.visibility = View.GONE
        }

    }

    override fun initializeViewModel() {
        sosViewModel = ViewModelProvider(this, viewModelFactory)[SosViewModel::class.java]
    }

    override fun mapReady() {
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            if(location != null) {
                val latLng = LatLng(location.latitude, location.longitude)
                updateMapLocation(latLng)
                if(!BuildConfig.PLACES_ENABLED) {
                    ContextCompat.getDrawable(this, R.drawable.marker_mini_map)?.let {
                        locationMarker = map?.addMarker(
                            MarkerOptions()
                                .position(latLng)
                                .draggable(true)
                                .icon(getMarkerIconFromDrawable(it)))
                        map?.setOnMarkerDragListener(object: GoogleMap.OnMarkerDragListener {
                            override fun onMarkerDragStart(p0: Marker?) {

                            }

                            override fun onMarkerDrag(p0: Marker?) {

                            }

                            override fun onMarkerDragEnd(p0: Marker?) {
                                locationMarker?.position = p0?.position
                            }
                        })
                    }
                }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK) {
            if(requestCode == RequestCodes.PICK_LOCATION.code) {
                data?.extras?.getParcelable<Address>(IntentConstant.ADDRESS.name)?.let {
                    val resultIntent = Intent()
                    resultIntent.putExtra(IntentConstant.ADDRESS.name, it)
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
            }
        }
    }

    private fun getMarkerIconFromDrawable(drawable: Drawable): BitmapDescriptor? {
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}