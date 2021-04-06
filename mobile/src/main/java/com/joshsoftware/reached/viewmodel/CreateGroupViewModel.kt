package com.joshsoftware.reached.viewmodel

import com.joshsoftware.core.model.User
import com.joshsoftware.core.repository.GroupRepository
import com.joshsoftware.core.viewmodel.BaseViewModel
import javax.inject.Inject

class CreateGroupViewModel @Inject constructor(var repository: GroupRepository): BaseViewModel<String>() {

    fun createGroup(id: String, userId: String, user: User, groupName: String) {
        executeRoutine {
            val id = repository.createGroup(id, userId, user, groupName)
            _result.value = id
        }
    }

}