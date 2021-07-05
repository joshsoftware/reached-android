package com.joshsoftware.core.ui

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.joshsoftware.core.BaseLocationPermissionActivity
import com.joshsoftware.core.PermissionActivity
import com.joshsoftware.core.service.LocationUpdateService
import com.joshsoftware.core.viewmodel.LoginViewModel
import timber.log.Timber
import javax.inject.Inject


private const val RC_SIGN_IN = 1
open abstract class BaseLoginActivity: BaseLocationPermissionActivity() {


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
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 21 || requestCode == 22) {
            checkForLocationPermission()
        }
        else if(resultCode == Activity.RESULT_OK) {
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

    protected fun startLocationTrackingService() {
        val intent = Intent(this, LocationUpdateService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }

    abstract fun attemptSignIn(account: GoogleSignInAccount)

}