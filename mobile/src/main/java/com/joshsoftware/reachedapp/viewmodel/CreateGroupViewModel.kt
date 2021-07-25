package com.joshsoftware.reachedapp.viewmodel

import com.joshsoftware.core.AppSharedPreferences
import com.joshsoftware.core.model.Group
import com.joshsoftware.core.repository.GroupRepository
import com.joshsoftware.core.viewmodel.BaseViewModel
import javax.inject.Inject

class CreateGroupViewModel @Inject constructor(var repository: GroupRepository,
                                               var sharedPreferences: AppSharedPreferences)
    : BaseViewModel<Group>() {


}