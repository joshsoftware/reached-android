package com.joshsoftware.reached.ui

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.joshsoftware.core.AppSharedPreferences
import com.joshsoftware.core.ui.BaseLoginActivity
import com.joshsoftware.reached.databinding.ActivityLoginBinding
import com.joshsoftware.reached.ui.activity.GroupChoiceActivity
import com.joshsoftware.reached.ui.activity.GroupMemberActivity
import com.joshsoftware.reached.ui.activity.INTENT_GROUP_ID
import javax.inject.Inject

class LoginActivity : BaseLoginActivity() {
    lateinit var binding: ActivityLoginBinding

    @Inject
    lateinit var sharedPreferences: AppSharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if(sharedPreferences.userData != null) {
            sharedPreferences.userId?.let {
                viewModel.fetchGroup(it)
            }
        }

        binding.btnGoogleSignIn.setOnClickListener {
            if(sharedPreferences.userData == null) {
                signIn()
            }
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
                startGroupChoiceActivity()
            }
            finish()
        })

        viewModel.error.observe(this, Observer {

        })

        viewModel.spinner.observe(this, Observer {loading ->
            if(loading) {
                showProgressView(binding.parent)
            } else {
                hideProgressView()
            }

        })
    }

    private fun startGroupMembersActivity(groupId: String) {
        val intent = Intent(this, GroupMemberActivity::class.java)
        intent.putExtra(INTENT_GROUP_ID, groupId)
        startActivity(intent)
    }

    private fun startGroupChoiceActivity() {
        val intent = Intent(this, GroupChoiceActivity::class.java)
        startActivity(intent)
    }
}