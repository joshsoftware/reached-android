package com.joshsoftware.reached.ui.activity

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.joshsoftware.core.AppSharedPreferences
import com.joshsoftware.core.BaseMapActivity
import com.joshsoftware.core.model.Group
import com.joshsoftware.core.model.Member
import com.joshsoftware.core.viewmodel.MapViewModel
import com.joshsoftware.reached.R
import com.joshsoftware.reached.databinding.ActivityMapMobileBinding
import kotlinx.android.synthetic.main.activity_map_mobile.*
import kotlinx.android.synthetic.main.member_view.view.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import javax.inject.Inject


const val INTENT_MEMBER_ID = "MEMBER_ID"
class MapActivity: BaseMapActivity(), BaseMapActivity.OnBaseMapActivityReadyListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var sharedPreferences: AppSharedPreferences

    lateinit var viewModel: MapViewModel
    var memberId: String? = null
    var groupId: String? = null
    lateinit var binding: ActivityMapMobileBinding
    private var markers: ArrayList<Marker> = arrayListOf()
    private var showProgress = true
    private var groups = mutableListOf<Group>()
    private var currentPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapMobileBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        listener = this

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.let {
            setMapFragment(it)
        }

        handleIntentArguments()
        setupNavigationButtonListeners()
    }

    private fun setupNavigationButtonListeners() {
        imgNext.setOnClickListener {
            if(currentPosition < groups.size - 1) {
                setCurrentGroup(groups[++currentPosition])
            }
        }

        imgPrevious.setOnClickListener {
            if(currentPosition > 0) {
                setCurrentGroup(groups[--currentPosition])
            }
        }

        txtShowList.setOnClickListener {
            finish()
        }
    }

    private fun handleIntentArguments() {
        intent.extras?.getString(INTENT_MEMBER_ID)?.let {
            memberId = it
        }
        intent.extras?.getString(INTENT_GROUP_ID)?.let {
            groupId = it
        }
    }

    private fun setCurrentGroup(group: Group?) {
        group?.apply {
            txtGroupName.text = name
            removeMarkers()
            val deferreds = mutableListOf<Deferred<Boolean>>()
            lifecycleScope.launch {
                members.forEach { (k, v) ->
                    members[k]?.id = k
                    deferreds.add(addMarkerToMapFor(members[k]!!))
                }
                val list = deferreds.awaitAll()
                updateCamera()
            }

        }
    }

    override fun initializeViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory)[MapViewModel::class.java]

        viewModel.groups.observe(this, { groups ->
            if(showProgress) {
                hideProgressView()
                showProgress = false
            }
            if(!groups.isNullOrEmpty()) {
                this@MapActivity.groups = groups
                setCurrentGroup(groups[currentPosition])
            }
        })

        viewModel.result.observe(this, { member ->
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
//            map?.animateCamera(CameraUpdateFactory.newLatLngZoom(bounds.center, 12f));
        }

    }

    override fun mapReady() {
        showProgressView(binding.parent)
        if(memberId != null && groupId != null) {
            groupCard.visibility = View.GONE
            viewModel.observeLocationChanges(groupId!!, memberId!!)
        } else {
            sharedPreferences.userId?.let { viewModel.fetchGroups(it) }
        }
        setZoom(16f)
        animateCameraZoom()
    }

    private fun removeMarkers() {
        markers.forEach {
            it.remove()
        }
    }

    private fun addMarkerToMapFor(member: Member): CompletableDeferred<Boolean> {
        val deferred = CompletableDeferred<Boolean>()
        val view = (getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
                .inflate(R.layout.marker_view, null)
        val imageView = view.findViewById<ImageView>(R.id.imgProfile)
        if(member.profileUrl != null) {
            Glide.with(this).load(member.profileUrl).listener( object: RequestListener<Drawable?> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable?>?, isFirstResource: Boolean): Boolean {
                    createMarkerFrom(view, member)
                    deferred.complete(true)
                    return true
                }
                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable?>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    createMarkerFrom(view, member)
                    deferred.complete(true)
                    imageView.setImageDrawable(resource)
                    return true
                }
            }).into(imageView);
        } else {
            createMarkerFrom(view, member)
            deferred.complete(true)
        }
        return deferred
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
}