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

    private var _sos = MutableLiveData<SosUser?>()
    val sos: LiveData<SosUser?>
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

    fun sendSos(groupId: String, userId: String, user: User) {
        executeRoutine {
            repository.sendSos(groupId, userId, user)
        }
    }

    fun deleteSos(groupId: String) {
        viewModelScope.launch {
            repository.deleteSos(groupId)
        }
    }

    fun observeSos(groupId: String) {
        repository.observeSos(groupId, {
            _sos.value = it
        }, {
                                  _error.value = it.message
                              })
    }

}