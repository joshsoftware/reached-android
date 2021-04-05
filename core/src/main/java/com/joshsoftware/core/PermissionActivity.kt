package com.joshsoftware.core

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.joshsoftware.core.ui.BaseActivity

abstract class PermissionActivity: BaseActivity() {
    private val TAG = PermissionActivity::class.simpleName
    private val FIRST_GROUP_PERMISSION_INDEX = 0
    private val permissionToActionMap = hashMapOf<String, (Status) -> Unit>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    protected fun requestPermission(permission: Array<out String>, action: (Status) -> Unit) {
        val permissionName = getPermissionLastComponent(permission[0])
        permissionToActionMap[permissionName] = action

        if(ContextCompat.checkSelfPermission(applicationContext,
                permission[FIRST_GROUP_PERMISSION_INDEX]) == PackageManager.PERMISSION_GRANTED) {
            logPermissionGrant(permission[FIRST_GROUP_PERMISSION_INDEX])
            action(Status.GRANTED)
        } else {
            logRequestingPermission(permission[FIRST_GROUP_PERMISSION_INDEX])
            ActivityCompat.requestPermissions(this,
                arrayOf(permission[FIRST_GROUP_PERMISSION_INDEX]),
                0)
            action(Status.REQUESTING)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[FIRST_GROUP_PERMISSION_INDEX] == PackageManager.PERMISSION_GRANTED) {
            logPermissionGrant(permissions[FIRST_GROUP_PERMISSION_INDEX])
            permissionToActionMap[getPermissionLastComponent(permissions[FIRST_GROUP_PERMISSION_INDEX])]?.invoke(Status.GRANTED)
        } else {
            permissionToActionMap[getPermissionLastComponent(permissions[FIRST_GROUP_PERMISSION_INDEX])]?.invoke(Status.DENIED)
            logPermissionDenied(permissions[FIRST_GROUP_PERMISSION_INDEX])
        }
    }



    private fun logPermissionGrant(permission: String) {
        val permissionText = getPermissionLastComponent(permission)
        Log.d(TAG,"${permissionText}: Permission granted")
    }

    private fun logRequestingPermission(permission: String) {
        val permissionText = getPermissionLastComponent(permission)
        Log.d(TAG,"${permissionText}: Requesting permission")
    }

    private fun logPermissionDenied(permission: String) {
        val permissionText = getPermissionLastComponent(permission)
        Log.e(TAG,"${permissionText}: Permission denied")
    }

    private fun getPermissionLastComponent(permission: String): String {
        return permission.substringAfterLast(".")
    }

    enum class Status {
        REQUESTING,
        GRANTED,
        DENIED
    }

}