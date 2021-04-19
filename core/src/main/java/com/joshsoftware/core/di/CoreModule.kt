package com.joshsoftware.core.di

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.joshsoftware.core.AppSharedPreferences
import com.joshsoftware.core.di.viewmodel.DaggerViewModelFactory
import com.joshsoftware.core.di.viewmodel.ViewModelKey
import com.joshsoftware.core.firebase.FirebaseAuthManager
import com.joshsoftware.core.ui.BaseLoginActivity
import com.joshsoftware.core.util.FirebaseRealtimeDbManager
import com.joshsoftware.core.viewmodel.GroupListViewModel
import com.joshsoftware.core.viewmodel.GroupMemberViewModel
import com.joshsoftware.core.viewmodel.LoginViewModel
import com.joshsoftware.core.viewmodel.MapViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import javax.inject.Singleton

@Module
abstract class CoreModule {

    @Binds
    @IntoMap
    @ViewModelKey(LoginViewModel::class)
    abstract fun buildLoginViewModel(viewModel: LoginViewModel): ViewModel
    @Binds
    @IntoMap
    @ViewModelKey(GroupMemberViewModel::class)
    abstract fun buildGroupMemberViewModel(viewModel: GroupMemberViewModel): ViewModel
    @Binds
    @IntoMap
    @ViewModelKey(GroupListViewModel::class)
    abstract fun buildGroupListViewModel(viewModel: GroupListViewModel): ViewModel
    @Binds
    @IntoMap
    @ViewModelKey(MapViewModel::class)
    abstract fun buildMapViewModel(viewModel: MapViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: DaggerViewModelFactory): ViewModelProvider.Factory

}