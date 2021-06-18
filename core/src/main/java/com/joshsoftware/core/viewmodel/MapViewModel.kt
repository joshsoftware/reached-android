package com.joshsoftware.core.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.joshsoftware.core.model.Group
import com.joshsoftware.core.model.Member
import com.joshsoftware.core.model.User
import com.joshsoftware.core.repository.GroupRepository
import javax.inject.Inject

class MapViewModel @Inject constructor(var repository: GroupRepository)
    : BaseViewModel<Member?>() {
    protected val _members = MutableLiveData<ArrayList<Member>>()
    val members: LiveData<ArrayList<Member>>
        get() = _members

    protected val _groups = MutableLiveData<MutableList<Group>>()
    val groups: LiveData<MutableList<Group>>
        get() = _groups

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

    fun fetchGroups(userId: String) {
        executeRoutine {
            repository.fetchGroupList(userId, {
                _groups.value = it
            }, {
                _error.value = it.localizedMessage
            })
        }
    }

    fun markSafe(userId: String, member: Member): LiveData<Boolean> {
        val liveData = MutableLiveData<Boolean>()
        executeRoutine {
           liveData.value = repository.markSafe(userId, member)
        }
        return liveData
    }

}