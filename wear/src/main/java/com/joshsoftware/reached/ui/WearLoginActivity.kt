package com.joshsoftware.reached.ui

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.joshsoftware.core.AppSharedPreferences
import com.joshsoftware.core.ui.BaseLoginActivity
import com.joshsoftware.reached.databinding.ActivityLoginBinding
import timber.log.Timber
import javax.inject.Inject

class WearLoginActivity : BaseLoginActivity() {
    lateinit var binding: ActivityLoginBinding

    @Inject
    lateinit var sharedPreferences: AppSharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btnGoogleSignIn.setOnClickListener {
            signIn()
        }

        registerViewModelObservers()
    }

    override fun attemptSignIn(account: GoogleSignInAccount) {
        viewModel.signInWithGoogle(account)
    }

    private fun registerViewModelObservers() {
        viewModel.result.observe(this, Observer { (id, user) ->
            sharedPreferences.saveUserId(id)
            sharedPreferences.saveUserData(user)
            viewModel.fetchGroup(id)
        })

        viewModel.groupId.observe(this, Observer { id ->
            if(id != null) {
                startGroupMembersActivity(id)
            } else {
                startGroupWaitActivity()
            }
            finish()
        })

        viewModel.error.observe(this, Observer { error ->
            Timber.d(error)
        })

        viewModel.spinner.observe(this, Observer { loading ->
            if(loading) {
                showProgressView(binding.parent)
            } else {
                hideProgressView()
            }
        })
    }

    private fun startGroupWaitActivity() {
        val intent = Intent(this, GroupWaitActivity::class.java)
        startActivity(intent)
    }

    private fun startGroupMembersActivity(id: String) {
        val intent = Intent(this, GroupMemberActivity::class.java)
        intent.putExtra(INTENT_GROUP_ID, id)
        startActivity(intent)
    }
}