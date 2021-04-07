package com.joshsoftware.reached.ui

import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.joshsoftware.core.AppSharedPreferences
import com.joshsoftware.core.ui.BaseLoginActivity
import com.joshsoftware.reached.databinding.ActivityLoginMobileBinding
import com.joshsoftware.reached.service.LocationUpdateService
import com.joshsoftware.reached.ui.activity.GroupChoiceActivity
import com.joshsoftware.reached.ui.activity.GroupListActivity
import com.joshsoftware.reached.ui.activity.GroupMemberActivity
import com.joshsoftware.reached.ui.activity.INTENT_GROUP_ID
import timber.log.Timber
import javax.inject.Inject

class LoginActivity : BaseLoginActivity() {
    lateinit var binding: ActivityLoginMobileBinding

    @Inject
    lateinit var sharedPreferences: AppSharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginMobileBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        checkForLocationPermission()
        if(sharedPreferences.userData != null) {
            sharedPreferences.userId?.let {
                viewModel.fetchUserDetails(it)
            }
        }

        binding.btnGoogleSignIn.setOnClickListener {
            if(sharedPreferences.userData == null) {
                signIn()
            }
        }

        registerViewModelObservers()
    }

    private fun checkForLocationPermission() {
        requestPermission(arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION)) { coarseStatus ->
            if(coarseStatus == Status.GRANTED) {
                requestPermission(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)) { fineStatus ->
                    if (fineStatus == Status.GRANTED) {
                        requestPermission(arrayOf(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)) { backgroundStatus ->
                            if (backgroundStatus == Status.GRANTED) {
                                startLocationTrackingService()
                            } else if (backgroundStatus ==  Status.DENIED) {
                                checkForLocationPermission()
                            }
                        }
                    } else if (fineStatus ==  Status.DENIED) {
                        checkForLocationPermission()
                    }
                }
            } else if (coarseStatus ==  Status.DENIED) {
                checkForLocationPermission()
            }
        }
    }

    private fun startLocationTrackingService() {
        val intent = Intent(this, LocationUpdateService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }

    override fun attemptSignIn(account: GoogleSignInAccount) {
        viewModel.signInWithGoogle(account)
    }

    private fun registerViewModelObservers() {

        viewModel.result.observe(this, Observer { (id, user) ->
            sharedPreferences.saveUserId(id)
            sharedPreferences.saveUserData(user)
            viewModel.fetchUserDetails(id)
        })

        viewModel.user.observe(this, Observer { user ->
            user?.let {
                sharedPreferences.saveUserData(user)
                if (user.groups.isEmpty()) {
                    startGroupChoiceActivity()
                } else {
                    startGroupListActivity()
                }
                finish()
            }
        })

        viewModel.error.observe(this, Observer { error ->
            Timber.d(error)
        })

        viewModel.spinner.observe(this, Observer {loading ->
            if(loading) {
                showProgressView(binding.parent)
            } else {
                hideProgressView()
            }

        })
    }

    private fun startGroupListActivity() {
        val intent = Intent(this, GroupListActivity::class.java)
        startActivity(intent)
    }

    private fun startGroupChoiceActivity() {
        val intent = Intent(this, GroupChoiceActivity::class.java)
        startActivity(intent)
    }
}