package com.joshsoftware.core.viewmodel

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.joshsoftware.core.model.Group
import com.joshsoftware.core.model.SosUser
import com.joshsoftware.core.model.User
import com.joshsoftware.core.repository.GroupRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GroupMemberViewModel @Inject constructor(var repository: GroupRepository): BaseViewModel<Group?>() {

    private var _sos = MutableLiveData<Boolean?>()
    val sos: LiveData<Boolean?>
        get() = _sos
    private var _deleteGroup = MutableLiveData<Boolean?>()
    val deleteGroup: LiveData<Boolean?>
        get() = _deleteGroup
    private var _leaveGroup = MutableLiveData<Boolean?>()
    val leaveGroup: LiveData<Boolean?>
        get() = _leaveGroup

    fun fetchGroupDetails(groupId: String) {
            repository.fetchGroupDetails(groupId, {
                _result.value = it
            }, {
                _error.value = it.message
            })
    }

    fun updateLocationForMember(groupId: String, userId: String, location: Location) {
        viewModelScope.launch {
            repository.updateLocation(groupId, userId, location)
        }
    }

    fun sendSos(groupId: String, userId: String, user: User, sosSent: Boolean) {
        executeRoutine {
            _sos.value = repository.sendSos(groupId, userId, user, sosSent)
        }
    }

    fun deleteGroup(group: Group, userId: String) {
        executeRoutine {
            _deleteGroup.value = repository.deleteGroup(group, userId)
        }
    }

    fun leaveGroup(groupId: String, userId: String) {
        executeRoutine {
            _leaveGroup.value = repository.leaveGroup(groupId, userId)
        }
    }


}