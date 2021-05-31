package com.joshsoftware.reached.di

import com.joshsoftware.reached.ui.LoginActivity
import com.joshsoftware.reached.ui.activity.*
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityModule {

    @ContributesAndroidInjector
    abstract fun contributeLoginActivity(): LoginActivity

    @ContributesAndroidInjector
    abstract fun contributeGroupMemberActivity(): GroupMemberActivity

    @ContributesAndroidInjector(modules = [GroupChoiceModule::class])
    abstract fun contributeGroupChoiceActivity(): GroupChoiceActivity

    @ContributesAndroidInjector
    abstract fun contributeMapActivity(): MapActivity

    @ContributesAndroidInjector
    abstract fun contributeHomeActivity(): HomeActivity

    @ContributesAndroidInjector(modules = [GroupListModule::class])
    abstract fun contributeGroupListActivity(): GroupListActivity
}