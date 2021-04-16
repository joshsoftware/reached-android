package com.joshsoftware.reached.di

import com.joshsoftware.core.service.LocationUpdateService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceModule {
    @ContributesAndroidInjector
    abstract fun contributeLocationUpdateService(): LocationUpdateService
}