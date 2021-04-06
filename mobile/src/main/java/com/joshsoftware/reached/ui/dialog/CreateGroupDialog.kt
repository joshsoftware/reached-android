package com.joshsoftware.reached.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.joshsoftware.core.AppSharedPreferences
import com.joshsoftware.core.di.Injectable
import com.joshsoftware.reached.R
import com.joshsoftware.reached.databinding.DialogCreateGroupBinding
import com.joshsoftware.reached.viewmodel.CreateGroupViewModel
import java.util.*
import javax.inject.Inject

class CreateGroupDialog: DialogFragment(), Injectable {
    private lateinit var binding: DialogCreateGroupBinding

    @Inject
    lateinit var sharedPreferences: AppSharedPreferences

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: CreateGroupViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogCreateGroupBinding.inflate(LayoutInflater.from(context))
        context?.let {

            binding.apply {
                buttonNegative.setOnClickListener {
                    dismiss()
                }

                buttonPositive.setOnClickListener {
                    val groupId = UUID.randomUUID().toString()
                    val id = sharedPreferences.userId
                    val user = sharedPreferences.userData
                    if (user != null) {
                        if (id != null) {
                            viewModel.createGroup(groupId, id, user)
                        }
                    }
                }
            }

            val builder = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogStyle)
            builder.setView(binding.root)
            val dialog = builder.create()
            dialog.setCanceledOnTouchOutside(false)
            dialog.setCancelable(false)
            dialog.show()
            return dialog
        }


        return super.onCreateDialog(savedInstanceState)

    }
}