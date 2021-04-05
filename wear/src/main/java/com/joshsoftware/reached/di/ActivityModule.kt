package com.joshsoftware.reached.di

import com.joshsoftware.reached.ui.GroupMemberActivity
import com.joshsoftware.reached.ui.GroupWaitActivity
import com.joshsoftware.reached.ui.LoginActivity
import com.joshsoftware.reached.ui.MapActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityModule {
    @ContributesAndroidInjector
    abstract fun contributeLoginActivity(): LoginActivity

    @ContributesAndroidInjector
    abstract fun contributeGroupMemberActivity(): GroupMemberActivity

    @ContributesAndroidInjector
    abstract fun contributeGroupMapActivity(): MapActivity

    @ContributesAndroidInjector
    abstract fun contributeGroupWaitActivity(): GroupWaitActivity

}