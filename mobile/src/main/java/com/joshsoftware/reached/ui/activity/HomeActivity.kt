package com.joshsoftware.reached.ui.activity

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.view.View
import android.view.animation.AnticipateOvershootInterpolator
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.Observer
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
import kotlinx.android.synthetic.main.layout_create_group.*
import kotlinx.android.synthetic.main.layout_reached_header.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject


class HomeActivity : BaseActivity() {

    lateinit var adapter: HomeAdapter

    lateinit var viewModel: HomeViewModel
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var geofencingClient: GeofencingClient
    private var geofenceAdded: Boolean = false
    @Inject
    lateinit var sharedPreferences: AppSharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        geofencingClient = LocationServices.getGeofencingClient(this)
        setupViewPager()
        fetchGroups()

        txtSos.setOnClickListener {
            if(sharedPreferences.userId != null  && sharedPreferences.userData != null) {
                viewModel.sendSos(sharedPreferences.userId!!, sharedPreferences.userData!!, true)
            }
        }
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
                            updateCurrentAdapterList(it)
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
        }, { group ->

        }, { group, position ->
            showChoiceDialog("Do you ]want to delete this group?", {
                viewModel.deleteGroup(group).observe(this, {
                    showToastMessage("Group was deleted successfully!")
                })
            })
        }) { group ->
            startQrCodeActivity(group)
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
        intent.putExtra(IntentConstant.GROUP_ID.name, groupId)
        startActivity(intent)
    }


    private fun startMapActivity() {
        val intent = Intent(this, MapActivity::class.java)
        startActivity(intent)
    }

    override fun initializeViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory)[HomeViewModel::class.java]
        viewModel.result.observe(this, {
            if(!geofenceAdded) {
                addGeofences(it)
                geofenceAdded = true
            }
            adapter.submitList(it)
            adapter.notifyDataSetChanged()
            dotsIndicator.refreshDots()
        })

        viewModel.spinner.observe(this, {
            if(it) showProgressView() else hideProgressView()
        })
    }

    fun updateCurrentAdapterList(group: Group) {
        adapter.notifyItemInserted(adapter.currentList.size - 1)
        dotsIndicator.refreshDots()
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

            val geofencingBuilder = GeofencingRequest.Builder()
            val addressList = mutableListOf<Address>()
            list.forEach { group ->
                group.members.forEach { (key, member) ->
                    if(key == userId) {
                        group.members[key]?.address?.forEach { (t, address) ->
                            geofencingClient.removeGeofences(mutableListOf(t))
                            val geofence = Geofence.Builder()
                                    .setRequestId(t)
                                    .setCircularRegion(
                                            address.lat,
                                            address.long,
                                            address.radius.toFloat())
                                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                                    .build()
                            geofencingBuilder.addGeofence(geofence)
                            val geofenceRequest = geofencingBuilder.build()
                            val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
                            intent.putExtra(IntentConstant.GROUP_ID.name, group.id)
                            intent.putExtra(IntentConstant.MEMBER_ID.name, key)
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
            }


        }
    }
}