package com.joshsoftware.reached.ui.activity

import androidx.lifecycle.MutableLiveData
import com.joshsoftware.core.model.User
import com.joshsoftware.core.repository.GroupRepository
import com.joshsoftware.core.viewmodel.BaseViewModel
import javax.inject.Inject

class GroupChoiceViewModel @Inject constructor(var repository: GroupRepository): BaseViewModel<String>() {

    fun createGroup(id: String, userId: String, user: User) {
        executeRoutine {
            val id = repository.createGroup(id, userId, user)
                _result.value = id
        }
    }
    fun joinGroup(id: String, userId: String, user: User): MutableLiveData<String> {
        val liveData = MutableLiveData<String>()
        executeRoutine {
            val id = repository.joinGroup(id, userId, user)
            liveData.value = id
        }
        return liveData
    }
}