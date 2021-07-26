package com.joshsoftware.reachedapp.ui.dialog

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.animation.AnticipateOvershootInterpolator
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.ViewModelProvider
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.joshsoftware.core.AppSharedPreferences
import com.joshsoftware.core.model.Group
import com.joshsoftware.core.ui.BaseDialogFragment
import com.joshsoftware.reachedapp.R
import com.joshsoftware.reachedapp.databinding.DialogCreateGroupBinding
import com.joshsoftware.reachedapp.ui.activity.GroupMemberActivity
import com.joshsoftware.reachedapp.ui.activity.INTENT_GROUP
import com.joshsoftware.reachedapp.viewmodel.CreateGroupViewModel
import javax.inject.Inject

class CreateGroupDialog: BaseDialogFragment() {
    private var constraintSet  = ConstraintSet()

    private lateinit var binding: DialogCreateGroupBinding

    @Inject
    lateinit var sharedPreferences: AppSharedPreferences

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: CreateGroupViewModel

    var listener: CreateGroupDialogListener? = null


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        initializeViewModel()

        binding = DialogCreateGroupBinding.inflate(LayoutInflater.from(context))
        context?.let {

            binding.apply {
                buttonNegative.setOnClickListener {
                    dismiss()
                }

                buttonPositive.setOnClickListener {

                }
            }

            val builder = MaterialAlertDialogBuilder(it, R.style.AlertDialogStyle)
            builder.setView(binding.root)
            val dialog = builder.create()
            dialog.setCanceledOnTouchOutside(false)
            dialog.setCancelable(false)
            dialog.show()

            return dialog
        }


        return super.onCreateDialog(savedInstanceState)

    }

    override fun initializeViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory)[CreateGroupViewModel::class.java]

        viewModel.result.observe(this, { result ->
            result?.let {
                startGroupMemberActivity(it)
                dismiss()
            }
        })

        viewModel.spinner.observe(this, { loading ->
            loading?.let {
                if (it) showProgressBar(binding) else hideProgressBar()
            }
        })

        viewModel.error.observe(this, { error ->
            error?.let {
                showErrorMessage(it)
            }
        })
    }

    private fun startGroupMemberActivity(group: Group) {
        if (activity != null) {
            val intent = Intent(activity, GroupMemberActivity::class.java)
            intent.putExtra(INTENT_GROUP, group)
            startActivity(intent)
            listener?.onCreate()
        }
    }

    private fun showProgressBar(binding: DialogCreateGroupBinding) {
        binding.apply {
            dialogTitleTextView.setText("Creating group...")
            constraintSet.clone(parentLayout)
            constraintSet.clear(groupEditTextLayout.id, ConstraintSet.END)
            constraintSet.connect(groupEditTextLayout.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.END)
            constraintSet.connect(progressBar.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            constraintSet.connect(progressBar.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
            val transition = ChangeBounds()
            transition.interpolator = AnticipateOvershootInterpolator(0.5f)
            transition.duration = 500
            TransitionManager.beginDelayedTransition(parentLayout, transition)
            constraintSet.applyTo(parentLayout)
        }
    }

    private fun hideProgressBar() {
        binding.apply {
            constraintSet.load(context, R.layout.dialog_create_group)
            val transition = ChangeBounds()
            transition.interpolator = AnticipateOvershootInterpolator(0.5f)
            transition.duration = 500
            TransitionManager.beginDelayedTransition(parentLayout, transition)
            constraintSet.applyTo(parentLayout)
        }
    }

    fun setDialogListener(listener: CreateGroupDialogListener) {
        this.listener = listener
    }

    interface CreateGroupDialogListener {
        fun onCreate()
    }
}