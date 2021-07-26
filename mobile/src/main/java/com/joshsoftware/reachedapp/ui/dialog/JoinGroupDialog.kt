package com.joshsoftware.reachedapp.ui.dialog

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.animation.AnticipateOvershootInterpolator
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.google.android.gms.location.LocationServices
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.joshsoftware.core.AppSharedPreferences
import com.joshsoftware.core.model.Group
import com.joshsoftware.core.ui.BaseDialogFragment
import com.joshsoftware.reachedapp.R
import com.joshsoftware.reachedapp.databinding.DialogJoinGroupBinding
import com.joshsoftware.reachedapp.ui.activity.GroupChoiceViewModel
import com.joshsoftware.reachedapp.ui.activity.HomeActivity
import com.joshsoftware.reachedapp.ui.activity.INTENT_GROUP
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

    override fun onSaveInstanceState(outState: Bundle) {

    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        initializeViewModel()

        binding = DialogJoinGroupBinding.inflate(LayoutInflater.from(context))
        context?.let { ctx ->

            binding.apply {

                arguments?.getParcelable<Group>(INTENT_GROUP)?.let { group ->
                    this@JoinGroupDialog.group = group
                    messageTextView.text = String.format(getString(R.string.you_have_been_invited_to_the_group_join_to_proceed), group.name)
                }

                buttonNegative.setOnClickListener {
                    dismiss()
                }

                buttonPositive.setOnClickListener {
                    isNetWorkAvailable(ctx) {
                        if (isLocationPermissionNotGranted()) {
                            requestPermissions(
                                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION),
                                10
                            )
                        } else {
                            updateLocationForGroup(ctx)
                        }
                    }
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

    fun updateLocationForGroup(ctx: Context) {
        group?.id?.let { gId ->
            updateLastLocation(ctx, gId)
        }
    }

    @SuppressLint("MissingPermission")
    fun updateLastLocation(context: Context, gId: String) {
        val client = LocationServices.getFusedLocationProviderClient(context)
        client.lastLocation.addOnCompleteListener { task ->
            var lat = 0.0
            var long = 0.0
            if(task.isSuccessful) {
                if(task.result != null) {
                    lat = task.result.latitude
                    long = task.result.longitude
                }
            }

            viewModel.joinGroup(
                gId,
                sharedPreferences.userId!!,
                sharedPreferences.userData!!,
                lat,
                long
            ).observe(this@JoinGroupDialog, {
                startGroupMemberActivity()
                dismiss()
            })
        }
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
            val intent = Intent(activity, HomeActivity::class.java)
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


    private fun isLocationPermissionNotGranted(): Boolean {
        context?.let { ctx ->
            return ActivityCompat.checkSelfPermission(
                ctx,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                ctx,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        } ?: run {
            return false
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 10) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    context?.let(::updateLocationForGroup)
            } else {
                showAlert("Please grant location permission to join the group", 10)
            }
        }
    }
}