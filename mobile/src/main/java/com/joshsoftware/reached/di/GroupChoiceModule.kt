package com.joshsoftware.reached.di

import com.joshsoftware.reached.ui.activity.GroupChoiceActivity
import com.joshsoftware.reached.ui.dialog.CreateGroupDialog
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class GroupChoiceModule {
    @ContributesAndroidInjector
    abstract fun contributeCreateGroupDialog(): CreateGroupDialog
}