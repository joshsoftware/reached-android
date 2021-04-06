package com.joshsoftware.core.util

import android.location.Location
import com.google.firebase.database.*
import com.google.gson.internal.ObjectConstructor
import com.joshsoftware.core.model.Group
import com.joshsoftware.core.model.Member
import com.joshsoftware.core.model.SosUser
import com.joshsoftware.core.model.User
import java.util.ArrayList
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.system.measureTimeMillis

class FirebaseRealtimeDbManager {
    private val db = FirebaseDatabase.getInstance()
    val groupReference = db.getReference(FirebaseDatabaseKey.GROUPS.key)
    val userReference = db.getReference(FirebaseDatabaseKey.USERS.key)
    val sosReference = db.getReference(FirebaseDatabaseKey.SOS.key)

    suspend fun addUserWith(id: String, user: User) = suspendCoroutine<User> { continuation ->
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

    suspend fun sendSOS(groupId: String, user: User, userId: String) = suspendCoroutine<String> { continuation ->
        sosReference.child(groupId).setValue(SosUser(true, userId, user.name)).addOnCompleteListener {
            if(it.isSuccessful) {
                continuation.resume(groupId)
            } else {
                it.exception?.let { ex ->
                    continuation.resumeWithException(ex)
                }
            }
        }
    }


    suspend fun deleteSos(groupId: String) = suspendCoroutine<String> { continuation ->
        sosReference.child(groupId).removeValue()
    }

    suspend fun createGroupWith(id: String, userId: String, user: User) = suspendCoroutine<String> { continuation ->
        groupReference.child(id).setValue(Group(
            members = arrayListOf(Member(id = userId, name = user.name)),
            created_by = userId
        )).addOnCompleteListener {
            if(it.isSuccessful) {
                continuation.resume(id)
            } else {
                it.exception?.let { ex ->
                    continuation.resumeWithException(ex)
                }
            }
        }
    }


    suspend fun joinGroupWith(id: String, userId: String, user: User) = suspendCoroutine<String> { continuation ->
        groupReference.child(id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                continuation.resumeWithException(error.toException())
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val group = snapshot.getValue(Group::class.java)
                group?.let {
                    it.members.add(Member(userId, user.name))
                }
                groupReference.child(id).setValue(group).addOnCompleteListener {
                    if(it.isSuccessful) {
                        continuation.resume(id)
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
        onFetch: (String?) -> Unit,
        onError: (DatabaseError) -> Unit
    ) {
        groupReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val groups = snapshot.value as HashMap<*, *>?
                var found = false
                groups?.keys?.forEach { key ->
                    val map = groups[key] as HashMap<String, ArrayList<Member>>
                    val members = map["members"] as ArrayList<HashMap<*, *>>
                    members.forEach { member ->
                        if(member["id"] == userId) {
                            found = true
                            onFetch(key as String?)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error)
            }
        })
    }

    suspend fun fetchGroup(userId: String) = suspendCoroutine<String?> { continuation ->

        groupReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val groups = snapshot.value as HashMap<*, *>?
                var found = false
                if(groups != null) {
                    groups.keys.forEach { key ->
                        val map = groups[key] as HashMap<String, HashMap<*, *>>
                        val members = map["members"] as HashMap<String, Any>
                        members.forEach { member ->
                            if(member.key == userId) {
                                found = true
                                continuation.resume(key as String?)
                            }
                        }
                    }
                    if(!found) {
                        continuation.resume(null)
                    }
                    if(groups.isEmpty()) {
                        continuation.resume(null)
                    }
                } else {
                    continuation.resume(null)
                }

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
                    val index = group?.members?.indexOfFirst { it.id == memberId }
                    index?.let {
                        if (index != -1) {
                            onFetch(group.members[index])
                        }
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
                val index = group?.members?.indexOfFirst { it.id == userId }
                index?.let {
                    if (index != -1) {
                        group.members[index].lat = location.latitude
                        group.members[index].long = location.longitude
                        groupReference.child(groupId).setValue(group)
                    }
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
                    onFetch(group.members)
                } else {
                    onFetch(arrayListOf())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onCancel(error)
            }
        })
    }

    fun observeSos(groupId: String,
                             onFetch: (SosUser?) -> Unit,
                             onCancel: (DatabaseError) -> Unit) {
        sosReference.child(groupId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val sosUser = snapshot.getValue(SosUser::class.java)
                onFetch(sosUser)
            }

            override fun onCancelled(error: DatabaseError) {
                onCancel(error)
            }
        })
    }
}