package com.joshsoftware.reachedapp.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.joshsoftware.reachedapp.R
import com.joshsoftware.reachedapp.ui.dialog.OnboardingBottomSheetDialogFragment

class OnboardingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        showOnboardingDialog()
    }

    private fun showOnboardingDialog() {
        val bottomSheet = OnboardingBottomSheetDialogFragment()
        bottomSheet.show(supportFragmentManager, bottomSheet.tag)
    }
}