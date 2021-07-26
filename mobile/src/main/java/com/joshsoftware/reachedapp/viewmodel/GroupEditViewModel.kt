package com.joshsoftware.reachedapp.viewmodel

import com.joshsoftware.core.model.Group
import com.joshsoftware.core.model.Member
import com.joshsoftware.core.repository.GroupRepository
import com.joshsoftware.core.viewmodel.BaseViewModel
import javax.inject.Inject

class GroupEditViewModel @Inject constructor(val repository: GroupRepository): BaseViewModel<Member>() {


    fun deleteMember(member: Member, group: Group) {
        executeRoutine {
            _result.value = repository.deleteMember(member, group).await()
        }
    }
}