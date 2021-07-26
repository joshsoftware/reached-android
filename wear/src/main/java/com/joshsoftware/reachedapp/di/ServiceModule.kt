package com.joshsoftware.reachedapp.di

import com.joshsoftware.reachedapp.service.ReachedWearFirebaseService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceModule {
    @ContributesAndroidInjector
    abstract fun contributeReachedWearMessagingService(): ReachedWearFirebaseService
}