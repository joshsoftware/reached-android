package com.joshsoftware.reachedapp.viewmodel

import androidx.lifecycle.MutableLiveData
import com.joshsoftware.core.model.User
import com.joshsoftware.core.repository.GroupRepository
import com.joshsoftware.core.viewmodel.BaseViewModel
import javax.inject.Inject

class SosViewModel @Inject constructor(val repository: GroupRepository): BaseViewModel<Boolean>() {

    fun sendSos(userId: String, user: User): MutableLiveData<Boolean> {
        val liveData = MutableLiveData<Boolean>()
        executeRoutine {
            liveData.value = repository.sendSos(userId, user)
        }
        return liveData
    }

}