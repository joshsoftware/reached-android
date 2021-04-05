package com.joshsoftware.core.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.joshsoftware.core.model.Member
import com.joshsoftware.core.repository.GroupRepository
import javax.inject.Inject

class MapViewModel @Inject constructor(var repository: GroupRepository)
    : BaseViewModel<Member?>() {
    protected var _members = MutableLiveData<ArrayList<Member>>()

    val members: LiveData<ArrayList<Member>>
        get() = _members

    fun observeLocationChanges(groupId: String, memberId: String) {
        executeRoutine {
            repository.fetchMember(groupId, memberId, {
                _result.value = it
            }, {
                _error.value = it.message
            })

        }
    }

    fun observeMembersForChanges(groupId: String) {
            repository.fetchMembers(groupId, {
                _members.value = it
            }, {
                _error.value = it.message
            })
    }

}