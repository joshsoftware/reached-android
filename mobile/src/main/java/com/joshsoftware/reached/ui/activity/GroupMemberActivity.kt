package com.joshsoftware.reached.ui.activity

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
import com.joshsoftware.core.model.Member
import com.joshsoftware.core.ui.BaseActivity
import com.joshsoftware.core.util.ConversionUtil
import com.joshsoftware.core.viewmodel.GroupMemberViewModel
import com.joshsoftware.reached.R
import com.joshsoftware.reached.databinding.ActivityGroupMemberMobileBinding
import com.joshsoftware.reached.ui.LoginActivity
import com.joshsoftware.reached.ui.adapter.MemberAdapter
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
    lateinit var createdBy: String

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

            bottomAppBar.setNavigationOnClickListener {
                finish()
            }

            showOnMapLayout.setOnClickListener {
                startMapActivity(Member(""))
            }
        }

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        println("new intent")
    }
    private fun sendSos() {
        if(binding.sosLabel.text == getString(R.string.send_sos)) {
            binding.sosLabel.text = getString(R.string.mark_safe)
        } else {
            binding.sosLabel.text = getString(R.string.send_sos)
        }
        viewModel.sendSos(groupId, userId = sharedPreferences.userId!!, user = sharedPreferences.userData!!)
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
                supportActionBar?.title = it.name
                val util = ConversionUtil()
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


        viewModel.spinner.observe(this, Observer { loading ->
            if(loading != null) {
                if (loading) {
                    showProgressView(binding.parent)
                } else {
                    hideProgressView()
                }
            }
        })
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