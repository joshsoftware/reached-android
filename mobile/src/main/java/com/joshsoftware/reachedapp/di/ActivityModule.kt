package com.joshsoftware.reachedapp.di

import com.joshsoftware.reachedapp.ui.LoginActivity
import com.joshsoftware.reachedapp.ui.activity.*
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

    @ContributesAndroidInjector(modules = [GroupListModule::class])
    abstract fun contributeHomeActivity(): HomeActivity

    @ContributesAndroidInjector
    abstract fun contributeProfileActivity(): ProfileActivity

    @ContributesAndroidInjector
    abstract fun contributePickLocationActivity(): PickLocationActivity

    @ContributesAndroidInjector
    abstract fun contributeSavePickedLocationActivity(): SavePickedLocationActivity

    @ContributesAndroidInjector
    abstract fun contributeGroupEditActivity(): GroupEditActivity

    @ContributesAndroidInjector
    abstract fun contributePermissionActivity(): PermissionsRequiredActivity

}