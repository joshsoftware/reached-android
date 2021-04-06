package com.joshsoftware.core.ui

import android.widget.Toast
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
}