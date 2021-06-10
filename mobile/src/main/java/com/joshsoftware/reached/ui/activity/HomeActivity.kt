package com.joshsoftware.reached.ui.activity

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.view.animation.AnticipateOvershootInterpolator
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.joshsoftware.core.AppSharedPreferences
import com.joshsoftware.core.geofence.GeoConstants
import com.joshsoftware.core.geofence.GeofenceBroadcastReceiver
import com.joshsoftware.core.model.Address
import com.joshsoftware.core.model.Group
import com.joshsoftware.core.model.IntentConstant
import com.joshsoftware.core.model.Member
import com.joshsoftware.core.ui.BaseActivity
import com.joshsoftware.reached.R
import com.joshsoftware.reached.ui.adapter.HomeAdapter
import com.joshsoftware.reached.viewmodel.HomeViewModel
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.home_view.*
import kotlinx.android.synthetic.main.layout_create_group.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject


class HomeActivity : BaseActivity() {

    lateinit var adapter: HomeAdapter

    lateinit var viewModel: HomeViewModel
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var geofencingClient: GeofencingClient

    @Inject
    lateinit var sharedPreferences: AppSharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        geofencingClient = LocationServices.getGeofencingClient(this)
        setupViewPager()
        fetchGroups()
        fabCreate.setOnClickListener {
            showCreateGroupLayout()
        }
        imgSliderTop.setOnClickListener {
            hideCreateGroup()
        }
        btnCreate.setOnClickListener {
            val groupName = groupEditText.text.toString()

            if(TextUtils.isEmpty(groupName)) {
                showToastMessage(getString(R.string.valid_please_enter_group_name))
                return@setOnClickListener
            }
            val groupId = UUID.randomUUID().toString()
            val id = sharedPreferences.userId
            val user = sharedPreferences.userData
            if (user != null) {
                if (id != null) {
                    val client = LocationServices.getFusedLocationProviderClient(applicationContext)
                    client.lastLocation.addOnSuccessListener { location ->
                        var lat = 0.0
                        var long = 0.0
                        if(location != null) {
                            lat = location.latitude
                            long = location.longitude
                        }
                        viewModel.createGroup(groupId, id, user, groupName, lat, long).observe(this, {
                            showToastMessage("Group created successfully!")
                        })
                    }
                }
            }
            hideCreateGroup()
        }

        txtShowOnMap.setOnClickListener {
            startMapActivity()
        }
    }


    private fun fetchGroups() {
        isNetWorkAvailable {
            sharedPreferences.userId?.let {
                viewModel.fetchGroups(it)
            }
        }
    }

    private fun setupViewPager() {
        adapter = HomeAdapter(sharedPreferences, { member, groupId ->
            startProfileActivity(member, groupId)
        }) {
            startQrCodeActivity(it)
        }
        homeViewPager.adapter = adapter
        dotsIndicator.setViewPager2(homeViewPager)

        val pageMargin = resources.getDimensionPixelOffset(R.dimen.page_margin).toFloat()
        val pageOffset = resources.getDimensionPixelOffset(R.dimen.offset).toFloat()
        homeViewPager.setPageTransformer { page, position ->
            val myOffset: Float = position * -(2 * pageOffset + pageMargin)
            if (position < -1) {
                page.translationX = -myOffset
            } else if (position <= 1) {
                page.translationX = myOffset
            } else {
                page.translationX = myOffset
            }
        }
    }

    private fun startProfileActivity(member: Member, groupId: String) {
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra(IntentConstant.MEMBER.name, member)
        intent.putExtra(INTENT_GROUP_ID, groupId)
        startActivity(intent)
    }


    private fun startMapActivity() {
        val intent = Intent(this, MapActivity::class.java)
        startActivity(intent)
    }

    override fun initializeViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory)[HomeViewModel::class.java]
        viewModel.result.observe(this, {
            if(it.isNotEmpty()) {
                addGeofences(it)
            }
            adapter.submitList(it)
            adapter.notifyDataSetChanged()
            dotsIndicator.refreshDots()
        })

        viewModel.spinner.observe(this, {
            if(it) showProgressView() else hideProgressView()
        })
    }


    private fun showCreateGroupLayout() {
        val set = ConstraintSet()
        set.clone(parentLayout)
        set.clear(createGroupLayout.id, ConstraintSet.TOP)
        set.connect(createGroupLayout.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        val transition = ChangeBounds()
        transition.interpolator = AnticipateOvershootInterpolator(0.5f)
        transition.duration = 500
        TransitionManager.beginDelayedTransition(parentLayout, transition)
        set.applyTo(parentLayout)
    }

    private fun hideCreateGroup() {
        val set = ConstraintSet()
        set.clone(parentLayout)
        set.connect(createGroupLayout.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        set.clear(createGroupLayout.id, ConstraintSet.BOTTOM)
        val transition = ChangeBounds()
        transition.interpolator = AnticipateOvershootInterpolator(0.5f)
        transition.duration = 200
        TransitionManager.beginDelayedTransition(parentLayout, transition)
        set.applyTo(parentLayout)
    }

    private fun startQrCodeActivity(group: Group) {
        val intent = Intent(this, QrCodeActivity::class.java)
        intent.putExtra(INTENT_GROUP, group)
        startActivity(intent)
    }private fun addGeofences(list: MutableList<Group>) {
        sharedPreferences.userId?.let { userId ->
            val addressList = getAddressListForUserId(userId, list)
            geofencingClient.removeGeofences(addressList.map { it.id })

            val geofencingBuilder = GeofencingRequest.Builder()
            addressList.forEach { address ->
                val geofence = Geofence.Builder()
                        .setRequestId(address.id!!)
                        .setCircularRegion(
                                address.lat,
                                address.long,
                                address.radius.toFloat())
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                        .build()
                geofencingBuilder.addGeofence(geofence)

            }
            if(addressList.isNotEmpty()) {
                val geofenceRequest = geofencingBuilder.build()
                val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
                intent.action = GeoConstants.ACTION_GEO_FENCE
                val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
                geofencingClient.addGeofences(geofenceRequest, pendingIntent).addOnCompleteListener {
                    if(it.isSuccessful) {
                        Timber.e("Geofence added successfully")
                    } else {
                        Timber.e("Failed to add geofence")
                    }
                }
            }

        }
    }

    private fun getAddressListForUserId(userId: String, list: MutableList<Group>): MutableList<Address> {
        val addressList = mutableListOf<Address>()
        list.forEach {
            it.members.forEach { (key, value) ->
                if(key == userId) {
                    it.members[key]?.address?.forEach { (t, u) ->
                        it.members[key]!!.address[t]!!.id = t
                        addressList.add(it.members[key]!!.address[t]!!)
                    }
                }
            }
        }
        return addressList
    }
}