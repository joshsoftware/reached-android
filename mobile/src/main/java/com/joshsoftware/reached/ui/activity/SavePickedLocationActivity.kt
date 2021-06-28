package com.joshsoftware.reached.ui.activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.joshsoftware.core.AppSharedPreferences
import com.joshsoftware.core.BaseMapActivity
import com.joshsoftware.core.model.Address
import com.joshsoftware.core.model.IntentConstant
import com.joshsoftware.reached.R
import com.joshsoftware.reached.model.Transition
import com.joshsoftware.reached.ui.SosMapActivity
import com.joshsoftware.reached.viewmodel.SaveLocationViewModel
import com.joshsoftware.reached.viewmodel.SosViewModel
import kotlinx.android.synthetic.main.activity_save_picked_location.*
import kotlinx.android.synthetic.main.layout_save_location_header.*
import javax.inject.Inject


class SavePickedLocationActivity : SosMapActivity(), BaseMapActivity.OnBaseMapActivityReadyListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var sharedPreferences: AppSharedPreferences

    lateinit var viewModel: SaveLocationViewModel

    lateinit var address: Address
    lateinit var memberId: String
    lateinit var groupId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save_picked_location)
        setupMapFragmnet()

        handleIntent()
        setupListeners()
    }

    private fun handleIntent() {
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
        sosViewModel = ViewModelProvider(this, viewModelFactory)[SosViewModel::class.java]

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
        addSosListener(txtSos, sharedPreferences)
        imgBack.setOnClickListener {
            finish()
        }
        txtSave.setOnClickListener {
            isNetWorkAvailable {
                if(TextUtils.isEmpty(edtLocation.text)) {
                    showToastMessage("Please enter a name for location")
                    return@isNetWorkAvailable
                }
                address.name = edtLocation.text.toString()
                viewModel.saveAddress(memberId, groupId, address)
            }
        }
        radioBtnHome.setOnClickListener {
            edtLocation.setText("Home")
        }
        radioBtnWork.setOnClickListener {
            edtLocation.setText("Work")
        }
    }

    private fun setupUi(address: Address) {
        edtLocation.addTextChangedListener {
            if(edtLocation.text.toString() != "Home") radioBtnHome.isChecked = false
            if(edtLocation.text.toString() != "Work") radioBtnWork.isChecked = false
        }

        if(address.address.isEmpty()) {
            txtAddressLabel.visibility = View.GONE
            txtAddress.visibility = View.GONE
        } else {
            txtAddressLabel.visibility = View.VISIBLE
            txtAddress.visibility = View.VISIBLE
        }
        txtAddress.text = address.address
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

