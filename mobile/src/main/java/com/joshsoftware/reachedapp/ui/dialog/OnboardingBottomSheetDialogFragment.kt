package com.joshsoftware.reachedapp.ui.dialog

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import androidx.viewpager2.widget.ViewPager2.ORIENTATION_HORIZONTAL
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.joshsoftware.core.AppSharedPreferences
import com.joshsoftware.reachedapp.R
import com.joshsoftware.reachedapp.databinding.DialogBottomSheetOnboardingBinding
import com.joshsoftware.reachedapp.model.OnboardingData
import com.joshsoftware.reachedapp.ui.LoginActivity
import com.joshsoftware.reachedapp.ui.adapter.OnboardingAdapter

class OnboardingBottomSheetDialogFragment: BottomSheetDialogFragment()  {
    private var bottomSheetBehavior: BottomSheetBehavior<View>? = null
    private lateinit var binding: DialogBottomSheetOnboardingBinding
    private var adapter: OnboardingAdapter? = null
    val handler = Handler()
    var pageCounter : Runnable? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogBottomSheetOnboardingBinding.inflate(LayoutInflater.from(context), null, false)

        binding.apply {
            context?.let { ctx ->
                AppSharedPreferences(ctx).setOnboardingShown(true)
            }

            viewPagerOnboarding.orientation = ORIENTATION_HORIZONTAL
            adapter = OnboardingAdapter()
            viewPagerOnboarding.adapter = adapter
            adapter?.submitList(listOf(OnboardingData(R.drawable.onboarding_one_background, R.drawable.onboarding_one, getString(R.string.onboarding_text_one)),
                OnboardingData(R.drawable.onboarding_two_background, R.drawable.onboarding_two, getString(R.string.onboarding_text_two)),
                OnboardingData(R.drawable.onboarding_three_background, R.drawable.onboarding_three, getString(R.string.onboarding_text_three)),
                OnboardingData(R.drawable.onboarding_four_background, R.drawable.onboarding_four, getString(R.string.onboarding_text_four))))
            dotsIndicator.setViewPager2(viewPagerOnboarding)
            pageCounter = object : Runnable {
                override fun run() {
                    if(!isDetached) {
                        if (viewPagerOnboarding.currentItem < 4) {
                            viewPagerOnboarding.currentItem++
                            if(viewPagerOnboarding.currentItem != 3) {
                                handler.postDelayed(this, 2 * 1000)
                            }
                        }
                    }
                }
            }
            handler.postDelayed(pageCounter!!, 2 * 1000)

            btnContinue.setOnClickListener {
                startLoginActivity()
            }
        }
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        bottomSheetDialog.setContentView(binding.root)
        bottomSheetDialog.window?.decorView?.findViewById<View>(R.id.design_bottom_sheet)?.setBackgroundColor(android.R.color.transparent)
        bottomSheetBehavior = BottomSheetBehavior.from(binding.root.parent as View)
        bottomSheetBehavior?.peekHeight = BottomSheetBehavior.PEEK_HEIGHT_AUTO
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        return bottomSheetDialog
    }

    private fun startLoginActivity() {
        val intent = Intent(context, LoginActivity::class.java)
        startActivity(intent)
    }


}