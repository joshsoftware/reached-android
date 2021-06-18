package com.joshsoftware.core.util

import android.location.Location
import com.google.android.gms.common.api.Response
import com.google.firebase.database.*
import com.google.gson.Gson
import com.joshsoftware.core.BuildConfig
import com.joshsoftware.core.di.AppType
import com.joshsoftware.core.model.*
import com.joshsoftware.core.util.FirebaseDatabaseKey.*
import kotlinx.coroutines.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FirebaseRealtimeDbManager {
    private val db = FirebaseDatabase.getInstance(BuildConfig.DATABASE_URL)
    val groupReference = db.getReference(GROUPS.key)
    val userReference = db.getReference(USERS.key)
    val requestReference = db.getReference(REQUESTS.key)
    val dateTimeUtils = DateTimeUtils()

    suspend fun addUserWith(id: String, user: User, token: String, appType: AppType) = suspendCoroutine<User> { continuation ->
        userReference.child(id).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val requestedUser = snapshot.getValue(User::class.java)
                if(requestedUser != null) {
                    if(appType == AppType.MOBILE) {
                        requestedUser.token.phone = token
                    } else {
                        requestedUser.token.watch = token
                    }
                    userReference.child(id).setValue(requestedUser).addOnCompleteListener {
                        if(it.isSuccessful) {
                            continuation.resume(user)
                        } else {
                            it.exception?.let { ex ->
                                continuation.resumeWithException(ex)
                            }
                        }
                    }
                } else {
                    if(appType == AppType.MOBILE) {
                        user.token.phone = token
                    } else {
                        user.token.watch = token
                    }
                    userReference.child(id).setValue(user).addOnCompleteListener {
                        if(it.isSuccessful) {
                            continuation.resume(user)
                        } else {
                            it.exception?.let { ex ->
                                continuation.resumeWithException(ex)
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                continuation.resumeWithException(error.toException())
            }
        })
    }

    suspend fun toggleSosState(user: User, userId: String) = suspendCoroutine<Boolean?> { continuation ->
        userReference.child(userId).child("sosState").addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.value
                if(value  != null) {
                    if(!(value as Boolean)) {
                        userReference.child(userId).child("sosState").setValue(true).addOnCompleteListener {
                            if(it.isSuccessful) {
                                user.groups.forEach { (t, u) ->
                                    groupReference
                                            .child(t)
                                            .child(MEMBERS.key)
                                            .child(userId)
                                            .child("sosState")
                                            .setValue(true)
                                }
                                continuation.resume(true)
                            } else {
                                it.exception?.let { ex -> continuation.resumeWithException(ex) }
                            }
                        }
                    } else {
                        continuation.resume(true)
                    }

                } else {
                    userReference.child(userId).child("sosState").setValue(true).addOnCompleteListener {
                        if(it.isSuccessful) {
                            continuation.resume(true)
                        } else {
                            it.exception?.let { ex -> continuation.resumeWithException(ex) }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

    }

    suspend fun createGroupWith(id: String, userId: String, user: User, groupName: String,  lat: Double, long: Double) = suspendCoroutine<Pair<Group, User>> { continuation ->
        val map = hashMapOf<String, Member>()
        map[userId] = Member(name = user.name, profileUrl = user.profileUrl, lat = lat, long = long, lastUpdated = dateTimeUtils.getCurrentTime())
        val group = Group(
            members = map,
            created_by = userId,
            name = groupName
        )
        groupReference.child(id).setValue(group).addOnCompleteListener {
            if(it.isSuccessful) {
                updateUserWithGroup(id, user, userId,  { responseUser ->
                    group.id = id
                    continuation.resume(group to responseUser)
                },  { ex ->
                    continuation.resumeWithException(ex)
                })
            } else {
                it.exception?.let { ex ->
                    continuation.resumeWithException(ex)
                }
            }
        }
    }

    private fun updateUserWithGroup(
        groupId: String,
        user: User,
        userId: String,
        onComplete: (User) -> Unit,
        onError: (Exception) -> Unit
    ) {
        userReference.child(userId).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                onError(error.toException())
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val requestedUser = snapshot.getValue(User::class.java)
                if(requestedUser != null) {
                    requestedUser.groups[groupId] = true
                    userReference.child(userId).setValue(requestedUser).addOnCompleteListener {
                        if(it.isSuccessful) {
                            onComplete(requestedUser)
                        } else {
                            it.exception?.let(onError)
                        }
                    }
                } else {
                    user.groups[groupId] = true
                    userReference.child(userId).setValue(user).addOnCompleteListener {
                        if(it.isSuccessful) {
                            onComplete(user)
                        } else {
                            it.exception?.let(onError)
                        }
                    }
                }

            }

        })
    }

    suspend fun joinGroupWith(id: String, userId: String, user: User, lat: Double, long: Double) = suspendCoroutine<Pair<String, User>> { continuation ->
        groupReference.child(id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                continuation.resumeWithException(error.toException())
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val group = snapshot.getValue(Group::class.java)
                group?.let {
                    it.members.put(userId, Member(name = user.name, profileUrl = user.profileUrl, lat = lat, long = long, lastUpdated = dateTimeUtils.getCurrentTime()))
                }
                groupReference.child(id).setValue(group).addOnCompleteListener {
                    if(it.isSuccessful) {
                        updateUserWithGroup(id, user, userId,  {responseUser ->
                            continuation.resume(id to responseUser)
                        },  { ex ->
                            continuation.resumeWithException(ex)
                        })
                    } else {
                        it.exception?.let {ex ->
                            continuation.resumeWithException(ex)
                        }

                    }
                }
            }
        })
    }

    fun isGroupCreated(
        userId: String,
        onFetch: (Boolean) -> Unit,
        onError: (DatabaseError) -> Unit
    ) {
        userReference.child(userId).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    if(user.groups.size > 0) {
                        onFetch(true)
                    } else {
                        onFetch(false)
                    }
                } else {
                    onFetch(false)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error)
            }
        })
    }

    suspend fun fetchUserDetails(userId: String) = suspendCoroutine<User?> { continuation ->
        userReference.child(userId).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                continuation.resume(snapshot.getValue(User::class.java))
            }

            override fun onCancelled(error: DatabaseError) {
                continuation.resumeWithException(error.toException())
            }
        })
    }

    suspend fun fetchGroup(userId: String) = suspendCoroutine<String?> { continuation ->

        groupReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
//                val groups = snapshot.value as HashMap<*, *>?
//                var found = false
//                if(groups != null) {
//                    groups.keys.forEach { key ->
//                        val map = groups[key] as HashMap<String, HashMap<*, *>>
//                        val members = map["members"] as HashMap<String, Any>
//                        members.forEach { member ->
//                            if(member.key == userId) {
//                                found = true
//                                continuation.resume(key as String?)
//                            }
//                        }
//                    }
//                    if(!found) {
//                        continuation.resume(null)
//                    }
//                    if(groups.isEmpty()) {
//                        continuation.resume(null)
//                    }
//                } else {
//                    continuation.resume(null)
//                }
                continuation.resume(null)
            }

            override fun onCancelled(error: DatabaseError) {
                continuation.resumeWithException(error.toException())
            }
        })
    }

    fun fetchMember(groupId: String, memberId: String,
                    onFetch: (Member?) -> Unit,
                    onCancel: (DatabaseError) -> Unit) {
        groupReference.child(groupId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val group = snapshot.getValue(Group::class.java)
                if (group == null) {
                    onFetch(null)
                } else {
                    val member = group.members.get(memberId)
                    member?.let {
                        onFetch(member)
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                onCancel(error)
            }
        })
    }

    fun fetchGroupDetails(groupId: String,
                          onFetch: (Group?) -> Unit,
                          onCancel: (DatabaseError) -> Unit
    ) {
        groupReference.child(groupId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val group = snapshot.getValue(Group::class.java)
                onFetch(group)
            }

            override fun onCancelled(error: DatabaseError) {
                onCancel(error)
            }
        })

    }

    suspend fun updateLocation(groupId: String, userId: String, location: Location) = suspendCoroutine<Group?> { continuation ->
        groupReference.child(groupId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val group = snapshot.getValue(Group::class.java)
                val member = group?.members?.get(userId)
                member?.let {
                    member.lat = location.latitude
                    member.long = location.longitude
                    member.lastUpdated = dateTimeUtils.getCurrentTime()
                    groupReference.child(groupId).setValue(group)
                }


                continuation.resume(group)
            }

            override fun onCancelled(error: DatabaseError) {
                continuation.resumeWithException(error.toException())
            }
        })
    }

    fun fetchMembers(groupId: String,
                     onFetch: (ArrayList<Member>) -> Unit,
                     onCancel: (DatabaseError) -> Unit) {
        groupReference.child(groupId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val group = snapshot.getValue(Group::class.java)
                if(group != null) {
                    onFetch(ConversionUtil().getMemberListFromMap(group.members))
                } else {
                    onFetch(arrayListOf())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onCancel(error)
            }
        })
    }


    fun fetchGroupList(userId: String,
                               onSuccess: (ArrayList<Group>) -> Unit,
                               onError: (Exception) -> Unit) {
        val groupList = arrayListOf<Group>()
        userReference.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                onError(error.toException())
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val userObject = snapshot.getValue(User::class.java)
                userObject?.let { user ->
                    if(user.groups.isNotEmpty()) {
                        user.groups.forEach { (s, b) ->
                            groupReference.child(s).addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val group = snapshot.getValue(Group::class.java)
                                    group?.let {
                                        it.id = s
                                        val idx = groupList.indexOfFirst { gid -> gid.id == s }
                                        if(idx != -1) {
                                            groupList.removeAt(idx)
                                            groupList.add(idx, it)
                                        } else {
                                            groupList.add(it)
                                        }
                                        if(user.groups.size == groupList.size) {
                                            onSuccess(groupList)
                                        }
                                    } ?: run {
                                        user.groups.remove(s)
                                        val idx = groupList.indexOfFirst { gId -> gId.id == s }
                                        if(idx != -1) {
                                            groupList.removeAt(idx)
                                        }
                                        if(user.groups.size == groupList.size) {
                                            onSuccess(groupList)
                                        }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    onError(error.toException())
                                }
                            })
                        }
                    } else {
                        onSuccess(arrayListOf<Group>())
                    }

                }

            }
        })
    }

    fun updateUserPhoneToken(userId: String, token: String) {
        userReference.child(userId).child(TOKEN.key).child(PHONE.key).setValue(token)
    }

    fun updateUserWatchToken(userId: String, token: String) {
        userReference.child(userId).child(TOKEN.key).child(WATCH.key).setValue(token)
    }

    suspend fun leaveGroup(requestId: String, groupId: String, userId: String) = suspendCoroutine<Boolean>{ continuation ->
        deleteRequestWith(requestId, userId, {
            removeRequestFromUser(requestId, userId, {
                groupReference.child(groupId).child(MEMBERS.key).child(userId).removeValue().addOnCompleteListener {
                    if(it.isSuccessful) {
                        removeGroupFromUserRef(groupId, userId,  {
                            continuation.resume(true)
                        }, { ex ->
                            continuation.resumeWithException(ex)
                        })
                    } else {
                        it.exception?.let {
                            continuation.resumeWithException(it)
                        }
                    }
                }
            }, {ex ->
                continuation.resumeWithException(ex)
            })

        }, { ex ->
            continuation.resumeWithException(ex)
        })

    }

    fun removeRequestFromUser(
        requestId: String,
        userId: String,
        onComplete: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        userReference.child(userId).child("requests").child(requestId)
                .removeValue().addOnCompleteListener {
            if(it.isSuccessful) {
                onComplete(requestId)
            } else {
                it.exception?.let(onError)
            }
        }
    }

    suspend fun deleteRequestWith(requestId: String, userId: String) = suspendCoroutine<Boolean> { continuation ->
        deleteRequestWith(requestId, userId,  {
            removeRequestFromUser(requestId, userId, {
                continuation.resume(true)
            }, {
                continuation.resumeWithException(it)
            })
        }, {
            continuation.resumeWithException(it)
        })
    }

    private fun deleteRequestWith(
        requestId: String,
        userId: String,
        onComplete: (String) -> Unit,
        onError: (Exception) -> Unit,
    ) {
        requestReference.child(requestId).removeValue().addOnCompleteListener {
            if(it.isSuccessful) {
                onComplete(requestId)
            } else {
                it.exception?.let(onError)
            }
        }
    }

    suspend fun deleteGroup(group: Group) = suspendCoroutine<Boolean> { continuation ->
        group.id?.let { gId ->
            groupReference.child(gId).removeValue().addOnCompleteListener {
                if(it.isSuccessful) {
                    group.members.forEach { s, member ->
                        deleteGroupFromUser(group, s)
                    }
                    continuation.resume(true)
                } else {
                    it.exception?.let { ex ->
                        continuation.resumeWithException(ex)
                    }
                }
            }
        }

    }

    fun deleteGroupFromUser(group: Group, userId: String){
        group.id?.let {
            userReference.child(userId).child(GROUPS.key).child(it).removeValue()
        }
    }

    private fun removeGroupFromUserRef(
        groupId: String,
        userId: String,
        onFinish: () -> Unit,
        onError:(Exception) -> Unit
    ) {
        userReference.child(userId).child("groups").child(groupId).removeValue().addOnCompleteListener {
            if(it.isSuccessful) {
                onFinish()
            } else {
                it.exception?.let(onError)
            }
        }
    }

    suspend fun requestLeaveGroup(
        groupId: String,
        userId: String,
        createdBy: String,
        name: String,
        groupName: String
    ) = suspendCoroutine<Boolean> { continuation ->
        val newRef = requestReference.push()
        newRef.setValue(Request(
            RequestType.LEAVE.key,
            LeaveRequestData(
                group = RequestParam(groupId, groupName),
                from = RequestParam(userId, name),
                to = createdBy
            )
        )).addOnCompleteListener {
            if(it.isSuccessful) {
                updateUserWithRequest(userId, newRef.key.toString(), {
                    continuation.resumeWithException(it)
                }, {
                    continuation.resume(true)
                })
            } else {
                it.exception?.let {ex ->
                    continuation.resumeWithException(ex)
                }
            }
        }
    }

    fun updateUserWithRequest(
        userId: String,
        requestId: String,
        onError: (Exception) -> Unit,
        onComplete: (User) -> Unit
    ) {
        userReference.child(userId).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                onError(error.toException())
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val requestedUser = snapshot.getValue(User::class.java)
                if(requestedUser != null) {
                    requestedUser.requests[requestId] = true
                    userReference.child(userId).setValue(requestedUser).addOnCompleteListener {
                        if(it.isSuccessful) {
                            onComplete(requestedUser)
                        } else {
                            it.exception?.let(onError)
                        }
                    }
                }

            }

        })
    }

    suspend fun checkIfLeaveRequestExists(userId: String, groupId: String) = suspendCoroutine<Boolean>{ continuation ->
        requestReference.orderByChild("data/from/id").equalTo(userId).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                continuation.resumeWithException(error.toException())
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.children.count() > 0) {
                    snapshot.children.forEach {
                        val t = object: GenericTypeIndicator<Request<LeaveRequestData>>(){}
                        val request = it.getValue(t)
                        if(request?.data?.group?.id == groupId) {
                            continuation.resume(true)
                            return@forEach
                        }
                    }
                } else {
                    continuation.resume(false)
                }
            }
        })
    }

    suspend fun getRequests(groupId: String) = suspendCoroutine<List<LeaveRequestData>> { continuation ->
        requestReference.orderByChild("data/group/id").equalTo(groupId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val result = LinkedList<LeaveRequestData>()
                snapshot.children.forEach {
                    val requestData = it.getValue(object: GenericTypeIndicator<Request<LeaveRequestData>>(){})
                    requestData?.data?.let { data ->
                        data.fromId = snapshot.key
                        data.requestId = it.key
                        result.add(data)
                    }
                }
                continuation.resume(result)
            }

            override fun onCancelled(error: DatabaseError) {
                continuation.resumeWithException(error.toException())
            }

        })
    }


    fun saveAddress(memberId: String, groupId: String, address: Address): Deferred<Address> {
        val deferred = CompletableDeferred<Address>()
        groupReference
                .child(groupId)
                .child(MEMBERS.key)
                .child(memberId)
                .child(ADDRESS.key)
                .push().setValue(address).addOnCompleteListener {
                    if(it.isSuccessful) {
                        deferred.complete(address)
                    } else {
                        it.exception?.let { ex -> deferred.completeExceptionally(ex) }
                    }
                }
        return deferred
    }

    fun deleteAddress(groupId: String, memberId: String, addressId: String): Deferred<Boolean> {
        val deferred = CompletableDeferred<Boolean>()
        groupReference.child(groupId).child(MEMBERS.key).child(memberId)
            .child(ADDRESS.key).child(addressId).removeValue().addOnCompleteListener {
                if(it.isSuccessful) {
                    deferred.complete(true)
                } else {
                    it.exception?.let { ex -> deferred.completeExceptionally(ex) }
                }
            }
        return deferred
    }

}