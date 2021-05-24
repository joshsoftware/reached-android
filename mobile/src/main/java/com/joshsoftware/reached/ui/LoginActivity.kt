package com.joshsoftware.reached.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.view.View
import android.view.animation.AnticipateOvershootInterpolator
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.Constraints
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.joshsoftware.core.AppSharedPreferences
import com.joshsoftware.core.di.AppType
import com.joshsoftware.core.ui.BaseLoginActivity
import com.joshsoftware.reached.R
import com.joshsoftware.reached.databinding.ActivityLoginMobileBinding
import com.joshsoftware.reached.model.OnboardingData
import com.joshsoftware.reached.ui.activity.GroupChoiceActivity
import com.joshsoftware.reached.ui.activity.GroupListActivity
import com.joshsoftware.reached.ui.adapter.OnboardingAdapter
import timber.log.Timber
import javax.inject.Inject

class LoginActivity : BaseLoginActivity(), BaseLoginActivity.BaseActivityListener {
    lateinit var binding: ActivityLoginMobileBinding

    private var bottomSheetBehavior: BottomSheetBehavior<View>? = null
    private var adapter: OnboardingAdapter? = null
    @Inject
    lateinit var sharedPreferences: AppSharedPreferences

    val handler = Handler()
    var pageCounter : Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginMobileBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setListener(this)

//        if(!sharedPreferences.onboardingShown) {
            Handler().postDelayed({
                showOnboardingLayout()
            }, 200)
            sharedPreferences.setOnboardingShown(true)
//        }
        binding.apply {
            imgSkip.setOnClickListener {
                showLoginComponents(true)
                hideOnboarding()
            }

            btnGoogleSignIn.setOnClickListener {
                if(sharedPreferences.userData == null) {
                    signIn()
                }

            }
        }

        binding.apply {
            viewPagerOnboarding.orientation = ViewPager2.ORIENTATION_HORIZONTAL
            adapter = OnboardingAdapter()
            viewPagerOnboarding.adapter = adapter
            adapter?.submitList(listOf(
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
            ))
            dotsIndicator.setViewPager2(viewPagerOnboarding)
            pageCounter = object : Runnable {
                override fun run() {
                        if (viewPagerOnboarding.currentItem < 4) {
                            viewPagerOnboarding.currentItem++
                            if(viewPagerOnboarding.currentItem != 3) {
                                handler.postDelayed(this, 2 * 1000)
                            }
                        }
                }
            }
            handler.postDelayed(pageCounter!!, 2 * 1000)
            viewPagerOnboarding.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    if(position == 3) {
                        btnContinue.text = getString(R.string.button_text_continue)
                        val padding = resources.getDimensionPixelOffset(R.dimen.dimen_button_padding)
                        btnContinue.setPadding(padding, 0, padding, 0)
                        btnContinue.icon = null
                    } else {
                        btnContinue.text = ""
                        btnContinue.setPadding(0, 0, 0, 0)
                        btnContinue.icon = getDrawable(R.drawable.ic_onboarding_arrow)
                    }
                }
            })
            btnContinue.setOnClickListener {
                if(viewPagerOnboarding.currentItem == 3) {
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
                showProgressView(binding.parent)
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
        }
    }
    private fun startGroupListActivity() {
        val intent = Intent(this, GroupListActivity::class.java)
        startActivity(intent)
    }

    private fun startGroupChoiceActivity() {
        val intent = Intent(this, GroupChoiceActivity::class.java)
        startActivity(intent)
    }

    override fun onPermissionGrant() {
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