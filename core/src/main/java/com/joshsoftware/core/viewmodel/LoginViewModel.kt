package com.joshsoftware.core.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.joshsoftware.core.LoginRepository
import com.joshsoftware.core.di.AppType
import com.joshsoftware.core.model.User
import com.joshsoftware.core.repository.GroupRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import javax.inject.Inject

class LoginViewModel @Inject constructor(var repository: LoginRepository,
                                         var groupRepo: GroupRepository)
    : BaseViewModel<Pair<String, User>>() {
    protected var _groupId = MutableLiveData<String?>()
    val groupId: LiveData<String?>
        get() = _groupId

    protected var _user = MutableLiveData<User?>()
    val user: LiveData<User?>
        get() = _user

    fun signInWithGoogle(account: GoogleSignInAccount, appType: AppType) {
        _spinner.value = true

        executeRoutine {
            withContext(Dispatchers.IO) {
                val idToUser = repository.signInWithGoogle(account, appType)
                withContext(Dispatchers.Main) {
                    _result.value = idToUser
                }
            }
        }
    }

    fun fetchGroup(userId: String) {
        _spinner.value = true

        executeRoutine {
            withContext(Dispatchers.IO) {
                val groupId = groupRepo.fetchGroup(userId)
                withContext(Dispatchers.Main) {
                    _groupId.value = groupId
                }
            }
        }
    }

    fun fetchUserDetails(userId: String) {
        executeRoutine {
            _user.value = repository.fetchUserDetails(userId)
        }
    }
}