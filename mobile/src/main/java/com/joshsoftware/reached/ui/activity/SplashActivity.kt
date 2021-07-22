package com.joshsoftware.reached.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class
SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startLoginActivity()
        finish()
    }

    private fun startLoginActivity() {
        val intent = Intent(this, PermissionsRequiredActivity::class.java)
        startActivity(intent)
    }


}