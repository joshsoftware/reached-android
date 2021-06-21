package com.joshsoftware.reached.ui

import android.os.Bundle
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.joshsoftware.core.AppSharedPreferences
import com.joshsoftware.core.BaseMapActivity
import com.joshsoftware.core.ui.BaseActivity
import com.joshsoftware.reached.viewmodel.SosViewModel
import javax.inject.Inject

abstract class SosMapActivity: BaseMapActivity() {

    protected lateinit var sosViewModel: SosViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    protected fun addSosListener(textView: TextView, sharedPreferences: AppSharedPreferences) {
        textView.setOnClickListener {
            if(sharedPreferences.userId != null  && sharedPreferences.userData != null) {
                sosViewModel.sendSos(sharedPreferences.userId!!, sharedPreferences.userData!!).observe(this, Observer {
                    showToastMessage("Sos sent successfully!")
                })
            }
        }
    }
}