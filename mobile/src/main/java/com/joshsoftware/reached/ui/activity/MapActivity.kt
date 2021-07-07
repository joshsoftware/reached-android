package com.joshsoftware.reached.ui.activity

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.joshsoftware.core.AppSharedPreferences
import com.joshsoftware.core.BaseMapActivity
import com.joshsoftware.core.model.Group
import com.joshsoftware.core.model.IntentConstant
import com.joshsoftware.core.model.Member
import com.joshsoftware.core.viewmodel.MapViewModel
import com.joshsoftware.reached.R
import com.joshsoftware.reached.databinding.ActivityMapMobileBinding
import com.joshsoftware.reached.ui.SosMapActivity
import com.joshsoftware.reached.utils.Constants
import com.joshsoftware.reached.viewmodel.SosViewModel
import kotlinx.android.synthetic.main.activity_map_mobile.*
import kotlinx.android.synthetic.main.activity_map_mobile.txtSos
import kotlinx.android.synthetic.main.layout_reached_header.*
import kotlinx.android.synthetic.main.member_view.view.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import javax.inject.Inject


const val INTENT_MEMBER_ID = "MEMBER_ID"

class MapActivity : SosMapActivity(), BaseMapActivity.OnBaseMapActivityReadyListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var sharedPreferences: AppSharedPreferences

    lateinit var viewModel: MapViewModel
    var member: Member? = null
    var group: Group? = null
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
        addSosListener(txtSos, sharedPreferences)

        handleIntentArguments(intent)
        setupNavigationButtonListeners()
    }

    private fun setupNavigationButtonListeners() {
        if (groups.size == 1) {
            imgNext.visibility = View.GONE
            imgPrevious.visibility = View.GONE
        }
        imgNext.setOnClickListener {
            if (currentPosition < groups.size - 1) {
                setCurrentGroup(groups[++currentPosition])
                updateArrows()
            }
        }

        imgPrevious.setOnClickListener {
            if (currentPosition > 0) {
                setCurrentGroup(groups[--currentPosition])
                updateArrows()
            }
        }

        txtShowList.setOnClickListener {
            onBackPressed()
        }
    }

    private fun handleIntentArguments(intent: Intent?) {
        intent?.extras?.getParcelable<Member>(IntentConstant.MEMBER.name)?.let {
            member = it
        }
        intent?.extras?.getParcelable<Group>(IntentConstant.GROUP.name)?.let {
            group = it
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntentArguments(intent)
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
        sosViewModel = ViewModelProvider(this, viewModelFactory)[SosViewModel::class.java]

        viewModel.groups.observe(this, { groups ->
            if (showProgress) {
                hideProgressView()
                showProgress = false
            }
            if (!groups.isNullOrEmpty()) {
                this@MapActivity.groups = groups
                setCurrentGroup(groups[currentPosition])
                updateArrows()
            }
        })

        viewModel.result.observe(this, { member ->
            if (showProgress) {
                hideProgressView()
                showProgress = false
            }
            removeMarkers()

            member?.let {
                lifecycleScope.launch {
                    val result = addMarkerToMapFor(it)
                    result.await()
                    updateCamera()
                }
            }
        })
    }

    private fun updateCamera() {
        val builder = LatLngBounds.Builder()
        if (markers.size == 1) {
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

    private fun updateArrows() {
        when {
            currentPosition + 1 >= groups.size -> {
                imgNext.isEnabled = false
                imgNext.alpha = Constants.ALPHA_FADE
                imgPrevious.alpha = Constants.ALPHA_VALUE_1
                imgPrevious.isEnabled = true
            }
            currentPosition - 1 < 0 -> {
                imgNext.isEnabled = true
                imgNext.alpha = Constants.ALPHA_VALUE_1
                imgPrevious.alpha = Constants.ALPHA_FADE
                imgPrevious.isEnabled = false

            }
            else -> {
                imgNext.isEnabled = true
                imgNext.alpha = Constants.ALPHA_VALUE_1
                imgPrevious.alpha = Constants.ALPHA_VALUE_1
                imgPrevious.isEnabled = true
            }
        }
    }

    override fun mapReady() {
        showProgressView()
        if (member != null && group != null) {
            viewModel.fetchMemberData(member?.id!!, group?.id!!).observe(this, { member ->
                setupGeofence(member!!)
                if (member?.id != sharedPreferences.userId) {
                    imgMarkSafe.setOnClickListener {
                        if (imgMarkSafe.tag == R.drawable.unsafe) {
                            viewModel.markSafe(member?.id!!, member!!).observe(this, {
                                showToastMessage("Marked safe successfully!")
                                imgMarkSafe.setImageResource(R.drawable.safe)
                                imgMarkSafe.tag = R.drawable.safe
                                txtMember.text = "${member!!.name}"
                                txtMember.setTextColor(Color.BLACK)
                            })
                        }
                    }
                }
                groupCard.visibility = View.GONE
                memberCard.visibility = View.VISIBLE
                var color = Color.RED
                val text = if (member!!.sosState) {
                    imgMarkSafe.tag = R.drawable.unsafe
                    txtSos.visibility = View.GONE
                    "${member!!.name} needs help"
                } else {
                    imgMarkSafe.tag = R.drawable.safe
                    txtSos.visibility = View.VISIBLE
                    color = Color.BLACK
                    "${member!!.name}"
                }
                txtMember.text = text
                txtMember.setTextColor(color)
                imgMarkSafe.setImageResource(if (member!!.sosState) R.drawable.unsafe else R.drawable.safe)
                viewModel.observeLocationChanges(group?.id!!, member?.id!!)
            })
        } else if (member != null) {
            groupCard.visibility = View.GONE
            memberCard.visibility = View.VISIBLE
            viewModel.observeLocationChanges(group?.id!!, member?.id!!)
        } else {
            sharedPreferences.userId?.let { viewModel.fetchGroups(it) }
        }
        setZoom(16f)
        animateCameraZoom()
    }

    private fun setupGeofence(member: Member) {
        member.address.forEach { (_, address) ->
            val circleOptions = CircleOptions()
                .center(LatLng(address.lat, address.long))
                .radius(address.radius.toDouble())
                .fillColor(0x40ff0000)
                .strokeColor(Color.TRANSPARENT)
                .strokeWidth(2f);
            map?.addCircle(circleOptions)
        }

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
        if (member.profileUrl.isNullOrEmpty().not()) {
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

    override fun onBackPressed() {
        finish()
    }
}