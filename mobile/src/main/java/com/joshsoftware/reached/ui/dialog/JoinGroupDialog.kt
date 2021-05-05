package com.joshsoftware.reached.ui.dialog

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.animation.AnticipateOvershootInterpolator
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.ViewModelProvider
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.google.android.gms.location.LocationServices
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.joshsoftware.core.AppSharedPreferences
import com.joshsoftware.core.model.Group
import com.joshsoftware.core.ui.BaseDialogFragment
import com.joshsoftware.reached.R
import com.joshsoftware.reached.databinding.DialogJoinGroupBinding
import com.joshsoftware.reached.ui.activity.GroupChoiceViewModel
import com.joshsoftware.reached.ui.activity.GroupMemberActivity
import com.joshsoftware.reached.ui.activity.INTENT_GROUP
import kotlinx.android.synthetic.main.member_view.view.*
import java.util.*
import javax.inject.Inject

class JoinGroupDialog: BaseDialogFragment() {

    private lateinit var binding: DialogJoinGroupBinding

    private var constraintSet  = ConstraintSet()

    @Inject
    lateinit var sharedPreferences: AppSharedPreferences

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: GroupChoiceViewModel
    var group: Group? = null

    companion object
    {
        fun newInstance(group: Group): JoinGroupDialog{
            val args = Bundle()
            args.putParcelable(INTENT_GROUP, group)
            val fragment = JoinGroupDialog()
            fragment.arguments = args
            return fragment
        }

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        initializeViewModel()

        binding = DialogJoinGroupBinding.inflate(LayoutInflater.from(context))
        context?.let {

            binding.apply {

                arguments?.getParcelable<Group>(INTENT_GROUP)?.let { group ->
                    this@JoinGroupDialog.group = group
                    messageTextView.text = String.format(getString(R.string.you_have_been_invited_to_the_group_join_to_proceed), group.name)
                }

                buttonNegative.setOnClickListener {
                    dismiss()
                }

                buttonPositive.setOnClickListener {
                    group?.id?.let { gId ->
                        val client = LocationServices.getFusedLocationProviderClient(context)
                        client.lastLocation.addOnSuccessListener { location ->
                            var lat = 0.0
                            var long = 0.0
                            if(location != null) {
                                lat = location.latitude
                                long = location.longitude
                            }
                            viewModel.joinGroup(
                                gId,
                                sharedPreferences.userId!!,
                                sharedPreferences.userData!!,
                                lat,
                                long
                            ).observe(this@JoinGroupDialog, androidx.lifecycle.Observer {
                                startGroupMemberActivity()
                                dismiss()
                            })
                        }
                    }
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
        viewModel = ViewModelProvider(this, viewModelFactory)[GroupChoiceViewModel::class.java]

        viewModel.spinner.observe(this, { loading ->
            loading.let {
                if(it) showProgressBar(binding) else hideProgressBar()
            }
        })

        viewModel.error.observe(this, { errorMessage ->
            errorMessage?.let {
                showErrorMessage(it)
            }
        })
    }

    private fun startGroupMemberActivity() {
        if(activity != null) {
            val intent = Intent(activity, GroupMemberActivity::class.java)
            intent.putExtra(INTENT_GROUP, group)
            startActivity(intent)
        }
    }

    private fun showProgressBar(binding: DialogJoinGroupBinding) {
        binding.apply {
            dialogTitleTextView.text = getString(R.string.joining_group)
            constraintSet.clone(parentLayout)
            constraintSet.clear(messageTextView.id, ConstraintSet.END)
            constraintSet.connect(messageTextView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.END)
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
}