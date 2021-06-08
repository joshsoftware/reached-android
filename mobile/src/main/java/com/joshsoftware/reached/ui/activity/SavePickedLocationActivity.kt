package com.joshsoftware.reached.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.joshsoftware.core.model.Address
import com.joshsoftware.core.model.IntentConstant
import com.joshsoftware.reached.R
import kotlinx.android.synthetic.main.activity_save_picked_location.*
import kotlinx.android.synthetic.main.layout_save_location_header.*

class SavePickedLocationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save_picked_location)
        intent.extras?.getParcelable<Address>(IntentConstant.ADDRESS.name)?.let {
            setupUi(it)
        }
        setupListeners()
    }

    private fun setupListeners() {
        imgBack.setOnClickListener {
            finish()
        }
    }

    private fun setupUi(address: Address) {
        txtAddress.text = address.name
    }
}