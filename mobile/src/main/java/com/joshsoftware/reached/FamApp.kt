package com.joshsoftware.reached

import android.app.Activity
import android.app.Application
import android.app.Service
import com.joshsoftware.reached.di.AppInjector
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.HasServiceInjector
import javax.inject.Inject

class FamApp: Application(), HasActivityInjector, HasServiceInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    @Inject
    lateinit var dispatchingServiceInjector: DispatchingAndroidInjector<Service>

    override fun onCreate() {
        super.onCreate()
        AppInjector.inject(this)
    }

    override fun activityInjector(): AndroidInjector<Activity> = dispatchingAndroidInjector
    override fun serviceInjector(): AndroidInjector<Service>  = dispatchingServiceInjector

}