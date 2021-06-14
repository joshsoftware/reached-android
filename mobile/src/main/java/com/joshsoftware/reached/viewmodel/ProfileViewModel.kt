package com.joshsoftware.reached.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.joshsoftware.core.model.User
import com.joshsoftware.core.repository.GroupRepository
import com.joshsoftware.core.repository.UserRepository
import com.joshsoftware.core.viewmodel.BaseViewModel
import javax.inject.Inject

class ProfileViewModel @Inject constructor(var repository: UserRepository,
                                           val groupRepository: GroupRepository)
    : BaseViewModel<User>() {

    fun fetchUserDetails(id: String) {
        executeRoutine {
            _result.value = repository.getUserDetailsFor(id)
        }
    }

    fun deleteAddress(groupId: String, memberId: String, addressId: String?): LiveData<Boolean> {
        val liveData = MutableLiveData<Boolean>()
        executeRoutine {
            addressId?.let {
                liveData.value = groupRepository.deleteAddress(groupId, memberId, addressId).await()
            }
        }
        return liveData
    }

}