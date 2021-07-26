package com.joshsoftware.reachedapp.di

import com.joshsoftware.reachedapp.ui.dialog.CreateGroupDialog
import com.joshsoftware.reachedapp.ui.dialog.JoinGroupDialog
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class GroupChoiceModule {
    @ContributesAndroidInjector
    abstract fun contributeCreateGroupDialog(): CreateGroupDialog
    @ContributesAndroidInjector
    abstract fun contributeJoinGroupDialog(): JoinGroupDialog
}