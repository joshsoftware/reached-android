package com.joshsoftware.core.viewmodel

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.joshsoftware.core.model.Group
import com.joshsoftware.core.model.LeaveRequestData
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
    private var _leaveGroupRequest = MutableLiveData<Boolean?>()
    val leaveGroupRequest: LiveData<Boolean?>
        get() = _leaveGroupRequest
    private var _leaveGroup = MutableLiveData<Boolean?>()
    val leaveGroup: LiveData<Boolean?>
        get() = _leaveGroup

    private var _requestExists = MutableLiveData<Boolean>()
    val requestExists: LiveData<Boolean>
        get() = _requestExists

    private var _leaveRequests = MutableLiveData<List<LeaveRequestData>>()
    val leaveRequests: LiveData<List<LeaveRequestData>>
        get() = _leaveRequests

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

    fun requestLeaveGroup(groupId: String, userId: String, createdBy: String, name: String, groupName: String) {
        executeRoutine {
            _leaveGroupRequest.value = repository.requestLeaveGroup(groupId, userId, createdBy, name, groupName)
        }
    }

    fun leaveGroup(requestId: String, groupId: String, userId: String) {
        executeRoutine {
            _leaveGroup.value = repository.leaveGroup(requestId, groupId, userId)
        }
    }

    fun declineGroupLeaveRequest(requestId: String, userId: String) {
        executeRoutine {
           repository.deleteRequestWith(requestId, userId)
        }
    }

    fun leaveRequestExists(userId: String, groupId: String) {
        executeRoutine {
            _requestExists.value = repository.leaveRequestExists(userId, groupId)
        }
    }

    fun getLeaveRequests(groupId: String) {
        executeRoutine {
            _leaveRequests.value = repository.getLeaveRequests(groupId)
        }
    }


}