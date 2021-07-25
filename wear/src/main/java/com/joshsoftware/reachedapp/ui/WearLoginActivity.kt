package com.joshsoftware.reachedapp.ui

import android.content.Intent
import android.os.Bundle
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.joshsoftware.core.AppSharedPreferences
import com.joshsoftware.core.BaseLocationPermissionActivity
import com.joshsoftware.core.di.AppType
import com.joshsoftware.core.ui.BaseLoginActivity
import com.joshsoftware.reachedapp.databinding.ActivityLoginBinding
import timber.log.Timber
import javax.inject.Inject

class WearLoginActivity : BaseLoginActivity(), BaseLocationPermissionActivity.PermissionListener {
    lateinit var binding: ActivityLoginBinding

    @Inject
    lateinit var sharedPreferences: AppSharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        listener = this

        if(sharedPreferences.userData != null) {
            sharedPreferences.userId?.let {
                if (sharedPreferences.userData!!.groups.isEmpty()) {
                    startGroupWaitActivity()
                } else {
                    startGroupListActivity()
                }
                finish()
            }
        }
        binding.btnGoogleSignIn.setOnClickListener {
//            isNetWorkAvailable {
//                if(sharedPreferences.userData == null) {
//                    signIn()
//                }
//            }
            if(!allLocationPermissionsNotGranted()) {
                val fragment = WearProminentDisclosureDialog().apply {
                    show(supportFragmentManager, "prominent")
                }
                fragment.listener = object: WearProminentDisclosureDialog.Listener {
                    override fun onPositiveClick() {
                        checkForLocationPermission()
                    }

                    override fun onNegativeClick() {
                        finish()
                    }
                }
            } else {
                listener?.onPermissionGrant()
            }
        }

        registerViewModelObservers()
    }

    override fun attemptSignIn(account: GoogleSignInAccount) {
        viewModel.signInWithGoogle(account, AppType.WEAR)
    }

    override fun askForPermission(account: GoogleSignInAccount) {
        if(!allLocationPermissionsNotGranted()) {
            val fragment = WearProminentDisclosureDialog().apply {
                show(supportFragmentManager, "prominent")
            }
            fragment.listener = object: WearProminentDisclosureDialog.Listener {
                override fun onPositiveClick() {
                    checkForLocationPermission()
                }

                override fun onNegativeClick() {
                    finish()
                }
            }
        } else {
            listener?.onPermissionGrant()
        }
    }

    private fun registerViewModelObservers() {
        viewModel.result.observe(this, { (id, user) ->
            sharedPreferences.saveUserId(id)
            sharedPreferences.saveUserData(user)
            viewModel.fetchUserDetails(id)
        })

        viewModel.user.observe(this, { user ->
            user?.let {
                sharedPreferences.saveUserData(user)
                if (user.groups.isEmpty()) {
                    startGroupWaitActivity()
                } else {
                    startGroupListActivity()
                }
                finish()
            }
        })

        viewModel.error.observe(this, { error ->
            Timber.d(error)
        })

        viewModel.spinner.observe(this, { loading ->
            if(loading) {
                showProgressView()
            } else {
                hideProgressView()
            }
        })
    }

    private fun startGroupWaitActivity() {
        val intent = Intent(this, GroupWaitActivity::class.java)
        startActivity(intent)
    }

    private fun startGroupListActivity() {
        val intent = Intent(this, WearGroupListActivity::class.java)
        startActivity(intent)
    }

    override fun onPermissionGrant() {
        startLocationTrackingService()
        if(sharedPreferences.userData != null) {
            sharedPreferences.userId?.let {
                if (sharedPreferences.userData!!.groups.isEmpty()) {
                    startGroupWaitActivity()
                } else {
                    startGroupListActivity()
                }
                finish()
            }
        } else {
            signIn()
        }
    }

    override fun onAllowAllTheTimeDenied() {
        TODO("Not yet implemented")
    }
}