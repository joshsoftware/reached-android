package com.joshsoftware.core

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.joshsoftware.core.ui.BaseActivity

abstract class BaseLocationPermissionActivity : BaseActivity() {

    protected var listener: PermissionListener? = null

    protected fun checkForLocationPermission() {
        if (allLocationPermissionsGranted()) {
            listener?.onPermissionGrant()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if(backgroundPermissionNotGranted()) {
                    listener?.onAllowAllTheTimeDenied()
                } else {
                    requestPermissions(
                        arrayOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ),
                        10
                    )
                }
            } else {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    10
                )
            }
        }
    }

    protected fun allLocationPermissionsGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    protected fun backgroundPermissionNotGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 10) {
            if (grantResults.isNotEmpty()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    ) {
                        if (grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                            listener?.onPermissionGrant()
                        } else {
                            listener?.onAllowAllTheTimeDenied()
                        }
                    } else {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            )
                        ) {
                            // If User Checked 'Don't Show Again' checkbox for runtime permission, then navigate user to Settings
                            showAlert("You have to Allow permission to access user location", 22)
                        }
                    }
                } else {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    ) {
                        listener?.onPermissionGrant()
                    } else {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            )
                        ) {
                            // If User Checked 'Don't Show Again' checkbox for runtime permission, then navigate user to Settings
                            showAlert("You have to Allow permission to access user location", 22)
                        }
                    }
                }

            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                ) {
                    // If User Checked 'Don't Show Again' checkbox for runtime permission, then navigate user to Settings
                    showAlert("You have to Allow permission to access user location", 22)
                }
                //code for deny
            }
        }
    }


    open fun showAlert(message: String, requestCode: Int) {
        val dialog: AlertDialog.Builder = AlertDialog.Builder(this)
        dialog.setTitle("Permission Required")
        dialog.setCancelable(false)
        dialog.setMessage(message)
        dialog.setPositiveButton("Settings") { _, _ ->
            val i = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts(
                    "package",
                    packageName, null
                )
            )
            //i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityForResult(i, requestCode)
        }
        val alertDialog: AlertDialog = dialog.create()
        alertDialog.show()
    }

    interface PermissionListener {
        fun onPermissionGrant()
        fun onAllowAllTheTimeDenied()
    }
}