package com.joshsoftware.reached.ui.activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.joshsoftware.core.BaseMapActivity
import com.joshsoftware.core.model.Address
import com.joshsoftware.core.model.IntentConstant
import com.joshsoftware.reached.R
import com.joshsoftware.reached.viewmodel.SaveLocationViewModel
import kotlinx.android.synthetic.main.activity_save_picked_location.*
import kotlinx.android.synthetic.main.layout_save_location_header.*
import javax.inject.Inject


class SavePickedLocationActivity : BaseMapActivity(), BaseMapActivity.OnBaseMapActivityReadyListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var viewModel: SaveLocationViewModel

    lateinit var address: Address
    lateinit var memberId: String
    lateinit var groupId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save_picked_location)
        setupMapFragmnet()

//        intent.extras?.getParcelable<Address>(IntentConstant.ADDRESS.name)?.let {
//            address = it
//            setupUi(it)
//        }
        handleIntent()
        setupListeners()
    }

    private fun handleIntent() {
        intent.putExtra(IntentConstant.MEMBER_ID.name, "WVG6pD4AvbUwe2c03LkPgVfC1KU2")
        intent.putExtra(IntentConstant.GROUP_ID.name, "79a60b6f-a6fa-4f07-967d-bf140e6835cb")
        intent.putExtra(IntentConstant.ADDRESS.name, Address(null, "Test address", 18.5135, 73.7699, 100))
        intent.extras?.getString(IntentConstant.MEMBER_ID.name)?.let {
            memberId = it
        } ?: kotlin.run { throw Exception("Member id required for saving location") }

        intent.extras?.getString(IntentConstant.GROUP_ID.name)?.let {
            groupId = it
        } ?: kotlin.run { throw Exception("Group id required for saving location") }

        intent.extras?.getParcelable<Address>(IntentConstant.ADDRESS.name)?.let {
            address = it
            setupUi(address)
        } ?: kotlin.run { throw Exception("Address with lat, long and name required for saving location") }
    }

    private fun setupMapFragmnet() {
        listener = this
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.let {
            setMapFragment(it)
        }
    }

    override fun initializeViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory)[SaveLocationViewModel::class.java]

        viewModel.result.observe(this, { address ->
            if(address != null) {
                showToastMessage(getString(R.string.address_save_success_message))
                val resultIntent = Intent()
                resultIntent.putExtra(IntentConstant.ADDRESS.name, address)
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        })
        viewModel.error.observe(this, {
            if(it != null) showErrorMessage(it)
        })
        viewModel.spinner.observe(this,  {
            if(it) showProgressView() else hideProgressView()
        })
    }

    private fun setupListeners() {
        imgBack.setOnClickListener {
            finish()
        }
        txtSave.setOnClickListener {
            isNetWorkAvailable {
                viewModel.saveAddress(memberId, groupId, address)
            }
        }
    }

    private fun setupUi(address: Address) {
        txtAddress.text = address.name
    }

    override fun mapReady() {
        setZoom(16f)
        val latLng = LatLng(address.lat, address.long)
        updateMapLocation(latLng)
        addMarker(latLng)
    }

    private fun addMarker(latLng: LatLng) {
        ContextCompat.getDrawable(this, R.drawable.marker_mini_map)?.let {
            map?.addMarker(
                    MarkerOptions()
                            .position(latLng)
                            .icon(getMarkerIconFromDrawable(it)))
        }

    }

    private fun updateMapLocation(latLng: LatLng?) {
        val cu = CameraUpdateFactory.newLatLng(latLng)
        map?.animateCamera(cu);
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

