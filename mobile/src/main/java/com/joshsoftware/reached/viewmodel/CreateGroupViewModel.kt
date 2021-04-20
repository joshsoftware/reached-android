package com.joshsoftware.reached.viewmodel

import android.location.Location
import com.joshsoftware.core.model.Group
import com.joshsoftware.core.model.User
import com.joshsoftware.core.repository.GroupRepository
import com.joshsoftware.core.viewmodel.BaseViewModel
import javax.inject.Inject

class CreateGroupViewModel @Inject constructor(var repository: GroupRepository): BaseViewModel<Group>() {

    fun createGroup(id: String, userId: String, user: User, groupName: String,  lat: Double, long: Double) {
        executeRoutine {
            val id = repository.createGroup(id, userId, user, groupName, lat, long)
            _result.value = id
        }
    }

}