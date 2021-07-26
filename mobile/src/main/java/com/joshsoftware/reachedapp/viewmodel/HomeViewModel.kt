package com.joshsoftware.reachedapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.joshsoftware.core.AppSharedPreferences
import com.joshsoftware.core.model.Group
import com.joshsoftware.core.model.LeaveRequestData
import com.joshsoftware.core.model.User
import com.joshsoftware.core.repository.GroupRepository
import com.joshsoftware.core.viewmodel.BaseViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class HomeViewModel @Inject constructor(val sharedPreferences: AppSharedPreferences,
                                        val repository: GroupRepository)
    : BaseViewModel<MutableList<Group>>() {

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

    fun fetchGroups(userId: String) {
        viewModelScope.launch {
            _spinner.value = true
            repository.fetchGroupList(userId, {
                _result.value = it
                _spinner.value = false
            }, {
                _error.value = it.localizedMessage
                _spinner.value = false
            })
        }

    }

    fun createGroup(id: String, userId: String, user: User, groupName: String, lat: Double, long: Double): LiveData<Group> {
        val liveData = MutableLiveData<Group>()
        executeRoutine {
            val (group, user) = repository.createGroup(id, userId, user, groupName, lat, long)
            sharedPreferences.userData?.let {
                it.groups = user.groups
                sharedPreferences.saveUserData(it)
            }
            liveData.value = group
        }
        return liveData
    }

    fun deleteGroup(group: Group): LiveData<Boolean> {
        val liveData = MutableLiveData<Boolean>()
        executeRoutine {
            liveData.value = repository.deleteGroup(group)
        }
        return liveData
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