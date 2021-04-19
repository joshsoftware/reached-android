package com.joshsoftware.reached.ui.activity

import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.joshsoftware.core.AppSharedPreferences
import com.joshsoftware.core.model.Group
import com.joshsoftware.core.model.Member
import com.joshsoftware.core.model.SosUser
import com.joshsoftware.core.ui.BaseLocationActivity
import com.joshsoftware.core.ui.adapter.MemberAdapter
import com.joshsoftware.core.util.ConversionUtil
import com.joshsoftware.core.viewmodel.GroupMemberViewModel
import com.joshsoftware.reached.R
import com.joshsoftware.reached.databinding.ActivityGroupMemberMobileBinding
import com.joshsoftware.reached.ui.LoginActivity
import kotlinx.android.synthetic.main.activity_group_member_mobile.*
import timber.log.Timber
import javax.inject.Inject


class GroupMemberActivity : BaseLocationActivity(), BaseLocationActivity.LocationChangeListener {

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
        setLocationChangeListener(this)
        binding.apply {
            intent.extras?.getParcelable<Group>(INTENT_GROUP)?.let {
                it.id?.let { gId ->
                    viewModel.fetchGroupDetails(gId)
                    groupId = gId
                    viewModel.observeSos(gId)
                }
            }
            setSupportActionBar(bottomAppBar)

            recyclerView.layoutManager = LinearLayoutManager(this@GroupMemberActivity)
            adapter = MemberAdapter(sharedPreferences) {
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
        }

    }

    private fun sendSos() {
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


        viewModel.result.observe(this, { group ->
            group?.let {
                supportActionBar?.title = it.name
                val util = ConversionUtil()
                val members = util.getMemberListFromMap(group.members)
                adapter.submitList(members)
            }
        })


        viewModel.sos.observe(this, Observer { sos ->
            if (sos != null) {
                sharedPreferences.userId?.let {
                    if(it != sos.id) {
                        showSosDialog(sos)
                    }
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

    override fun onLocationChange(location: Location) {
        intent.extras?.getString(INTENT_GROUP_ID)?.let { gId ->
            preferences.userId?.let {
                viewModel.updateLocationForMember(gId, it, location)
            }
        }
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

    fun showSosDialog(
        sos: SosUser?,
    ) {
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