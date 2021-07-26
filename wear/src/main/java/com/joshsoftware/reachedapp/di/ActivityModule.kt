package com.joshsoftware.reachedapp.di

import com.joshsoftware.reachedapp.ui.*
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityModule {
    @ContributesAndroidInjector
    abstract fun contributeLoginActivity(): WearLoginActivity

    @ContributesAndroidInjector
    abstract fun contributeGroupMemberActivity(): GroupMemberActivity

    @ContributesAndroidInjector
    abstract fun contributeGroupMapActivity(): MapActivity
    @ContributesAndroidInjector
    abstract fun contributeWearGroupListActivity(): WearGroupListActivity

    @ContributesAndroidInjector
    abstract fun contributeGroupWaitActivity(): GroupWaitActivity

}