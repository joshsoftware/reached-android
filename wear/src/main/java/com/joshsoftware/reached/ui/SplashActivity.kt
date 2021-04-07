package com.joshsoftware.reached.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startLoginActivity()
        finish()
    }

    private fun startLoginActivity() {
        val intent = Intent(this, WearLoginActivity::class.java)
        startActivity(intent)
    }


}