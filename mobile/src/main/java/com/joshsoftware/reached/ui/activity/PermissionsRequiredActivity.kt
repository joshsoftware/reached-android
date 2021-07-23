package com.joshsoftware.reached.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.joshsoftware.core.BaseLocationPermissionActivity
import com.joshsoftware.reached.databinding.ActivityPermissionsRequiredBinding
import com.joshsoftware.reached.ui.LoginActivity
import com.joshsoftware.reached.ui.dialog.LocationPremissionsDialog

class PermissionsRequiredActivity : BaseLocationPermissionActivity() {
    lateinit var binding: ActivityPermissionsRequiredBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPermissionsRequiredBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    override fun initializeViewModel() {
    }

    private fun init() {
        binding.apply {
            if (allLocationPermissionsNotGranted()) {
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
                showLocationAlertDialogPermission()
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
}