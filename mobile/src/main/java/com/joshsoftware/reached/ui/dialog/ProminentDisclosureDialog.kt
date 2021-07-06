package com.joshsoftware.reached.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.joshsoftware.reached.R
import com.joshsoftware.reached.databinding.DialogProminentDisclosureBinding

class ProminentDisclosureDialog: DialogFragment() {

    lateinit var binding: DialogProminentDisclosureBinding
    var listener: Listener? = null

    companion object {
        fun newInstance() = ProminentDisclosureDialog()
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = DialogProminentDisclosureBinding.inflate(LayoutInflater.from(context))
        context?.let { ctx ->

            binding.apply {

                buttonNegative.setOnClickListener {
                    dismiss()
                }

                buttonPositive.setOnClickListener {
                    listener?.onPositiveClick()
                }
            }


            val builder = MaterialAlertDialogBuilder(ctx, R.style.AlertDialogStyle)
            builder.setView(binding.root)
            val dialog = builder.create()
            dialog.setCanceledOnTouchOutside(false)
            dialog.setCancelable(false)
            dialog.show()

            return dialog
        }

        return super.onCreateDialog(savedInstanceState)

    }


    interface Listener {
        fun onPositiveClick()
    }

}