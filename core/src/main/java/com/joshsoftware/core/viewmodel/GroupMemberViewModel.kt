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


}