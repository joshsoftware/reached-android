package com.joshsoftware.reached.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.joshsoftware.reached.R
import kotlinx.android.synthetic.main.activity_privacy_policy.*
import kotlinx.android.synthetic.main.layout_save_location_header.*

class PrivacyPolicyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)
        txtSos.visibility = View.GONE
        titleTextView.text = getString(R.string.privacy_policy)
        webview.loadUrl("https://ndhabrde11.github.io")
        imgBack.setOnClickListener {
            onBackPressed()
        }
    }
}