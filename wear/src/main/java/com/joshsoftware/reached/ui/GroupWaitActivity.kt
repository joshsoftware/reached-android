package com.joshsoftware.reached.ui

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.joshsoftware.core.AppSharedPreferences
import com.joshsoftware.core.ui.BaseActivity
import com.joshsoftware.reached.R
import com.joshsoftware.reached.viewmodel.GroupWaitViewModel
import javax.inject.Inject

class GroupWaitActivity : BaseActivity() {

    @Inject
    lateinit var sharedPreferences: AppSharedPreferences

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: GroupWaitViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_wait)
        sharedPreferences.userId?.let {
            viewModel.checkIfGroupJoinedOrCreated(it)
        }
    }

    override fun initializeViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory)[GroupWaitViewModel::class.java]

        viewModel.result.observe(this, Observer { id ->
            if(id != null) {
                startGroupMembersActivity(id)
            }
        })
    }

    private fun startGroupMembersActivity(id: String) {
        val intent = Intent(this, GroupMemberActivity::class.java)
        intent.putExtra(INTENT_GROUP_ID, id)
        startActivity(intent)
    }

}