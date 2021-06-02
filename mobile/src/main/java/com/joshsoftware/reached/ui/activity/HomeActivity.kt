package com.joshsoftware.reached.ui.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.view.View
import android.view.animation.AnticipateOvershootInterpolator
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationServices
import com.joshsoftware.core.AppSharedPreferences
import com.joshsoftware.core.model.Group
import com.joshsoftware.core.model.Member
import com.joshsoftware.core.ui.BaseActivity
import com.joshsoftware.reached.R
import com.joshsoftware.reached.ui.adapter.HomeAdapter
import com.joshsoftware.reached.viewmodel.HomeViewModel
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.home_view.*
import kotlinx.android.synthetic.main.layout_create_group.*
import java.util.*
import javax.inject.Inject


class HomeActivity : BaseActivity() {

    lateinit var adapter: HomeAdapter

    lateinit var viewModel: HomeViewModel
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var sharedPreferences: AppSharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
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
    }

    private fun fetchGroups() {
        isNetWorkAvailable {
            sharedPreferences.userId?.let {
                viewModel.fetchGroups(it)
            }
        }
    }

    private fun setupViewPager() {
        adapter = HomeAdapter(sharedPreferences, { member, id ->
            startMapActivity(member, id)
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

    private fun startMapActivity(member: Member, groupId: String) {
        val intent = Intent(this, MapActivity::class.java)
        intent.putExtra(INTENT_MEMBER_ID, member.id)
        intent.putExtra(INTENT_GROUP_ID, groupId)
        startActivity(intent)
    }

    override fun initializeViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory)[HomeViewModel::class.java]
        viewModel.result.observe(this, {
            adapter.submitList(it)
            adapter.notifyDataSetChanged()
            dotsIndicator.refreshDots()
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
    }
}