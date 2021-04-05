package com.joshsoftware.reached.di

import com.joshsoftware.reached.ui.LoginActivity
import com.joshsoftware.reached.ui.activity.GroupChoiceActivity
import com.joshsoftware.reached.ui.activity.GroupMemberActivity
import com.joshsoftware.reached.ui.activity.MapActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityModule {

    @ContributesAndroidInjector
    abstract fun contributeLoginActivity(): LoginActivity

    @ContributesAndroidInjector
    abstract fun contributeGroupMemberActivity(): GroupMemberActivity

    @ContributesAndroidInjector
    abstract fun contributeGroupChoiceActivity(): GroupChoiceActivity

    @ContributesAndroidInjector
    abstract fun contributeMapActivity(): MapActivity
}