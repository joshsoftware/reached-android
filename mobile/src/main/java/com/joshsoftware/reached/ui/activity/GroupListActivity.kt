package com.joshsoftware.reached.ui.activity

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.zxing.integration.android.IntentIntegrator
import com.joshsoftware.core.AppSharedPreferences
import com.joshsoftware.core.PermissionActivity
import com.joshsoftware.core.geofence.GeoConstants
import com.joshsoftware.core.geofence.GeofenceBroadcastReceiver
import com.joshsoftware.core.model.Address
import com.joshsoftware.core.model.Group
import com.joshsoftware.core.ui.BaseActivity
import com.joshsoftware.reached.ui.adapter.GroupsAdapter
import com.joshsoftware.reached.R
import com.joshsoftware.reached.ui.LoginActivity
import com.joshsoftware.core.viewmodel.GroupListViewModel
import com.joshsoftware.reached.ui.dialog.CreateGroupDialog
import com.joshsoftware.reached.ui.dialog.JoinGroupDialog
import com.joshsoftware.reached.utils.InviteLinkUtils
import com.journeyapps.barcodescanner.CaptureActivity
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.activity_group_list.*
import kotlinx.android.synthetic.main.activity_groups.*
import timber.log.Timber
import java.util.ArrayList
import javax.inject.Inject

const val REQUEST_CODE_MEMBERS = 1
class GroupListActivity : PermissionActivity(), HasSupportFragmentInjector {

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
        val linkUtils = InviteLinkUtils()
        linkUtils.handleDynamicLinks(intent) {
            showJoinGroupAlertDialog(it)
        }
        setSupportActionBar(findViewById(R.id.bottomAppBar))
        fetchGroups()

        add.setOnClickListener {
            toggleFabMenu()
        }

        fabCreate.setOnClickListener {
            val dialog = CreateGroupDialog()
            dialog.show(supportFragmentManager, dialog.tag)
            toggleFabMenu()
        }
        fabJoin.setOnClickListener {
            requestPermission(arrayOf(android.Manifest.permission.CAMERA), action = {
                if(it == PermissionActivity.Status.GRANTED) {
                    IntentIntegrator(this@GroupListActivity)
                            .setCaptureActivity(CaptureActivity::class.java)
                            .setOrientationLocked(false)
                            .setBeepEnabled(false)
                            .setPrompt("Scan QR code to join a group")
                            .initiateScan(); // `this` is the current Activity
                }
            })
            toggleFabMenu()
        }
    }

    private fun fetchGroups() {
        isNetWorkAvailable {
            sharedPreferences.userId?.let {
                viewModel.fetchGroups(it)
            }
        }
    }
    private fun showJoinGroupAlertDialog(group: Group) {
        val dialog = JoinGroupDialog.newInstance(group)
        dialog.show(supportFragmentManager, dialog.tag)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = GroupsAdapter {
            startGroupMembersActivity(it)
        }

        recyclerView.adapter = adapter
    }

    private fun startGroupMembersActivity(group: Group) {
        val intent = Intent(this, GroupMemberActivity::class.java)
        intent.putExtra(INTENT_GROUP, group)
        startActivityForResult(intent, REQUEST_CODE_MEMBERS)
    }

    private fun startGroupMembersActivity(groupId: String) {
        val intent = Intent(this, GroupMemberActivity::class.java)
        intent.putExtra(INTENT_GROUP, Group(id = groupId))
        startActivity(intent)
    }

    override fun initializeViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory)[GroupListViewModel::class.java]

        viewModel.result.observe(this, Observer { list ->
            if(list.isNullOrEmpty()) {
                showPlaceHolder(true)
            } else {
                showPlaceHolder(false)
                adapter.submitList(list)
            }
        })

        viewModel.spinner.observe(this, Observer{ loading ->
            loading?.let {
                if(it) {
                    showProgressView()
                } else {
                    hideProgressView()
                }
            }
        })

        viewModel.error.observe(this,  Observer{ error ->
            error?.let {
                showErrorMessage(it)
            }
        })
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
    private fun toggleFabMenu() {
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
    private fun rotateFabWithAnimation(fab: FloatingActionButton, degree: Float) {
        val interpolator = OvershootInterpolator()
        ViewCompat.animate(fab).rotation(degree).withLayer().setDuration(300).setInterpolator(interpolator).start()
    }

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return dispatchingAndroidInjector
    }

    // Get the results:
    override fun onActivityResult(
            requestCode: Int,
            resultCode: Int,
            data: Intent?
    ) {
        if(requestCode == REQUEST_CODE_MEMBERS) {
            fetchGroups()
        } else {
            val result =
                    IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            if (result != null) {
                if (result.contents == null) {
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
                } else {
                    val groupId = result.contents

                    val id = sharedPreferences.userId
                    val user = sharedPreferences.userData
                    if (user != null) {
                        if (id != null) {

                            val client = LocationServices.getFusedLocationProviderClient(applicationContext)
                            client.lastLocation.addOnSuccessListener { location ->
                                var lat = 0.0
                                var long = 0.0
                                if(location != null) {
                                    lat = location.latitude
                                    long = location.longitude
                                }
                                viewModel.joinGroup(groupId, id, user, lat, long)
                                        .observe(this, androidx.lifecycle.Observer {
                                            startGroupMembersActivity(result.contents)
                                        })

                            }
                        }
                    }
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }

    }

    private fun showPlaceHolder(show: Boolean) {
        if(show) {
            placeHolderLayout.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            placeHolderLayout.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }
    override fun onResume() {
        super.onResume()
        fetchGroups()
    }
}