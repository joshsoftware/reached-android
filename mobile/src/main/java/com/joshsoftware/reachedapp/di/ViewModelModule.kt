package com.joshsoftware.reachedapp.di

import androidx.lifecycle.ViewModel
import com.joshsoftware.core.di.viewmodel.ViewModelKey
import com.joshsoftware.reachedapp.ui.activity.GroupChoiceViewModel
import com.joshsoftware.reachedapp.viewmodel.*
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(GroupChoiceViewModel::class)
    abstract fun buildGroupChoiceViewModel(viewModel: GroupChoiceViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreateGroupViewModel::class)
    abstract fun buildCreateGroupViewModel(viewModel: CreateGroupViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HomeViewModel::class)
    abstract fun buildHomeViewModel(viewModel: HomeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProfileViewModel::class)
    abstract fun buildHProfileViewModel(viewModel: ProfileViewModel): ViewModel
    @Binds
    @IntoMap
    @ViewModelKey(SaveLocationViewModel::class)
    abstract fun buildHSaveLocationViewModel(viewModel: SaveLocationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GroupEditViewModel::class)
    abstract fun buildGroupEditViewModel(viewModel: GroupEditViewModel): ViewModel


    @Binds
    @IntoMap
    @ViewModelKey(SosViewModel::class)
    abstract fun buildSosViewModel(viewModel: SosViewModel): ViewModel


}