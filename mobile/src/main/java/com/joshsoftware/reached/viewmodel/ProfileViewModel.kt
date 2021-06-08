package com.joshsoftware.reached.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.joshsoftware.core.model.User
import com.joshsoftware.core.repository.UserRepository
import com.joshsoftware.core.viewmodel.BaseViewModel
import javax.inject.Inject

class ProfileViewModel @Inject constructor(var repository: UserRepository): BaseViewModel<User>() {

    fun fetchUserDetails(id: String) {
        executeRoutine {
            _result.value = repository.getUserDetailsFor(id)
        }
    }

}