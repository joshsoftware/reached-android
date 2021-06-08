package com.joshsoftware.core.repository

import com.joshsoftware.core.model.User
import com.joshsoftware.core.util.FirebaseRealtimeDbManager
import javax.inject.Inject

class UserRepository @Inject constructor(val dbManager: FirebaseRealtimeDbManager) {

    suspend fun getUserDetailsFor(id: String): User? {
        return dbManager.fetchUserDetails(id)
    }
}