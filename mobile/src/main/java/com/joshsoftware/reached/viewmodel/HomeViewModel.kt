package com.joshsoftware.reached.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.joshsoftware.core.AppSharedPreferences
import com.joshsoftware.core.model.Group
import com.joshsoftware.core.model.User
import com.joshsoftware.core.repository.GroupRepository
import com.joshsoftware.core.viewmodel.BaseViewModel
import javax.inject.Inject

class HomeViewModel @Inject constructor(val sharedPreferences: AppSharedPreferences,
                                        val repository: GroupRepository)
    : BaseViewModel<MutableList<Group>>() {

    fun fetchGroups(userId: String) {
        executeRoutine {
            repository.fetchGroupList(userId, {
                _result.value = it
            }, {
                _error.value = it.localizedMessage
            })
        }
    }

    fun joinGroup(id: String, userId: String, user: User, lat: Double, long: Double): MutableLiveData<String> {
        val liveData = MutableLiveData<String>()
        executeRoutine {
            val (id, user) = repository.joinGroup(id, userId, user, lat, long)
            sharedPreferences.userData?.let {
                it.groups = user.groups
                sharedPreferences.saveUserData(it)
            }
            liveData.value = id
        }
        return liveData
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
}