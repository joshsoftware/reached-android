package com.joshsoftware.core.viewmodel

import android.location.Location
import androidx.lifecycle.MutableLiveData
import com.joshsoftware.core.model.Group
import com.joshsoftware.core.model.User
import com.joshsoftware.core.repository.GroupRepository
import com.joshsoftware.core.viewmodel.BaseViewModel
import javax.inject.Inject

class GroupListViewModel @Inject constructor(var repository: GroupRepository): BaseViewModel<ArrayList<Group>>() {

    fun fetchGroups(user: User) {
        executeRoutine {
            _result.value = repository.fetchGroupList(user)
        }
    }


    fun joinGroup(id: String, userId: String, user: User, lat: Double, long: Double): MutableLiveData<String> {
        val liveData = MutableLiveData<String>()
        executeRoutine {
            val id = repository.joinGroup(id, userId, user, lat, long)
            liveData.value = id
        }
        return liveData
    }
}