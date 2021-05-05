package com.joshsoftware.reached.di

import com.joshsoftware.core.service.LocationUpdateService
import com.joshsoftware.reached.service.ReachedFirebaseService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceModule {
    @ContributesAndroidInjector
    abstract fun contributeLocationUpdateService(): LocationUpdateService
    @ContributesAndroidInjector
    abstract fun contributeReachedFirebaseService(): ReachedFirebaseService
}