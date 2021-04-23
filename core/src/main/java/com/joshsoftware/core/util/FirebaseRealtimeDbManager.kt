package com.joshsoftware.core.util

import android.location.Location
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.joshsoftware.core.di.AppType
import com.joshsoftware.core.model.Group
import com.joshsoftware.core.model.Member
import com.joshsoftware.core.model.SosUser
import com.joshsoftware.core.model.User
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FirebaseRealtimeDbManager {
    private val db = FirebaseDatabase.getInstance("https://reached-stage.firebaseio.com/")
    val groupReference = db.getReference(FirebaseDatabaseKey.GROUPS.key)
    val userReference = db.getReference(FirebaseDatabaseKey.USERS.key)
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
                    continuation.resume(requestedUser)
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

    suspend fun toggleSosState(groupId: String, user: User, userId: String) = suspendCoroutine<Boolean?> { continuation ->
        groupReference.child(groupId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val group = snapshot.getValue(Group::class.java)
                val member = group?.members?.get(userId)
                member?.let {
                    member.sosState = !member.sosState
                    groupReference.child(groupId).setValue(group)
                }
                continuation.resume(member?.sosState)
            }

            override fun onCancelled(error: DatabaseError) {
                continuation.resumeWithException(error.toException())
            }
        })
    }

    suspend fun createGroupWith(id: String, userId: String, user: User, groupName: String,  lat: Double, long: Double) = suspendCoroutine<Group> { continuation ->
        val map = hashMapOf<String, Member>()
        map[userId] = Member(name = user.name, profileUrl = user.profileUrl, lat = lat, long = long, lastUpdated = dateTimeUtils.getCurrentTime())
        val group = Group(
            members = map,
            created_by = userId,
            name = groupName
        )
        groupReference.child(id).setValue(group).addOnCompleteListener {
            if(it.isSuccessful) {
                updateUserWithGroup(id, user, userId,  {
                    group.id = id
                    continuation.resume(group)
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
        onComplete: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        user.groups[groupId] = true
        userReference.child(userId).setValue(user).addOnCompleteListener {
            if(it.isSuccessful) {
                onComplete()
            } else {
                it.exception?.let(onError)
            }
        }
    }

    suspend fun joinGroupWith(id: String, userId: String, user: User, lat: Double, long: Double) = suspendCoroutine<String> { continuation ->
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
                        updateUserWithGroup(id, user, userId,  {
                            continuation.resume(id)
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


    suspend fun fetchGroupList(userId: String) = suspendCoroutine<ArrayList<Group>> { continuation ->
        val groupList = arrayListOf<Group>()
        userReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                continuation.resumeWithException(error.toException())
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val userObject = snapshot.getValue(User::class.java)
                userObject?.let { user ->
                    user.groups.forEach { (s, b) ->
                        groupReference.child(s).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val group = snapshot.getValue(Group::class.java)
                                group?.let {
                                    it.id = s
                                    groupList.add(it)
                                    if(user.groups.size == groupList.size) {
                                        continuation.resume(groupList)
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                continuation.resumeWithException(error.toException())
                            }
                        })
                    }
                }

            }
        })
    }

    fun updateUserPhoneToken(userId: String, token: String) {
        userReference.child(userId).child("token").child("phone").setValue(token)
    }

    fun updateUserWatchToken(userId: String, token: String) {
        userReference.child(userId).child("token").child("watch").setValue(token)
    }

}