package com.joshsoftware.reached.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.joshsoftware.reached.R

class ScanQrCodeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_qr_code)
    }
}