package com.joshsoftware.reached.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.view.View
import android.view.animation.AnticipateOvershootInterpolator
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.joshsoftware.core.AppSharedPreferences
import com.joshsoftware.core.BaseLocationPermissionActivity
import com.joshsoftware.core.di.AppType
import com.joshsoftware.core.model.Group
import com.joshsoftware.core.ui.BaseLoginActivity
import com.joshsoftware.reached.R
import com.joshsoftware.reached.databinding.ActivityLoginMobileBinding
import com.joshsoftware.reached.model.OnboardingData
import com.joshsoftware.reached.ui.activity.GroupChoiceActivity
import com.joshsoftware.reached.ui.activity.HomeActivity
import com.joshsoftware.reached.ui.adapter.OnboardingAdapter
import timber.log.Timber
import javax.inject.Inject

class LoginActivity : BaseLoginActivity(), BaseLocationPermissionActivity.PermissionListener {
    lateinit var binding: ActivityLoginMobileBinding
    private var adapter: OnboardingAdapter? = null
    @Inject
    lateinit var sharedPreferences: AppSharedPreferences

    val handler = Handler()
    var pageCounter : Runnable? = null
    var inviteLinkGroup: Group? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginMobileBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        listener = this
        if(!sharedPreferences.onboardingShown) {
            Handler().postDelayed({
                showOnboardingLayout()
            }, 200)
            sharedPreferences.setOnboardingShown(true)
        } else {
            hideOnboarding()
        }
        binding.apply {
            imgSkip.setOnClickListener {
                hideOnboarding()
            }
            btnGoogleSignIn.setOnClickListener {
                isNetWorkAvailable {
                    if(sharedPreferences.userData == null) {
                        signIn()
                    }
                }
            }
        }

        binding.apply {
            viewPagerOnboarding.orientation = ViewPager2.ORIENTATION_HORIZONTAL
            adapter = OnboardingAdapter()
            viewPagerOnboarding.adapter = adapter
            val list = listOf(
                OnboardingData(
                    R.drawable.onboarding_one_background, R.drawable.onboarding_one, getString(
                        R.string.onboarding_text_one)),
                OnboardingData(
                    R.drawable.onboarding_two_background, R.drawable.onboarding_two, getString(
                        R.string.onboarding_text_two)),
                OnboardingData(
                    R.drawable.onboarding_three_background, R.drawable.onboarding_three, getString(
                        R.string.onboarding_text_three)),
                OnboardingData(
                    R.drawable.onboarding_four_background, R.drawable.onboarding_four, getString(
                        R.string.onboarding_text_four))
            )
            adapter?.submitList(list)
            dotsIndicator.setViewPager2(viewPagerOnboarding)
            imgContinue.setOnClickListener {
                if(viewPagerOnboarding.currentItem == list.size - 1) {
                    hideOnboarding()
                } else {
                    viewPagerOnboarding.setCurrentItem(viewPagerOnboarding.currentItem + 1, true)
                }
            }
        }

        registerViewModelObservers()
    }

    private fun showOnboardingLayout() {
        binding.apply {
            val set = ConstraintSet()
            set.clone(parent)
            set.connect(onboardingLayout.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)

            val transition = ChangeBounds()
            transition.interpolator = AnticipateOvershootInterpolator(0.5f)
            transition.duration = 500
            TransitionManager.beginDelayedTransition(parent, transition)
            set.applyTo(parent)
        }

    }


    override fun attemptSignIn(account: GoogleSignInAccount) {
        viewModel.signInWithGoogle(account, AppType.MOBILE)
    }

    private fun registerViewModelObservers() {

        viewModel.result.observe(this, Observer { (id, user) ->
            sharedPreferences.saveUserId(id)
            sharedPreferences.saveUserData(user)
            viewModel.fetchUserDetails(id)
        })

        viewModel.user.observe(this, Observer { user ->
            user?.let {
                sharedPreferences.saveUserData(user)
                if (user.groups.isEmpty()) {
                    startGroupChoiceActivity()
                } else {
                    startGroupListActivity()
                }
                finish()
            }
        })

        viewModel.error.observe(this, Observer { error ->
            Timber.d(error)
        })

        viewModel.spinner.observe(this, Observer {loading ->
            if(loading) {
                showProgressView()
            } else {
                hideProgressView()
            }

        })
    }

    private fun hideOnboarding() {
        binding.apply {
            val set = ConstraintSet()
            set.clone(parent)
            set.connect(onboardingLayout.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
            val transition = ChangeBounds()
            transition.interpolator = AnticipateOvershootInterpolator(0.5f)
            transition.duration = 200
            TransitionManager.beginDelayedTransition(parent, transition)
            set.applyTo(parent)
            checkForLocationPermission()
            showLoginComponents(true)
        }
    }
    private fun startGroupListActivity() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }

    private fun startGroupChoiceActivity() {
        val intent = Intent(this, GroupChoiceActivity::class.java)
        startActivity(intent)
    }

    override fun onPermissionGrant() {
        startLocationTrackingService()
        if(sharedPreferences.userData != null) {
            sharedPreferences.userId?.let {
                if (sharedPreferences.userData!!.groups.isEmpty()) {
                    startGroupChoiceActivity()
                } else {
                    startGroupListActivity()
                }
                finish()
            }
        }
    }

    private fun showLoginComponents(show: Boolean) {
        binding.apply {
            loginGroup.visibility = if(show) View.VISIBLE else View.GONE
            imgLogo.visibility = if(!show) View.VISIBLE else View.GONE
        }
    }
}