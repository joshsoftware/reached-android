package com.joshsoftware.reached.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.joshsoftware.core.AppSharedPreferences
import com.joshsoftware.core.ui.BaseActivity
import com.joshsoftware.reached.R
import com.joshsoftware.reached.ui.adapter.GroupsAdapter
import com.joshsoftware.reached.viewmodel.GroupListViewModel
import kotlinx.android.synthetic.main.activity_group_list.*
import javax.inject.Inject

class GroupListActivity : BaseActivity() {

    lateinit var adapter: GroupsAdapter
    lateinit var viewModel: GroupListViewModel
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var sharedPreferences: AppSharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_list)
        setupRecyclerView()
        sharedPreferences.userData?.let {
            viewModel.fetchGroups(it)
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = GroupsAdapter {

        }

        recyclerView.adapter = adapter
    }

    override fun initializeViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory)[GroupListViewModel::class.java]

        viewModel.result.observe(this, { list ->
            list?.let {
                adapter.submitList(it)
            }
        })

        viewModel.spinner.observe(this, { loading ->
            loading?.let {
                if(it) {
                    showProgressView(parentLayout)
                } else {
                    hideProgressView()
                }
            }
        })

        viewModel.error.observe(this,  { error ->
            error?.let {
                showErrorMessage(it)
            }
        })
    }
}