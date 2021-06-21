package com.joshsoftware.reached.ui.activity

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.view.MenuItem
import android.view.animation.AnticipateOvershootInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.material.tabs.TabLayoutMediator
import com.joshsoftware.core.AppSharedPreferences
import com.joshsoftware.core.geofence.GeoConstants
import com.joshsoftware.core.geofence.GeofenceBroadcastReceiver
import com.joshsoftware.core.model.Address
import com.joshsoftware.core.model.Group
import com.joshsoftware.core.model.IntentConstant
import com.joshsoftware.core.model.Member
import com.joshsoftware.core.ui.BaseActivity
import com.joshsoftware.reached.R
import com.joshsoftware.reached.ui.LoginActivity
import com.joshsoftware.reached.ui.adapter.HomeAdapter
import com.joshsoftware.reached.ui.dialog.JoinGroupDialog
import com.joshsoftware.reached.utils.InviteLinkUtils
import com.joshsoftware.reached.viewmodel.HomeViewModel
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.home_drawer_layout.*
import kotlinx.android.synthetic.main.layout_create_group.*
import kotlinx.android.synthetic.main.layout_reached_header.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject


class HomeActivity : BaseActivity(), HasSupportFragmentInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
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
        setContentView(R.layout.home_drawer_layout)

        val linkUtils = InviteLinkUtils()
        linkUtils.handleDynamicLinks(intent) {
            showJoinGroupAlertDialog(it)
        }

        geofencingClient = LocationServices.getGeofencingClient(this)
        setupViewPager()
        fetchGroups()

        txtSos.setOnClickListener {
            if(sharedPreferences.userId != null  && sharedPreferences.userData != null) {
                viewModel.sendSos(sharedPreferences.userId!!, sharedPreferences.userData!!).observe(this, Observer {
                    showToastMessage("Sos sent successfully!")
                })
            }
        }

        imgMenu.setOnClickListener {
            if(drawer_layout.isDrawerOpen(GravityCompat.START)) {
                drawer_layout.closeDrawer(GravityCompat.START)
            } else {
                drawer_layout.openDrawer(GravityCompat.START)
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

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
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
        }, { member, group ->
            startLocationActivity(member, group)
        },{ group ->
            startGroupEditActivity(group)
        }, { group, position ->
            showChoiceDialog("Do you ]want to delete this group?", {
                viewModel.deleteGroup(group).observe(this, {
                    sharedPreferences.userData?.apply {
                        groups.remove(group.id)
                        sharedPreferences.saveUserData(this)
                    }
                    adapter.notifyItemRemoved(position)
                    showToastMessage("Group was deleted successfully!")
                })
            })
        }) { group ->
            startQrCodeActivity(group)
        }
        homeViewPager.adapter = adapter
        TabLayoutMediator (dotsTabLayout, homeViewPager) { tab, pos ->

        }.attach()

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

    private fun startGroupEditActivity(group: Group) {
        val intent = Intent(this, GroupEditActivity::class.java)
        intent.putExtra(IntentConstant.GROUP.name, group)
        startActivity(intent)
    }

    private fun startLocationActivity(member: Member, group: Group) {
        val intent = Intent(this, MapActivity::class.java)
        intent.putExtra(IntentConstant.MEMBER.name, member)
        intent.putExtra(IntentConstant.GROUP.name, group)
        startActivity(intent)
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
            if(it.size == 0) {
                startGroupChoiceActivity()
            }
            if(!geofenceAdded) {
                addGeofences(it)
                geofenceAdded = true
            }
            adapter.submitList(it)
            adapter.notifyDataSetChanged()
        })

        viewModel.spinner.observe(this, {
            if(it) showProgressView() else hideProgressView()
        })
    }

    private fun startGroupChoiceActivity() {
        val intent = Intent(this, GroupChoiceActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    private fun updateCurrentAdapterList(group: Group) {
        adapter.notifyItemInserted(adapter.currentList.size - 1)
    }

    private fun showCreateGroupLayout() {
        val container = findViewById<ConstraintLayout>(R.id.container)
        val set = ConstraintSet()
        set.clone(container)
        set.clear(createGroupLayout.id, ConstraintSet.TOP)
        set.connect(createGroupLayout.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        val transition = ChangeBounds()
        transition.interpolator = AnticipateOvershootInterpolator(0.5f)
        transition.duration = 500
        TransitionManager.beginDelayedTransition(container, transition)
        set.applyTo(container)
    }

    private fun hideCreateGroup() {
        val container = findViewById<ConstraintLayout>(R.id.container)
        val set = ConstraintSet()
        set.clone(container)
        set.connect(createGroupLayout.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        set.clear(createGroupLayout.id, ConstraintSet.BOTTOM)
        val transition = ChangeBounds()
        transition.interpolator = AnticipateOvershootInterpolator(0.5f)
        transition.duration = 200
        TransitionManager.beginDelayedTransition(container, transition)
        set.applyTo(container)
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

    private fun showJoinGroupAlertDialog(group: Group) {
        val dialog = JoinGroupDialog.newInstance(group)
        dialog.show(supportFragmentManager, dialog.tag)
    }

    override fun supportFragmentInjector() = dispatchingAndroidInjector

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.logout -> {
                sharedPreferences.deleteUserData()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}