package com.joshsoftware.core.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Constraints
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.ViewModel
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomappbar.BottomAppBarTopEdgeTreatment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.shape.CutCornerTreatment
import com.google.android.material.shape.MaterialShapeDrawable
import com.joshsoftware.core.R
import com.joshsoftware.core.di.Injectable
import com.joshsoftware.core.viewmodel.BaseViewModel

abstract class BaseActivity: AppCompatActivity(), Injectable {
    var progressLayout: FrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeViewModel()
    }

    abstract fun initializeViewModel()

    protected fun showToastMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    protected fun showErrorMessage(message: String) {
        showToastMessage(getString(R.string.something_went_wrong) + ", " + getString(R.string.please_try_again))
    }
    /**
     * Shows error message alert dialog
     * @param message String
     */
    fun showChoiceDialog(
        message: String,
        onPositiveAction: () -> Unit,
        onNegativeAction: () -> Unit = {}
    ){
        var alertDialog: AlertDialog? = null
        val builder = AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                onPositiveAction()
                alertDialog?.dismiss()
            }.setNegativeButton(getString(R.string.no)) { _,_ ->
                onNegativeAction()
                alertDialog?.dismiss()
            }
        alertDialog = builder.create()
        alertDialog.show()
    }

    fun isNetWorkAvailable(onAvailable: () -> Unit) {
        val connMgr = applicationContext
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo

        if (networkInfo != null && networkInfo.isConnected) {
            onAvailable()
        } else {
            Toast.makeText(
                applicationContext,
                getString(R.string.network_error),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Shows the progress dialog in the given parent container view
     * @param rootView View
     */
    fun showProgressView(rootView: View) {
        if(progressLayout == null) {
            progressLayout = FrameLayout(this)
            val layoutParams = Constraints.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            progressLayout?.setBackgroundColor(Color.parseColor("#ffffff"))
            progressLayout?.alpha = 0.5f
            progressLayout?.elevation = resources.getDimension(R.dimen.progress_layout_elevation)
            progressLayout?.layoutParams = layoutParams

            val progressBar = ProgressBar(this)
            val progressParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER)
            progressBar.layoutParams = progressParams
            progressLayout?.addView(progressBar)
            if (rootView is ConstraintLayout) {
                rootView.addView(progressLayout)
            } else if (rootView is CoordinatorLayout) {
                rootView.addView(progressLayout)
            } else if (rootView is FrameLayout) {
                rootView.addView(progressLayout)
            }
        } else {
            progressLayout?.visibility = View.VISIBLE
        }
    }

    /**
     * Hides the progress dialog from the parent container view
     */
    open fun hideProgressView() {
        if(progressLayout !=  null) {
            progressLayout?.visibility = View.GONE
        }
    }
}