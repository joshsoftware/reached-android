package com.joshsoftware.core

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.database.FirebaseDatabase
import com.joshsoftware.core.firebase.FirebaseAuthManager
import com.joshsoftware.core.model.User
import com.joshsoftware.core.util.FirebaseRealtimeDbManager
import javax.inject.Inject

class LoginRepository @Inject constructor(var authManager: FirebaseAuthManager,
                                          var dbManager: FirebaseRealtimeDbManager) {

    suspend fun signInWithGoogle(account: GoogleSignInAccount): Pair<String, User> {
        val (id, user) = authManager.firebaseAuthWithGoogle(account)
        dbManager.addUserWith(id, user)
        return id to user
    }
}