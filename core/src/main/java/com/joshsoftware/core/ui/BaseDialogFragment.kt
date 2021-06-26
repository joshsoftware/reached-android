package com.joshsoftware.core.ui

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.joshsoftware.core.R
import com.joshsoftware.core.di.Injectable

abstract class BaseDialogFragment: DialogFragment(), Injectable {
    abstract fun initializeViewModel()

    protected fun showErrorMessage(message: String) {
        showToastMessage(getString(R.string.something_went_wrong) + ", " + getString(R.string.please_try_again))
    }
    protected fun showToastMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    protected fun isNetWorkAvailable(context: Context, onAvailable: () -> Unit) {
        val connMgr = context
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo

        if (networkInfo != null && networkInfo.isConnected) {
            onAvailable()
        } else {
            Toast.makeText(
                context,
                getString(R.string.network_error),
                Toast.LENGTH_LONG
            ).show()
        }
    }


    protected fun showAlert(message: String, requestCode: Int) {
        context?.let {
            val dialog: AlertDialog.Builder = AlertDialog.Builder(it)
            dialog.setTitle("Permission Required")
            dialog.setCancelable(false)
            dialog.setMessage(message)
            dialog.setPositiveButton("Settings") { _, _ ->
                val i = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts(
                        "package",
                        activity?.packageName, null
                    )
                )
                startActivityForResult(i, requestCode)
            }
            val alertDialog: AlertDialog = dialog.create()
            alertDialog.show()
        }
    }
}