package com.joshsoftware.reached.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.joshsoftware.core.AppSharedPreferences
import com.joshsoftware.core.model.Group
import com.joshsoftware.core.model.IntentConstant
import com.joshsoftware.core.model.Member
import com.joshsoftware.core.ui.BaseActivity
import com.joshsoftware.core.util.ConversionUtil
import com.joshsoftware.core.viewmodel.GroupMemberViewModel
import com.joshsoftware.reached.R
import com.joshsoftware.reached.databinding.ActivityGroupMemberMobileBinding
import com.joshsoftware.reached.ui.LoginActivity
import com.joshsoftware.reached.ui.adapter.MemberAdapter
import kotlinx.android.synthetic.main.activity_group_member_mobile.*
import javax.inject.Inject


class GroupMemberActivity : BaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var preferences: AppSharedPreferences

    @Inject
    lateinit var sharedPreferences: AppSharedPreferences
    lateinit var viewModel: GroupMemberViewModel
    lateinit var adapter: MemberAdapter
    lateinit var binding: ActivityGroupMemberMobileBinding
    lateinit var groupId: String
    var group: Group? = null
    var sosSent: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupMemberMobileBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        binding.apply {
            intent.extras?.getParcelable<Group>(INTENT_GROUP)?.let {
                it.id?.let { gId ->
                    viewModel.fetchGroupDetails(gId)
                    groupId = gId
                }
            }
            handleLeaveRequest(intent)
            setSupportActionBar(bottomAppBar)

            recyclerView.layoutManager = LinearLayoutManager(this@GroupMemberActivity)
            adapter = MemberAdapter(
                sharedPreferences
            ) {
                startMapActivity(it)
            }

            recyclerView.adapter = adapter

            add.setOnClickListener {
                toggleFabMenu()
            }

            fabAdd.setOnClickListener {
                toggleFabMenu()
                onAddMemberClick()
            }

            fabSos.setOnClickListener {
                toggleFabMenu()
                sendSos()
            }

            fabLeave.setOnClickListener {
                toggleFabMenu()
                leaveOrDeleteGroup()
            }

            bottomAppBar.setNavigationOnClickListener {
                finish()
            }

            showOnMapLayout.setOnClickListener {
                startMapActivity(Member(""))
            }
        }

    }

    private fun getLeaveRequests() {
        group?.let { nonNullGroup ->
            sharedPreferences.userId?.let {
                if (it == nonNullGroup.created_by) {
                    nonNullGroup.id?.let { it1 -> viewModel.getLeaveRequests(it1) }
                }
            }
        }
    }
    private fun leaveOrDeleteGroup() {
        group?.let { nonNullGroup ->
            sharedPreferences.userId?.let {
                if(it == nonNullGroup.created_by) {
                    viewModel.deleteGroup(nonNullGroup, it)
                } else {
                    if(leaveOrDeleteGroupLabel.text != getString(R.string.request_sent)) {
                        nonNullGroup.created_by?.let {createdBy ->
                            sharedPreferences.userData?.name?.let {name ->
                                nonNullGroup.name?.let { groupName ->
                                    viewModel.requestLeaveGroup(groupId, it, createdBy, name, groupName)
                                }
                            }
                        }
                    } else {
                        showToastMessage(getString(R.string.leave_request_already_sent_message))
                    }

                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleLeaveRequest(intent)
    }

    private fun handleLeaveRequest(intent: Intent?) {
        val requestId = intent?.extras?.getString(IntentConstant.REQUEST_ID.name)
        val groupId = intent?.extras?.getString(IntentConstant.GROUP_ID.name)
        val memberId = intent?.extras?.getString(IntentConstant.MEMBER_ID.name)
        val message = intent?.extras?.getString(IntentConstant.MESSAGE.name)
        showLeaveRequestDialog(requestId, groupId, memberId, message)
    }

    private fun showLeaveRequestDialog(
        requestId: String?,
        groupId: String?,
        memberId: String?,
        message: String?
    ) {
        if(requestId != null && groupId != null && memberId != null && message != null) {
            showChoiceDialog(message,  {
                viewModel.leaveGroup(requestId, groupId, memberId)
            }, {
                viewModel.declineGroupLeaveRequest(requestId, memberId)
            })
        }
    }

    private fun sendSos() {
        if(binding.sosLabel.text == getString(R.string.send_sos)) {
            binding.sosLabel.text = getString(R.string.mark_safe)
        } else {
            binding.sosLabel.text = getString(R.string.send_sos)
        }
        sosSent = !sosSent
        viewModel.sendSos(groupId, userId = sharedPreferences.userId!!, user = sharedPreferences.userData!!, sosSent = sosSent)
    }

    private fun setSosLabel(sosSent: Boolean) {
        if(sosSent) {
            binding.sosLabel.text = getString(R.string.mark_safe)
        } else {
            binding.sosLabel.text = getString(R.string.send_sos)
        }
    }

    private fun toggleFabMenu() {
        binding.apply {
            if(dialogLayout.visibility == View.VISIBLE) {
                rotateFabWithAnimation(add, 0f)
                fabMenuLayout.visibility = View.GONE
                dialogLayout.visibility = View.GONE
            } else {
                rotateFabWithAnimation(add, 135f)
                fabMenuLayout.visibility = View.VISIBLE
                dialogLayout.visibility = View.VISIBLE
                dialogLayout.alpha = 0.1f
            }
        }
    }
    private fun rotateFabWithAnimation(fab: FloatingActionButton, degree: Float) {
        val interpolator = OvershootInterpolator()
        ViewCompat.animate(fab).rotation(degree).withLayer().setDuration(300).setInterpolator(interpolator).start()
    }

    private fun onAddMemberClick() {
        intent.extras?.getParcelable<Group>(INTENT_GROUP)?.let {
            startQrCodeActivity(it)
        }
    }
    private fun startMapActivity(member: Member) {
        val intent = Intent(this, MapActivity::class.java)
        intent.putExtra(INTENT_MEMBER_ID, member.id)
        intent.putExtra(INTENT_GROUP_ID, groupId)
        startActivity(intent)
    }

    override fun initializeViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory)[GroupMemberViewModel::class.java]


        viewModel.result.observe(this, Observer { group ->
            group?.let {
                it.id = groupId
                this.group = it
                supportActionBar?.title = it.name
                val util = ConversionUtil()
                sharedPreferences.userId?.let { userId ->
                    viewModel.leaveRequestExists(userId, groupId)
                    getLeaveRequests()
                    group.members[userId]?.sosState?.let { sosSent ->
                        this.sosSent = sosSent
                        setSosLabel(sosSent)
                    }
                    setLeaveGroupLabel(it.created_by, userId)
                }
                val members = util.getMemberListFromMap(group.members)
                adapter.submitList(members)
            }
        })

        viewModel.sos.observe(this, Observer { sosSent ->
            sosSent?.let {
                if(it) {
                    showToastMessage(getString(R.string.sos_sent_successfully))
                } else {
                    showToastMessage(getString(R.string.sos_was_stopped))
                }
            }
        })


        viewModel.deleteGroup.observe(this, Observer { groupDeleted ->
            groupDeleted?.let {
                showToastMessage("You have deleted the group successfully!")
                setResult(Activity.RESULT_OK)
                finish()
            }
        })


        viewModel.leaveGroupRequest.observe(this, Observer { groupLeft ->
            groupLeft?.let {
                leaveOrDeleteGroupLabel.text = getString(R.string.request_sent)
                showToastMessage("Your request to leave the group has been sent successfully!")
            }
        })


        viewModel.leaveGroup.observe(this, Observer { groupLeft ->
            groupLeft?.let {
                showToastMessage("Removed successfully!")
            }
        })

        viewModel.requestExists.observe(this, Observer { exists ->
            if(exists) {
                leaveOrDeleteGroupLabel.text = getString(R.string.request_sent)
            }
        })


        viewModel.leaveRequests.observe(this, Observer { requests ->
                requests?.forEach { leaveRequest ->
                showLeaveRequestDialog(
                    leaveRequest.requestId,
                    leaveRequest.group?.id,
                    leaveRequest.from?.id,
                    "${leaveRequest.from?.name} wants to leave the group. Remove from group?"
                )
            }
        })


        viewModel.spinner.observe(this, Observer { loading ->
            if(loading != null) {
                if (loading) {
                    showProgressView()
                } else {
                    hideProgressView()
                }
            }
        })
    }

    private fun setLeaveGroupLabel(createdBy: String?, userId: String){
        if(createdBy == userId) {
            leaveOrDeleteGroupLabel.text = getString(R.string.delete_group)
        } else {
            leaveOrDeleteGroupLabel.text = getString(R.string.leave_group)
        }
    }

    private fun startQrCodeActivity(group: Group) {
        val intent = Intent(this, QrCodeActivity::class.java)
        intent.putExtra(INTENT_GROUP, group)
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.logout) {
            sharedPreferences.deleteUserData()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.group_members_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

}