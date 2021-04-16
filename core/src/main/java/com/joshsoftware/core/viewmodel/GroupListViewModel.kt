package com.joshsoftware.core.viewmodel

import com.joshsoftware.core.model.Group
import com.joshsoftware.core.model.User
import com.joshsoftware.core.repository.GroupRepository
import com.joshsoftware.core.viewmodel.BaseViewModel
import javax.inject.Inject

class GroupListViewModel @Inject constructor(var repository: GroupRepository): BaseViewModel<ArrayList<Group>>() {

    fun fetchGroups(user: User) {
        executeRoutine {
            _result.value = repository.fetchGroupList(user)
        }
    }
}