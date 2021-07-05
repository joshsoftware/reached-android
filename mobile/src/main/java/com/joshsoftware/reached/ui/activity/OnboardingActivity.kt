package com.joshsoftware.reached.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.joshsoftware.reached.R
import com.joshsoftware.reached.ui.dialog.OnboardingBottomSheetDialogFragment

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