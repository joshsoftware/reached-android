package com.joshsoftware.reached.ui

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.joshsoftware.core.AppSharedPreferences
import com.joshsoftware.core.model.Member
import com.joshsoftware.core.ui.BaseActivity
import com.joshsoftware.core.util.ConversionUtil
import com.joshsoftware.core.viewmodel.GroupMemberViewModel
import com.joshsoftware.reached.databinding.ActivityGroupMemberBinding
import javax.inject.Inject

const val INTENT_GROUP_ID = "INTENT_GROUP_ID"
const val INTENT_MEMBER_ID = "INTENT_MEMBER_ID"
class GroupMemberActivity : BaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var preferences: AppSharedPreferences

    lateinit var viewModel: GroupMemberViewModel
    lateinit var adapter: MemberAdapter
    lateinit var binding: ActivityGroupMemberBinding
    lateinit var groupId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupMemberBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        binding.apply {
            intent.extras?.getString(INTENT_GROUP_ID)?.let {
                isNetWorkAvailable {
                    viewModel.fetchGroupDetails(it)
                }
                groupId = it
            }
            recyclerView.layoutManager = LinearLayoutManager(this@GroupMemberActivity)
            adapter = MemberAdapter(sharedPreferences = preferences) {
                startMapActivity(it)
            }
            recyclerView.adapter = adapter
        }
    }

    override fun initializeViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory)[GroupMemberViewModel::class.java]
        viewModel.result.observe(this, Observer { group ->
            group?.let {
                val util = ConversionUtil()
                val members = util.getMemberListFromMap(group.members)
                members.add(Member(name = "All members"))
                adapter.submitList(members)
            }
        })
    }

    private fun startMapActivity(member: Member) {
        val intent = Intent(this, MapActivity::class.java)
        if(member.id == null) {
            intent.putExtra(INTENT_MEMBER_ID, "")
        } else {
            intent.putExtra(INTENT_MEMBER_ID, member.id)
        }
        intent.putExtra(INTENT_GROUP_ID, groupId)
        startActivity(intent)
    }
}