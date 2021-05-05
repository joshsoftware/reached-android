package com.joshsoftware.reached.ui.activity

import android.location.Location
import androidx.lifecycle.MutableLiveData
import com.joshsoftware.core.AppSharedPreferences
import com.joshsoftware.core.model.User
import com.joshsoftware.core.repository.GroupRepository
import com.joshsoftware.core.viewmodel.BaseViewModel
import javax.inject.Inject

class GroupChoiceViewModel @Inject constructor(var repository: GroupRepository,
                                               var sharedPreferences: AppSharedPreferences)
    : BaseViewModel<String>() {

    fun joinGroup(id: String, userId: String, user: User,  lat: Double, long: Double): MutableLiveData<String> {
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