package com.joshsoftware.reached.ui.activity

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.joshsoftware.core.BaseMapActivity
import com.joshsoftware.core.model.Member
import com.joshsoftware.core.viewmodel.MapViewModel
import com.joshsoftware.reached.R
import com.joshsoftware.reached.databinding.ActivityMapBinding
import javax.inject.Inject


const val INTENT_MEMBER_ID = "MEMBER_ID"
class MapActivity: BaseMapActivity(), BaseMapActivity.OnBaseMapActivityReadyListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: MapViewModel
    lateinit var groupId: String
    lateinit var memberId: String
    lateinit var binding: ActivityMapBinding
    private var markers: ArrayList<Marker> = arrayListOf()
    private var showProgress = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        listener = this
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.let {
            setMapFragment(it)
        }
        intent.extras?.getString(INTENT_MEMBER_ID)?.let {
            memberId = it
        }

        intent.extras?.getString(INTENT_GROUP_ID)?.let {
            groupId = it
        }
    }

    override fun initializeViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory)[MapViewModel::class.java]

        viewModel.result.observe(this, Observer { member ->
            if(showProgress) {
                hideProgressView()
                showProgress = false
            }
            removeMarkers()
            member?.let {
                addMarkerToMapFor(it)
                updateCamera()
            }
        })

        viewModel.members.observe(this, Observer { members ->
            if(showProgress) {
                hideProgressView()
                showProgress = false
            }
            removeMarkers()
            members.forEach {
                addMarkerToMapFor(it)
            }
            updateCamera()
        })
    }

    private fun updateCamera() {
        val builder = LatLngBounds.Builder()
        for (marker in markers) {
            builder.include(marker.position)
        }
        val bounds = builder.build()
        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        val padding = (width * 0.4).toInt() // offset from edges of the map 10% of screen


        val cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding)
        map?.moveCamera(cu)
    }

    override fun mapReady() {
        showProgressView(binding.parent)
        if(memberId.isNotEmpty()) {
            viewModel.observeLocationChanges(groupId, memberId)
        } else {
            viewModel.observeMembersForChanges(groupId)
        }
        setZoom(16f)
        animateCameraZoom()
    }

    private fun removeMarkers() {
        markers.forEach {
            it.remove()
        }
    }

    private fun addMarkerToMapFor(member: Member) {
        val memberPos = LatLng(member.lat!!, member.long!!)
        var marker = map?.addMarker(
            MarkerOptions()
                    .position(memberPos)
                    .title(member.name!!)
        )
        marker?.let { m ->
            markers.add(m)
        }
    }
}