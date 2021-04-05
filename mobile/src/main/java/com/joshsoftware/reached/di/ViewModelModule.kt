package com.joshsoftware.reached.di

import androidx.lifecycle.ViewModel
import com.joshsoftware.core.di.viewmodel.ViewModelKey
import com.joshsoftware.reached.ui.activity.GroupChoiceViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(GroupChoiceViewModel::class)
    abstract fun buildGroupChoiceViewModel(viewModel: GroupChoiceViewModel): ViewModel

}