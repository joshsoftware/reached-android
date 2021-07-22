package com.joshsoftware.reached.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.joshsoftware.reached.databinding.ActivityPermissionsRequiredBinding
import com.joshsoftware.reached.ui.dialog.LocationPremissionsDialog

class PermissionsRequiredActivity : AppCompatActivity() {
    lateinit var binding: ActivityPermissionsRequiredBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPermissionsRequiredBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        showLocationAlertDialogPermission()
    }
    private fun showLocationAlertDialogPermission() {
        val dialog = LocationPremissionsDialog()
        dialog.show(supportFragmentManager, dialog.tag)
    }
}