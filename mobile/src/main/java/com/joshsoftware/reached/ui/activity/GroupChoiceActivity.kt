package com.joshsoftware.reached.ui.activity

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.text.TextUtils
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.view.View
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationServices
import com.google.zxing.integration.android.IntentIntegrator
import com.joshsoftware.core.AppSharedPreferences
import com.joshsoftware.core.model.Group
import com.joshsoftware.core.ui.BaseLocationActivity
import com.joshsoftware.reached.R
import com.joshsoftware.reached.databinding.ActivityGroupChoiceBinding
import com.journeyapps.barcodescanner.CaptureActivity
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import java.util.*
import javax.inject.Inject


class GroupChoiceActivity : BaseLocationActivity(), BaseLocationActivity.LocationChangeListener, HasSupportFragmentInjector {

    lateinit var binding: ActivityGroupChoiceBinding

    @Inject
    lateinit var sharedPreferences: AppSharedPreferences

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: GroupChoiceViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupChoiceBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.apply {
            createButton.setOnClickListener {
                showCreateGroupLayout()
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
                            viewModel.createGroup(groupId, id, user, groupName, lat, long)
                        }
                    }
                }
            }
            imgSliderTop.setOnClickListener {
                hideCreateGroup()
            }
            joinButton.setOnClickListener {
                requestPermission(arrayOf(android.Manifest.permission.CAMERA), action = {
                    if(it == Status.GRANTED) {
                        IntentIntegrator(this@GroupChoiceActivity)
                                .setCaptureActivity(CaptureActivity::class.java)
                                .setOrientationLocked(true)
                                .initiateScan(); // `this` is the current Activity
                    }
                })
            }

        }
    }

    override fun initializeViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory)[GroupChoiceViewModel::class.java]

        viewModel.result.observe(this, androidx.lifecycle.Observer{ id ->
            id?.let {
                startGroupMemberActivity()
                finish()
            }
        })

        viewModel.spinner.observe(this, androidx.lifecycle.Observer { loading ->
            if(loading) {
                showProgressView()
            } else {
                hideProgressView()
            }
        })
    }

    private fun startGroupMemberActivity() {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
    }

    private fun startQrCodeActivity(id: String) {
        val intent = Intent(this, QrCodeActivity::class.java)
        intent.putExtra(INTENT_GROUP_ID, id)
        startActivity(intent)
    }

    // Get the results:
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        val result =
                IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            } else {
                val groupId = result.contents

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
                            viewModel.joinGroup(groupId, id, user, lat, long)
                                .observe(this, androidx.lifecycle.Observer {
                                    startGroupMembersActivity(result.contents)
                                })
                        }
                    }
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onLocationChange(location: Location) {

    }

    private fun startGroupMembersActivity(groupId: String) {
        val intent = Intent(this, GroupMemberActivity::class.java)
        intent.putExtra(INTENT_GROUP, Group(groupId))
        startActivity(intent)
        finish()
    }

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return dispatchingAndroidInjector
    }

    private fun showCreateGroupLayout() {
        binding.apply {
            val set = ConstraintSet()
            set.clone(parent)
            set.setVisibility(createButton.id, View.GONE)
            set.setVisibility(joinButton.id, View.GONE)
            set.clear(bottomConstraintLayout.id, ConstraintSet.TOP)
            set.connect(bottomConstraintLayout.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
            val transition = ChangeBounds()
            transition.interpolator = AnticipateOvershootInterpolator(0.5f)
            transition.duration = 500
            TransitionManager.beginDelayedTransition(parent, transition)
            set.applyTo(parent)
        }
    }



    private fun hideCreateGroup() {
        binding.apply {
            val set = ConstraintSet()
            set.clone(parent)
            set.connect(bottomConstraintLayout.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
            set.clear(bottomConstraintLayout.id, ConstraintSet.BOTTOM)
            set.setVisibility(createButton.id, View.VISIBLE)
            set.setVisibility(joinButton.id, View.VISIBLE)
            val transition = ChangeBounds()
            transition.interpolator = AnticipateOvershootInterpolator(0.5f)
            transition.duration = 200
            TransitionManager.beginDelayedTransition(parent, transition)
            set.applyTo(parent)
        }
    }

}