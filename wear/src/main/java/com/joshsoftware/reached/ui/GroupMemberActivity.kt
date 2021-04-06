package com.joshsoftware.reached.ui

import android.content.Intent
import android.location.Location
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.wear.widget.WearableLinearLayoutManager
import com.joshsoftware.core.AppSharedPreferences
import com.joshsoftware.core.model.Member
import com.joshsoftware.core.model.SosUser
import com.joshsoftware.core.ui.BaseLocationActivity
import com.joshsoftware.core.util.ConversionUtil
import com.joshsoftware.core.viewmodel.GroupMemberViewModel
import com.joshsoftware.reached.R
import com.joshsoftware.reached.databinding.ActivityGroupMemberBinding
import javax.inject.Inject

const val INTENT_GROUP_ID = "INTENT_GROUP_ID"
const val INTENT_MEMBER_ID = "INTENT_MEMBER_ID"
class GroupMemberActivity : BaseLocationActivity(), BaseLocationActivity.LocationChangeListener {

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
                fetchLocation()
                viewModel.fetchGroupDetails(it)
                groupId = it
                viewModel.observeSos(groupId)
            }

            recyclerView.layoutManager = WearableLinearLayoutManager(this@GroupMemberActivity)
            adapter = MemberAdapter {
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
        viewModel.sos.observe(this, Observer {sos ->
            if (sos != null) {
                preferences.userId?.let {
                    if(it != sos.id) {
                        showSosDialog(sos)
                    }
                }
            }
        })
    }

    override fun onLocationChange(location: Location) {
        intent.extras?.getString(INTENT_GROUP_ID)?.let { gId ->
            preferences.userId?.let {
                viewModel.updateLocationForMember(gId, it, location)
            }
        }
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

    fun showSosDialog(
        sos: SosUser?,
    ){
        sos?.let {
            var alertDialog: AlertDialog? = null
            val builder = AlertDialog.Builder(this)
                    .setTitle(getString(R.string.sos_alert))
                    .setMessage("Emergency! this is from ${it.name}. I need help. Press ok to track.")
                    .setNeutralButton("OK"
                    ) { p0, p1 ->
                        startMapActivity(Member(it.id))
                        viewModel.deleteSos(groupId)
                    }

            alertDialog = builder.create()
            alertDialog.show()
        }

    }
}