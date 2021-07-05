package com.joshsoftware.reached.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.joshsoftware.core.BaseMapActivity
import com.joshsoftware.core.model.Member
import com.joshsoftware.core.viewmodel.MapViewModel
import com.joshsoftware.reached.R
import com.joshsoftware.reached.databinding.ActivityMapBinding
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import javax.inject.Inject

class MapActivity : BaseMapActivity(), BaseMapActivity.OnBaseMapActivityReadyListener  {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: MapViewModel
    lateinit var groupId: String
    lateinit var memberId: String
    lateinit var binding: ActivityMapBinding
    private var markers: ArrayList<Marker> = arrayListOf()

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

        viewModel.result.observe(this, { member ->
            removeMarkers()
            member?.let {
                val result = addMarkerToMapFor(it)
                lifecycleScope.launch {
                    result.await()
                    updateCamera()
                }
            }
        })

        viewModel.members.observe(this, { members ->
            removeMarkers()
            members.forEach {
                val result = addMarkerToMapFor(it)
                lifecycleScope.launch {
                    result.await()
                    updateCamera()
                }
            }
        })
    }

    private fun updateCamera() {
        val builder = LatLngBounds.Builder()
        if(markers.size == 1) {
            val cu = CameraUpdateFactory.newLatLng(markers[0].position!!)
            map?.animateCamera(cu);
        } else {
            for (marker in markers) {
                builder.include(marker.position)
            }
            val bounds = builder.build()
            map?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));
        }
    }

    override fun mapReady() {
        map?.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.styled_map))
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

    private fun createMarkerFrom(view: View, member: Member) {
        try {
            val bitmap = createDrawableFromView(view)
            val memberPos = LatLng(member.lat!!, member.long!!)
            val marker = map?.addMarker(
                MarkerOptions()
                        .position(memberPos)
                        .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                        .title(member.name!!)
            )
            marker?.let { m ->
                markers.add(m)
            }
        } catch (e: Exception) {
            println("Exception ${e.localizedMessage}")
        }
    }
    private fun addMarkerToMapFor(member: Member): CompletableDeferred<Boolean> {
        val deferred = CompletableDeferred<Boolean>()
        val view = (getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
                .inflate(R.layout.marker_view, null)
        val imageView = view.findViewById<ImageView>(R.id.imgProfile)
        if(member.profileUrl.isNullOrEmpty().not()) {
            Glide
                    .with(this)
                    .asBitmap()
                    .load(member.profileUrl)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            val scale: Float = applicationContext.resources.displayMetrics.density
                            val pixels = (50 * scale + 0.5f).toInt()
                            val bitmap = Bitmap.createScaledBitmap(resource, pixels, pixels, true)
                            imageView.setImageBitmap(bitmap)
                            createMarkerFrom(view, member)
                            deferred.complete(true)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            imageView.setImageResource(R.drawable.ic_profile_placeholder);
                            createMarkerFrom(view, member)
                            deferred.complete(true)
                        }
                    });
        } else {
            imageView.setImageResource(R.drawable.ic_profile_placeholder);
            createMarkerFrom(view, member)
            deferred.complete(true)
        }
        return deferred
    }

}