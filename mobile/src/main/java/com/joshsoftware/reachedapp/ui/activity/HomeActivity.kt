package com.joshsoftware.reachedapp.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.view.animation.AnticipateOvershootInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationServices
import com.google.android.material.tabs.TabLayoutMediator
import com.joshsoftware.core.AppSharedPreferences
import com.joshsoftware.core.model.Group
import com.joshsoftware.core.model.IntentConstant
import com.joshsoftware.core.model.Member
import com.joshsoftware.reachedapp.R
import com.joshsoftware.reachedapp.ui.LoginActivity
import com.joshsoftware.reachedapp.ui.SosActivity
import com.joshsoftware.reachedapp.ui.adapter.HomeAdapter
import com.joshsoftware.reachedapp.ui.dialog.JoinGroupDialog
import com.joshsoftware.reachedapp.utils.GeofenceUtils
import com.joshsoftware.reachedapp.utils.InviteLinkUtils
import com.joshsoftware.reachedapp.viewmodel.HomeViewModel
import com.joshsoftware.reachedapp.viewmodel.SosViewModel
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.activity_group_member_mobile.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.home_drawer_layout.*
import kotlinx.android.synthetic.main.layout_create_group.*
import kotlinx.android.synthetic.main.layout_reached_header.*
import java.util.*
import javax.inject.Inject


class HomeActivity : SosActivity(), HasSupportFragmentInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    lateinit var adapter: HomeAdapter

    lateinit var viewModel: HomeViewModel
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var geofenceUtils: GeofenceUtils

    private var geofenceAdded: Boolean = false
    @Inject
    lateinit var sharedPreferences: AppSharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_drawer_layout)

        handleLeaveRequest(intent)
        setupViewPager()
        fetchGroups()

        addSosListener(txtSos, sharedPreferences)

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
                    getLastKnownLocation { lat, long ->
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
        navView.setNavigationItemSelectedListener { menuItem ->
            if(menuItem.itemId == R.id.nav_logout) {
                logout(sharedPreferences, LoginActivity::class.java)
            }
            true
        }
    }

    private fun handleLeaveRequest(intent: Intent?) {
        val requestId = intent?.extras?.getString(IntentConstant.REQUEST_ID.name)
        val groupId = intent?.extras?.getString(IntentConstant.GROUP_ID.name)
        val memberId = intent?.extras?.getString(IntentConstant.MEMBER_ID.name)
        val message = intent?.extras?.getString(IntentConstant.MESSAGE.name)
        showLeaveRequestDialog(requestId, groupId, memberId, message)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleLeaveRequest(intent)
    }

    private fun fetchGroups() {
        isNetWorkAvailable {
            sharedPreferences.userId?.let {
                viewModel.fetchGroups(it)
            }
        }
    }

    private fun setupViewPager() {
        adapter = HomeAdapter(sharedPreferences,
                              { member, groupId ->
                                  startProfileActivity(member, groupId)
                              }, { member, group ->
                                  startLocationActivity(member, group)
                              },{ group ->
                                  startGroupEditActivity(group)
                              }, { group, position ->
                                  if(sharedPreferences.userId == group.created_by) {
                                      showChoiceDialog("Do you want to delete this group?", {
                                          viewModel.deleteGroup(group).observe(this, {
                                              sharedPreferences.userData?.apply {
                                                  groups.remove(group.id)
                                                  sharedPreferences.saveUserData(this)
                                              }
                                              adapter.notifyItemRemoved(position)
                                              showToastMessage("Group was deleted successfully!")
                                          })
                                      })
                                  } else {
                                      showChoiceDialog("Do you want to leave this group?", {
                                          if(group.id != null && group.created_by != null && group.name != null) {
                                              viewModel.requestLeaveGroup(
                                                  group.id!!,
                                                  sharedPreferences.userId!!,
                                                  group.created_by!!,
                                                  sharedPreferences.userData?.name!!,
                                                  group.name!!
                                              )
                                          }
                                      })
                                  }

                              }, { group ->
                                  startQrCodeActivity(group)
                              })
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
        sosViewModel = ViewModelProvider(this, viewModelFactory)[SosViewModel::class.java]

        viewModel.result.observe(this, {
            if (it.size == 0) {
                val linkUtils = InviteLinkUtils()
                linkUtils.handleDynamicLinks(intent, {
                    showJoinGroupAlertDialog(it)
                }) {
                    startGroupChoiceActivity()
                }
            }
            if (!geofenceAdded) {

                sharedPreferences.userId?.let { userId ->
                    it.forEach { group ->
                        group.let { nonNullGroup ->
                            sharedPreferences.userId?.let {
                                if (it == nonNullGroup.created_by) {
                                    nonNullGroup.id?.let { it1 -> viewModel.getLeaveRequests(it1) }
                                }
                            }
                        }
                    }

                }
                geofenceUtils.addGeofences(it) { onPermissionCheck ->
                    requestPermission(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)) { status ->
                        if (status == Status.GRANTED) {
                            onPermissionCheck()
                        } else {
                            showToastMessage("Location Permission required!")
                        }
                    }
                }
                geofenceAdded = true
            }
            adapter.submitList(it)
            adapter.notifyDataSetChanged()
        })

        viewModel.leaveGroupRequest.observe(this, Observer { groupLeft ->
            groupLeft?.let {
                leaveOrDeleteGroupLabel.text = getString(R.string.request_sent)
                showToastMessage("Your request to leave the group has been sent successfully!")
            }
        })

        viewModel.leaveGroup.observe(this, Observer { groupLeft ->
            groupLeft?.let {
                showToastMessage("Removed successfully!")
            }
        })

        viewModel.leaveRequests.observe(this, Observer { requests ->
            requests?.forEach { leaveRequest ->
                showLeaveRequestDialog(
                    leaveRequest.requestId,
                    leaveRequest.group?.id,
                    leaveRequest.from?.id,
                    "${leaveRequest.from?.name} wants to leave the group. Remove from group?"
                )
            }
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
    }

    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation(onLastKnownFetch: (Double, Double) -> Unit) {
        requestPermission(arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                  android.Manifest.permission.ACCESS_FINE_LOCATION), action = {
            if(it == Status.GRANTED) {
                val client = LocationServices.getFusedLocationProviderClient(applicationContext)
                client.lastLocation.addOnSuccessListener { location ->
                    var lat = 0.0
                    var long = 0.0
                    if(location != null) {
                        lat = location.latitude
                        long = location.longitude
                    }
                    onLastKnownFetch(lat, long)
                }
            }
        })
    }

    private fun showJoinGroupAlertDialog(group: Group) {
        val dialog = JoinGroupDialog.newInstance(group)
        dialog.show(supportFragmentManager, dialog.tag)
    }

    private fun showLeaveRequestDialog(
        requestId: String?,
        groupId: String?,
        memberId: String?,
        message: String?
    ) {
        if(requestId != null && groupId != null && memberId != null && message != null) {
            showChoiceDialog(message,  {
                viewModel.leaveGroup(requestId, groupId, memberId)
            }, {
                                 viewModel.declineGroupLeaveRequest(requestId, memberId)
                             })
        }
    }
    override fun supportFragmentInjector() = dispatchingAndroidInjector

}