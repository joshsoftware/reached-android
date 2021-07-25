package com.joshsoftware.reachedapp.di

import com.joshsoftware.core.geofence.GeofenceBroadcastReceiver
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class BroadcastReceiverModule {

    @ContributesAndroidInjector
    abstract fun contributeGeofenceBroadcastReceiver(): GeofenceBroadcastReceiver
}