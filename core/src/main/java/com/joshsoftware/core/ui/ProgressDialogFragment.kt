package com.joshsoftware.core.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.joshsoftware.core.databinding.DialogProgressLayoutBinding

class ProgressDialogFragment: DialogFragment() {
    private lateinit var binding: DialogProgressLayoutBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogProgressLayoutBinding.inflate(LayoutInflater.from(context), container   , false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            lottieAnimationView.setAnimation("loader.json")
            lottieAnimationView.playAnimation()
        }
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }
}