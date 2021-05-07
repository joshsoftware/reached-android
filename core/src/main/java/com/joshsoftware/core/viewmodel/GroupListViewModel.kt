package com.joshsoftware.core.viewmodel

import android.location.Location
import androidx.lifecycle.MutableLiveData
import com.joshsoftware.core.AppSharedPreferences
import com.joshsoftware.core.model.Group
import com.joshsoftware.core.model.User
import com.joshsoftware.core.repository.GroupRepository
import com.joshsoftware.core.viewmodel.BaseViewModel
import javax.inject.Inject

class GroupListViewModel @Inject constructor(var repository: GroupRepository,
                                             var sharedPreferences: AppSharedPreferences
): BaseViewModel<ArrayList<Group>>() {

    fun fetchGroups(userId: String) {
        executeRoutine {
            val groups = repository.fetchGroupList(userId)
            sharedPreferences.userData?.let {
                it.groups.clear()
                groups.forEach { group ->
                    group.id?.let {gId ->
                        it.groups[gId] = true
                    }
                }
                sharedPreferences.saveUserData(it)
            }
            _result.value = groups
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
}