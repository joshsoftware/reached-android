package com.joshsoftware.reached.di

import android.app.Application
import com.joshsoftware.core.di.CoreAppModule
import com.joshsoftware.core.di.CoreModule
import com.joshsoftware.reached.FamApp
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AndroidInjectionModule::class,
    AppModule::class,
    ActivityModule::class,
    ViewModelModule::class,
    CoreModule::class,
    CoreAppModule::class
])
interface AppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }

    fun inject(famApp: FamApp)
}