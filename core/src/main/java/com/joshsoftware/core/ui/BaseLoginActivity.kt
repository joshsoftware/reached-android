package com.joshsoftware.core.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.joshsoftware.core.viewmodel.LoginViewModel
import timber.log.Timber
import javax.inject.Inject

private const val RC_SIGN_IN = 1
open abstract class BaseLoginActivity: BaseActivity() {


    lateinit var viewModel: LoginViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        googleSignInClient.signOut()
    }


    override fun initializeViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]
    }

    protected fun signIn() {
//        FirebaseCrashlytics.getInstance().log(FirebaseConstants.LOGS.GOOGLE_SIGN_IN_CLICKED)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK) {
            if(requestCode == RC_SIGN_IN) {
                Timber.d("Google Sign in success")
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)
                account?.let {
                    isNetWorkAvailable {
                        attemptSignIn(it)
                    }
                }
            }
        }
    }

    abstract fun attemptSignIn(account: GoogleSignInAccount)
}