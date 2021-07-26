package com.joshsoftware.reachedapp.di

import androidx.lifecycle.ViewModel
import com.joshsoftware.core.di.viewmodel.ViewModelKey
import com.joshsoftware.reachedapp.viewmodel.GroupWaitViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(GroupWaitViewModel::class)
    abstract fun buildGroupWaitViewModel(viewModel: GroupWaitViewModel): ViewModel

}