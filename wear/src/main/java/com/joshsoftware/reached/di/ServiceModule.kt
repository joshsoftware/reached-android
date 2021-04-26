package com.joshsoftware.reached.di

import com.joshsoftware.reached.service.ReachedWearFirebaseService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceModule {
    @ContributesAndroidInjector
    abstract fun contributeReachedWearMessagingService(): ReachedWearFirebaseService
}