package com.joshsoftware.reached.ui.activity

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.joshsoftware.core.model.Group
import com.joshsoftware.core.model.IntentConstant
import com.joshsoftware.core.ui.BaseActivity
import com.joshsoftware.reached.R
import com.joshsoftware.reached.ui.adapter.MemberEditAdapter
import com.joshsoftware.reached.viewmodel.GroupEditViewModel
import kotlinx.android.synthetic.main.activity_group_edit.*
import javax.inject.Inject

class GroupEditActivity : BaseActivity() {

    lateinit var viewModel: GroupEditViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var adapter: MemberEditAdapter
    lateinit var group: Group

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_edit)
        intent.extras?.getParcelable<Group>(IntentConstant.GROUP.name)?.let {
            group = it
            txtGroupName.text = group.name
        }
        adapter = MemberEditAdapter({ member, _ ->
                                        viewModel.deleteMember(member, group)
                                    }, group.created_by!!
        )
        memberRecyclerView.layoutManager = LinearLayoutManager(this)
        memberRecyclerView.adapter = adapter

        adapter.submitList(group.members.map { it.value }.toMutableList())

        imgBack.setOnClickListener {
            onBackPressed()
        }
    }

    override fun initializeViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory)[GroupEditViewModel::class.java]
        viewModel.result.observe(this, { member ->
            val index = adapter.currentList.indexOfFirst { it.id == member.id }
            val list = adapter.currentList.toMutableList()
            list.removeAt(index)
            adapter.submitList(list)
        })
    }
}