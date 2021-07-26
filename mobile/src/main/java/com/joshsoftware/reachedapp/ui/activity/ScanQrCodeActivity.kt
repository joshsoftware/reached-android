package com.joshsoftware.reachedapp.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.joshsoftware.reachedapp.R

class ScanQrCodeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_qr_code)
    }
}