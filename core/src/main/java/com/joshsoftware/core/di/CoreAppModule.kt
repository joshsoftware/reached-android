package com.joshsoftware.core.di

import android.app.Application
import com.joshsoftware.core.AppSharedPreferences
import com.joshsoftware.core.firebase.FirebaseAuthManager
import com.joshsoftware.core.util.FirebaseRealtimeDbManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class CoreAppModule {

    @Provides
    @Singleton
    fun providesAppPrefs(context: Application): AppSharedPreferences {
        return AppSharedPreferences(context)
    }

    @Provides
    @Singleton
    fun providesFirebaseRealtimeDbManager(): FirebaseRealtimeDbManager {
        return FirebaseRealtimeDbManager()
    }

    @Provides
    @Singleton
    fun providesFirebaseAuthDbManager(): FirebaseAuthManager {
        return FirebaseAuthManager()
    }
}