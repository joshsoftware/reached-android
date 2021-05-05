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

    private var listener: BaseActivityListener? = null

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
    protected fun checkForLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            listener?.onPermissionGrant()
            startLocationTrackingService()
        } else {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    10
                )
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
                    10
                )
            }

        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 10) {
            if (grantResults.isNotEmpty()) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (grantResults[0] === PackageManager.PERMISSION_GRANTED
                            && grantResults[1] === PackageManager.PERMISSION_GRANTED) {
                        if(grantResults[2] === PackageManager.PERMISSION_GRANTED) {
                            listener?.onPermissionGrant()
                            startLocationTrackingService()
                        } else {
                            showAlert("Permission Required", "You have to Allow location access all the time", 21)
                        }
                    } else {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                            // If User Checked 'Don't Show Again' checkbox for runtime permission, then navigate user to Settings
                            showAlert("Permission Required", "You have to Allow permission to access user location", 22)
                        }                    }
                } else {
                    if (grantResults[0] === PackageManager.PERMISSION_GRANTED
                            && grantResults[1] === PackageManager.PERMISSION_GRANTED) {
                        listener?.onPermissionGrant()
                        startLocationTrackingService()
                    } else {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                            // If User Checked 'Don't Show Again' checkbox for runtime permission, then navigate user to Settings
                            showAlert("Permission Required", "You have to Allow permission to access user location", 22)
                        }                    }
                }

            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // If User Checked 'Don't Show Again' checkbox for runtime permission, then navigate user to Settings
                    showAlert("Permission Required", "You have to Allow permission to access user location", 22)
                }
                //code for deny
            }
        }
    }

    private fun showAlert(title: String, message: String, requestCode: Int) {
        val dialog: AlertDialog.Builder = AlertDialog.Builder(this)
        dialog.setTitle(title)
        dialog.setCancelable(false)
        dialog.setMessage(message)
        dialog.setPositiveButton("Settings", DialogInterface.OnClickListener { dialog, which ->
            val i = Intent(
                ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts(
                    "package",
                    packageName, null
                )
            )
            //i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityForResult(i, requestCode)
        })
        val alertDialog: AlertDialog = dialog.create()
        alertDialog.show()
    }


    private fun startLocationTrackingService() {
        val intent = Intent(this, LocationUpdateService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }

    abstract fun attemptSignIn(account: GoogleSignInAccount)

    interface BaseActivityListener {
        fun onPermissionGrant()
    }

    protected fun setListener(listener: BaseActivityListener) {
        this.listener = listener
    }
}