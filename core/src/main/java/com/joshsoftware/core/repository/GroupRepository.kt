package com.joshsoftware.core.repository

import android.location.Location
import com.google.firebase.database.DatabaseError
import com.joshsoftware.core.model.Group
import com.joshsoftware.core.model.Member
import com.joshsoftware.core.model.SosUser
import com.joshsoftware.core.model.User
import com.joshsoftware.core.util.FirebaseRealtimeDbManager
import javax.inject.Inject
import kotlin.collections.ArrayList

class GroupRepository @Inject constructor(var dbManager: FirebaseRealtimeDbManager) {

    suspend fun createGroup(id: String, userId: String, user: User, groupName: String,  lat: Double, long: Double): Group {
        return dbManager.createGroupWith(id, userId, user, groupName, lat, long)
    }

    suspend fun joinGroup(id: String, userId: String, user: User, lat: Double, long: Double): String {
        return dbManager.joinGroupWith(id, userId, user, lat, long)
    }

    suspend fun fetchGroup(userId: String): String? {
        return dbManager.fetchGroup(userId)
    }


    suspend fun fetchGroupList(user: User): ArrayList<Group> {
        return dbManager.fetchGroupList(user)
    }

    fun fetchGroupDetails(groupId: String,
                          onFetch: (Group?) -> Unit,
                          onCancel: (DatabaseError) -> Unit) {
        dbManager.fetchGroupDetails(groupId, onFetch, onCancel)
    }

    suspend fun updateLocation(groupId: String, userId: String, location: Location): Group? {
        return dbManager.updateLocation(groupId, userId, location)
    }
    suspend fun sendSos(groupId: String, userId: String, user: User): String? {
        return dbManager.sendSOS(groupId, user, userId)
    }

    suspend fun deleteSos(groupId: String): String? {
        return dbManager.deleteSos(groupId)
    }

    fun fetchMember(groupId: String, memberId: String,
                    onFetch: (Member?) -> Unit,
                    onCancel: (DatabaseError) -> Unit) {
        dbManager.fetchMember(groupId, memberId, onFetch, onCancel)
    }

    fun isGroupCreated(userId: String,
                       onFetch: (Boolean) -> Unit,
                       onCancel: (DatabaseError) -> Unit) {
        dbManager.isGroupCreated(userId, onFetch, onCancel)
    }

    fun fetchMembers(groupId: String,
                     onFetch: (ArrayList<Member>) -> Unit,
                     onCancel: (DatabaseError) -> Unit) {
        dbManager.fetchMembers(groupId, onFetch, onCancel)
    }

    fun observeSos(groupId: String,
                     onFetch: (SosUser?) -> Unit,
                     onCancel: (DatabaseError) -> Unit) {
        dbManager.observeSos(groupId, onFetch, onCancel)
    }

}