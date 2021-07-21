package com.joshsoftware.reached.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.joshsoftware.reached.R
import kotlinx.android.synthetic.main.dialog_wear_prominent_disclosure.*

class WearProminentDisclosureDialog : DialogFragment() {

    var listener: Listener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_wear_prominent_disclosure, container, false)
    }
    companion object {
        fun newInstance() = WearProminentDisclosureDialog()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buttonNegative.setOnClickListener {
            listener?.onNegativeClick()
            dismiss()
        }

        buttonPositive.setOnClickListener {
            listener?.onPositiveClick()
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        val params = dialog?.window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.MATCH_PARENT
        dialog?.window?.attributes = params as WindowManager.LayoutParams
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
    }
    interface Listener {
        fun onPositiveClick()
        fun onNegativeClick()
    }

}