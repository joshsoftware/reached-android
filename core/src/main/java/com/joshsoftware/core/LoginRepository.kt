package com.joshsoftware.core

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.joshsoftware.core.di.AppType
import com.joshsoftware.core.firebase.FirebaseAuthManager
import com.joshsoftware.core.model.User
import com.joshsoftware.core.util.FirebaseRealtimeDbManager
import javax.inject.Inject

class LoginRepository @Inject constructor(var authManager: FirebaseAuthManager,
                                          var dbManager: FirebaseRealtimeDbManager) {

    suspend fun fetchUserDetails(userId: String): User? {
        return dbManager.fetchUserDetails(userId)
    }

    fun updateUserPhoneToken(userId: String, token: String) {
        return dbManager.updateUserPhoneToken(userId, token)
    }

    fun updateUserWatchToken(userId: String, token: String) {
        return dbManager.updateUserWatchToken(userId, token)
    }

    suspend fun signInWithGoogle(account: GoogleSignInAccount, appType: AppType): Pair<String, User> {
        val (id, user, token) = authManager.firebaseAuthWithGoogle(account)
        dbManager.addUserWith(id, user, token, appType)
        if(appType == AppType.MOBILE) {
            user.token.phone = token
        } else {
            user.token.watch = token
        }
        return id to user
    }
}