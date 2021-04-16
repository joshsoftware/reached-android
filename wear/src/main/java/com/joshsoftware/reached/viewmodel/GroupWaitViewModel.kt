package com.joshsoftware.reached.viewmodel

import com.joshsoftware.core.repository.GroupRepository
import com.joshsoftware.core.viewmodel.BaseViewModel
import javax.inject.Inject

class GroupWaitViewModel @Inject constructor(var repository: GroupRepository): BaseViewModel<Boolean>() {

    fun checkIfGroupJoinedOrCreated(userId: String) {
        executeRoutine {
            repository.isGroupCreated(userId,  {
                _result.value = it
            }, {
                _error.value = it.message
            })
        }
    }

}