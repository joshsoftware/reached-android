package com.joshsoftware.reached.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.viewpager2.widget.ViewPager2.ORIENTATION_HORIZONTAL
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.joshsoftware.reached.R
import com.joshsoftware.reached.databinding.DialogBottomSheetOnboardingBinding
import com.joshsoftware.reached.model.OnboardingData
import com.joshsoftware.reached.ui.adapter.OnboardingAdapter

class OnboardingBottomSheetDialogFragment: BottomSheetDialogFragment()  {
    private var bottomSheetBehavior: BottomSheetBehavior<View>? = null
    private lateinit var binding: DialogBottomSheetOnboardingBinding
    private var adapter: OnboardingAdapter? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogBottomSheetOnboardingBinding.inflate(LayoutInflater.from(context), null, false)

        binding?.apply {
            viewPagerOnboarding.orientation = ORIENTATION_HORIZONTAL
            adapter = OnboardingAdapter()
            viewPagerOnboarding.adapter = adapter
            adapter?.submitList(listOf(OnboardingData(R.drawable.ic_stat_name, "hello world"),
                OnboardingData(R.drawable.ic_stat_name, "hello world"),
                OnboardingData(R.drawable.ic_stat_name, "hello world")))
            dotsIndicator.setViewPager2(viewPagerOnboarding)
        }
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        bottomSheetDialog.setContentView(binding.root)
        bottomSheetBehavior = BottomSheetBehavior.from(binding.root.parent as View)
        bottomSheetBehavior?.peekHeight = BottomSheetBehavior.PEEK_HEIGHT_AUTO
        return bottomSheetDialog
    }

}