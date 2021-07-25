package com.joshsoftware.reached.ui.dialog

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.joshsoftware.reached.R
import com.joshsoftware.reached.databinding.LocationPermissionLayoutBinding
import com.joshsoftware.reached.ui.activity.PrivacyPolicyActivity

class LocationPremissionsDialog : DialogFragment() {
    private lateinit var binding: LocationPermissionLayoutBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = LocationPermissionLayoutBinding.inflate(LayoutInflater.from(context))
        context?.let {

            binding.apply {
                buttonGoToPermissionLocation.setOnClickListener {
                    val i = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts(
                            "package",
                            context!!.packageName, null
                        )
                    )
                    //i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    activity?.let { it.startActivityForResult(i, 21) }
                    dialog?.dismiss()
                }
                textViewPolicy.setOnClickListener {
                    val intent = Intent(context, PrivacyPolicyActivity::class.java)
                    startActivity(intent)
                }
                setTextclickablePolicy()
                setBoldText()
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

    private fun LocationPermissionLayoutBinding.setTextclickablePolicy() {
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                val intent = Intent(context, PrivacyPolicyActivity::class.java)
                startActivity(intent)
            }

            override fun updateDrawState(textPaint: TextPaint) {
                super.updateDrawState(textPaint)
                textPaint.color = Color.BLUE
                textPaint.isUnderlineText = false
            }
        }
        val message = getString(R.string.location_app_message)
        val startIndex = message.indexOf("Privacy Policy")
        val endIndex = startIndex + "Privacy Policy".length
        val spannableString = SpannableString(message)
        spannableString.setSpan(
            clickableSpan,
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        textViewPolicy.text = spannableString
    }

    private fun LocationPermissionLayoutBinding.setBoldText() {
        val message = getString(R.string.location_app_setting_message)
        val spannableString = SpannableString(message)
        val higlightText = "Allow all the time"
        val startIndex = message.indexOf(higlightText)
        val endIndex = startIndex + higlightText.length
        spannableString.setSpan(
            StyleSpan(Typeface.BOLD),
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        textViewAppSettings.text = spannableString
    }
}