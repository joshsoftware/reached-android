package com.joshsoftware.reached.ui

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.joshsoftware.core.AppSharedPreferences
import com.joshsoftware.core.model.Group
import com.joshsoftware.core.ui.BaseActivity
import com.joshsoftware.core.viewmodel.GroupListViewModel
import com.joshsoftware.reached.databinding.ActivityGroupMemberBinding
import com.joshsoftware.reached.ui.adapter.GroupsAdapter
import javax.inject.Inject

class WearGroupListActivity : BaseActivity() {

    lateinit var adapter: GroupsAdapter
    lateinit var viewModel: GroupListViewModel
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var sharedPreferences: AppSharedPreferences
    lateinit var binding: ActivityGroupMemberBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupMemberBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setupRecyclerView()
        sharedPreferences.userId?.let {
            isNetWorkAvailable {
                viewModel.fetchGroups(it)
            }
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = GroupsAdapter {
            startGroupMembersActivity(it)
        }

        binding.recyclerView.adapter = adapter
    }

    private fun startGroupMembersActivity(group: Group) {
        val intent = Intent(this, GroupMemberActivity::class.java)
        intent.putExtra(INTENT_GROUP_ID, group.id)
        startActivity(intent)
    }

    override fun initializeViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory)[GroupListViewModel::class.java]

        viewModel.result.observe(this, Observer { list ->
            list?.let {
                adapter.submitList(it)
            }
        })

        viewModel.spinner.observe(this, Observer { loading ->
//            loading?.let {
//                if(it) {
//                    showProgressView(binding.parentLayout)
//                } else {
//                    hideProgressView()
//                }
//            }
        })

        viewModel.error.observe(this,  Observer { error ->
            error?.let {
                showErrorMessage(it)
            }
        })
    }

}