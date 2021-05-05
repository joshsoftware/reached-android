package com.joshsoftware.reached.di

import com.joshsoftware.reached.ui.dialog.CreateGroupDialog
import com.joshsoftware.reached.ui.dialog.JoinGroupDialog
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class GroupListModule {

    @ContributesAndroidInjector
    abstract fun contributeJoinGroupDialog(): JoinGroupDialog
    @ContributesAndroidInjector
    abstract fun contributeCreateGroupDialog(): CreateGroupDialog
}