package com.joshsoftware.core.repository

import android.location.Location
import com.google.firebase.database.DatabaseError
import com.joshsoftware.core.model.*
import com.joshsoftware.core.util.FirebaseRealtimeDbManager
import kotlinx.coroutines.Deferred
import java.lang.Exception
import javax.inject.Inject
import kotlin.collections.ArrayList

class GroupRepository @Inject constructor(var dbManager: FirebaseRealtimeDbManager) {

    suspend fun createGroup(id: String, userId: String, user: User, groupName: String,  lat: Double, long: Double): Pair<Group, User> {
        return dbManager.createGroupWith(id, userId, user, groupName, lat, long)
    }

    suspend fun joinGroup(id: String, userId: String, user: User, lat: Double, long: Double): Pair<String, User> {
        return dbManager.joinGroupWith(id, userId, user, lat, long)
    }

    suspend fun fetchGroup(userId: String): String? {
        return dbManager.fetchGroup(userId)
    }


    fun fetchGroupList(userId: String,
                               onSuccess: (ArrayList<Group>) -> Unit,
                               onError: (Exception) -> Unit) {
        dbManager.fetchGroupList(userId, onSuccess, onError)
    }

    fun fetchGroupDetails(groupId: String,
                          onFetch: (Group?) -> Unit,
                          onCancel: (DatabaseError) -> Unit) {
        dbManager.fetchGroupDetails(groupId, onFetch, onCancel)
    }

    suspend fun updateLocation(groupId: String, userId: String, location: Location): Group? {
        return dbManager.updateLocation(groupId, userId, location)
    }
    suspend fun sendSos(userId: String, user: User): Boolean? {
        return dbManager.toggleSosState(user, userId)
    }

    suspend fun markSafe(userId: String, member: Member): Boolean? {
        return dbManager.markUserSafe(member, userId)
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

    suspend fun requestLeaveGroup(groupId: String, userId: String, createdBy: String, name: String, groupName: String): Boolean? {
        return dbManager.requestLeaveGroup(groupId, userId, createdBy, name, groupName)
    }

    suspend fun leaveGroup(requestId: String, groupId: String, userId: String): Boolean? {
        return dbManager.leaveGroup(requestId, groupId, userId)
    }


    suspend fun deleteGroup(group: Group): Boolean? {
        return dbManager.deleteGroup(group)
    }

    suspend fun leaveRequestExists(userId: String, groupId: String): Boolean {
        return dbManager.checkIfLeaveRequestExists(userId, groupId)
    }

    suspend fun deleteRequestWith(requestId: String, userId: String): Boolean {
        return dbManager.deleteRequestWith(requestId, userId)
    }

    suspend fun getLeaveRequests(groupId: String): List<LeaveRequestData> {
        return dbManager.getRequests(groupId)
    }

    fun deleteAddress(groupId: String, memberId: String, addressId: String): Deferred<Boolean> {
        return dbManager.deleteAddress(groupId, memberId, addressId)
    }
}