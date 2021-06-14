package com.joshsoftware.reached.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.joshsoftware.core.model.Address
import com.joshsoftware.core.model.IntentConstant
import com.joshsoftware.core.model.Member
import com.joshsoftware.core.model.RequestCodes
import com.joshsoftware.core.ui.BaseActivity
import com.joshsoftware.reached.R
import com.joshsoftware.reached.ui.adapter.AddressAdapter
import com.joshsoftware.reached.viewmodel.ProfileViewModel
import kotlinx.android.synthetic.main.activity_groups.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_profile.*
import java.lang.Math.abs
import javax.inject.Inject

class ProfileActivity : BaseActivity() {
    private var groupId: String? = null
    private var userId: String? = null

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var viewModel: ProfileViewModel
    lateinit var adapter: AddressAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        intent.extras?.getParcelable<Member>(IntentConstant.MEMBER.name)?.let {
            userId = it.id
            setUserData(it)
            setViewPager(it)
        }

        intent.extras?.getString(IntentConstant.GROUP_ID.name)?.let {
            groupId = it
        }

        txtLocateNow.setOnClickListener {
            startMapActivity(userId, groupId)
        }
        txtAddLocaiton.setOnClickListener {
            startSelectLocationActivity()
        }
    }

    private fun setViewPager(member: Member) {
        val addressList = member.address.map {
            it.value.id = it.key
            it.value
        }.toMutableList()
        addressList.add(Address(null, "Test 2", "Prime plus", "enter", 2112.0, 1212.0, 100))
        adapter = AddressAdapter()
        locationViewPager.adapter = adapter
        locationViewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        locationViewPager.offscreenPageLimit = 1
        val nextItemVisiblePx = resources.getDimension(R.dimen.viewpager_next_item_visible)
        val currentItemHorizontalMarginPx = resources.getDimension(R.dimen.viewpager_current_item_horizontal_margin)
        val pageTranslationX = nextItemVisiblePx + currentItemHorizontalMarginPx
        locationViewPager.setPageTransformer { page, position ->
            page.translationX = -pageTranslationX * position
            // Next line scales the item's height. You can remove it if you don't want this effect
//            page.scaleY = 1 - (0.25f * abs(position))
        }
        adapter.submitList(addressList)
    }

    private fun startSelectLocationActivity() {
        val intent = Intent(this, PickLocationActivity::class.java)
        startActivityForResult(intent, RequestCodes.PICK_LOCATION.code)
    }

    private fun startMapActivity(memberId: String?, groupId: String?) {
        val intent = Intent(this, MapActivity::class.java)
        intent.putExtra(INTENT_MEMBER_ID, memberId)
        intent.putExtra(INTENT_GROUP_ID, groupId)
        startActivity(intent)
    }

    override fun initializeViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory)[ProfileViewModel::class.java]
    }

    private fun setUserData(member: Member) {
        member.profileUrl?.let { profileUrl ->
            Glide.with(this).load(profileUrl).into(imgProfile)
        }
        txtName.text = member.name
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK) {

        }
    }


}