package com.joshsoftware.reached.ui.activity

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.zxing.integration.android.IntentIntegrator
import com.joshsoftware.core.AppSharedPreferences
import com.joshsoftware.core.ui.BaseLocationActivity
import com.joshsoftware.reached.databinding.ActivityGroupChoiceBinding
import com.journeyapps.barcodescanner.CaptureActivity
import java.util.*
import javax.inject.Inject


class GroupChoiceActivity : BaseLocationActivity(), BaseLocationActivity.LocationChangeListener {

    lateinit var binding: ActivityGroupChoiceBinding

    @Inject
    lateinit var sharedPreferences: AppSharedPreferences

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var viewModel: GroupChoiceViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupChoiceBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.apply {
            createButton.setOnClickListener {
                val groupId = UUID.randomUUID().toString()
                val id = sharedPreferences.userId
                val user = sharedPreferences.userData
                if (user != null) {
                    if (id != null) {
                        viewModel.createGroup(groupId, id, user)
                    }
                }
            }

            joinButton.setOnClickListener {
                requestPermission(arrayOf(android.Manifest.permission.CAMERA), action = {
                    if(it == Status.GRANTED) {
                        IntentIntegrator(this@GroupChoiceActivity)
                                .setCaptureActivity(CaptureActivity::class.java)
                                .setOrientationLocked(true)
                                .initiateScan(); // `this` is the current Activity
                    }
                })
            }

        }
    }

    override fun initializeViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory)[GroupChoiceViewModel::class.java]

        viewModel.result.observe(this, androidx.lifecycle.Observer { id ->
            id?.let {
                startQrCodeActivity(it)
            }
        })

        viewModel.spinner.observe(this, androidx.lifecycle.Observer { loading ->
            if(loading) {
                showProgressView(binding.parent)
            } else {
                hideProgressView()
            }
        })
    }

    private fun startQrCodeActivity(id: String) {
        val intent = Intent(this, QrCodeActivity::class.java)
        intent.putExtra(INTENT_GROUP_ID, id)
        startActivity(intent)
    }

    // Get the results:
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
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
                        viewModel.joinGroup(groupId, id, user).observe(this, androidx.lifecycle.Observer {
                            startGroupMembersActivity(result.contents)
                        })
                    }
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onLocationChange(location: Location) {

    }


    private fun startGroupMembersActivity(groupId: String) {
        val intent = Intent(this, GroupMemberActivity::class.java)
        intent.putExtra(INTENT_GROUP_ID, groupId)
        startActivity(intent)
    }

}