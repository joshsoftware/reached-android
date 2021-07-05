package com.joshsoftware.reached

import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.BroadcastReceiver
import com.joshsoftware.reached.di.AppInjector
import dagger.android.*
import javax.inject.Inject

class FamApp: Application(), HasActivityInjector, HasServiceInjector, HasBroadcastReceiverInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    @Inject
    lateinit var dispatchingServiceInjector: DispatchingAndroidInjector<Service>

    @Inject
    lateinit var dispatchingReceiverInjector: DispatchingAndroidInjector<BroadcastReceiver>

    override fun onCreate() {
        super.onCreate()
        AppInjector.inject(this)
    }

    override fun activityInjector(): AndroidInjector<Activity> = dispatchingAndroidInjector
    override fun serviceInjector(): AndroidInjector<Service>  = dispatchingServiceInjector
    override fun broadcastReceiverInjector(): AndroidInjector<BroadcastReceiver> = dispatchingReceiverInjector

}