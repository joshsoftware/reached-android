package com.joshsoftware.core.ui

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.joshsoftware.core.PermissionActivity
import com.joshsoftware.core.service.LocationUpdateService
import com.joshsoftware.core.viewmodel.LoginViewModel
import timber.log.Timber
import javax.inject.Inject

private const val RC_SIGN_IN = 1
open abstract class BaseLoginActivity: PermissionActivity() {


    lateinit var viewModel: LoginViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        googleSignInClient.signOut()
    }


    override fun initializeViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]
    }

    protected fun signIn() {
//        FirebaseCrashlytics.getInstance().log(FirebaseConstants.LOGS.GOOGLE_SIGN_IN_CLICKED)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK) {
            if(requestCode == RC_SIGN_IN) {
                Timber.d("Google Sign in success")
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)
                account?.let {
                    isNetWorkAvailable {
                        attemptSignIn(it)
                    }
                }
            }
        }
    }
    protected fun checkForLocationPermission() {
        requestPermission(arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION)) { coarseStatus ->
            if(coarseStatus == Status.GRANTED) {
                requestPermission(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)) { fineStatus ->
                    if (fineStatus == Status.GRANTED) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            requestPermission(arrayOf(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)) { backgroundStatus ->
                                if (backgroundStatus == Status.GRANTED) {
                                    signIn()
                                    startLocationTrackingService()
                                } else if (backgroundStatus ==  Status.DENIED) {
                                    checkForLocationPermission()
                                }
                            }
                        } else {
                            signIn()
                            startLocationTrackingService()
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

    abstract fun attemptSignIn(account: GoogleSignInAccount)
}