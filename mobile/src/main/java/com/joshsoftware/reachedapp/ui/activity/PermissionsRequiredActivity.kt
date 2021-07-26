package com.joshsoftware.reachedapp.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.joshsoftware.core.BaseLocationPermissionActivity
import com.joshsoftware.reachedapp.databinding.ActivityPermissionsRequiredBinding
import com.joshsoftware.reachedapp.ui.LoginActivity
import com.joshsoftware.reachedapp.ui.dialog.LocationPremissionsDialog

class PermissionsRequiredActivity : BaseLocationPermissionActivity(),
    BaseLocationPermissionActivity.PermissionListener {
    lateinit var binding: ActivityPermissionsRequiredBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPermissionsRequiredBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        listener = this    }

    override fun initializeViewModel() {
    }

    private fun init() {
        binding.apply {
            if (allLocationPermissionsGranted()) {
                imageViewTick.visibility = View.VISIBLE
                enableLocation.visibility = View.GONE
                buttonGoToPermissionLocation.isEnabled = true
            } else {
                enableLocation.visibility = View.VISIBLE
                imageViewTick.visibility = View.GONE
                buttonGoToPermissionLocation.isEnabled = false
            }
            buttonGoToPermissionLocation.setOnClickListener {
                startLoginActivity()
            }
            enableLocation.setOnClickListener {
                checkForLocationPermission()
            }

        }

    }

    override fun onResume() {
        super.onResume()
        init()
    }

    private fun startLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun showLocationAlertDialogPermission() {
        val dialog = LocationPremissionsDialog()
        dialog.show(supportFragmentManager, dialog.tag)
    }

    override fun onPermissionGrant() {
        init()
    }

    override fun onAllowAllTheTimeDenied() {
        showLocationAlertDialogPermission()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 21 || requestCode == 22) {
            checkForLocationPermission()
        }
    }

}