package com.joshsoftware.reached.ui.dialog

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.ORIENTATION_HORIZONTAL
import androidx.viewpager2.widget.ViewPager2.SCREEN_STATE_ON
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.joshsoftware.reached.R
import com.joshsoftware.reached.databinding.DialogBottomSheetOnboardingBinding
import com.joshsoftware.reached.model.OnboardingData
import com.joshsoftware.reached.ui.adapter.OnboardingAdapter
import kotlinx.android.synthetic.main.dialog_bottom_sheet_onboarding.*

class OnboardingBottomSheetDialogFragment: BottomSheetDialogFragment()  {
    private var bottomSheetBehavior: BottomSheetBehavior<View>? = null
    private lateinit var binding: DialogBottomSheetOnboardingBinding
    private var adapter: OnboardingAdapter? = null
    val handler = Handler()
    var pageCounter : Runnable? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogBottomSheetOnboardingBinding.inflate(LayoutInflater.from(context), null, false)

        binding?.apply {
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
                            handler.postDelayed(this, 2 * 1000)
                        }
                    }
                }
            }
            handler.postDelayed(pageCounter!!, 2 * 1000)
        }
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        bottomSheetDialog.setContentView(binding.root)
        bottomSheetDialog.window?.decorView?.findViewById<View>(R.id.design_bottom_sheet)?.setBackgroundColor(android.R.color.transparent)
        bottomSheetBehavior = BottomSheetBehavior.from(binding.root.parent as View)
        bottomSheetBehavior?.peekHeight = BottomSheetBehavior.PEEK_HEIGHT_AUTO
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        return bottomSheetDialog
    }

}