package com.joshsoftware.reached.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.joshsoftware.reached.ui.LoginActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startLoginActivity()
        finish()
    }

    private fun startLoginActivity() {
        val intent = Intent(this, OnboardingActivity::class.java)
        startActivity(intent)
    }


}