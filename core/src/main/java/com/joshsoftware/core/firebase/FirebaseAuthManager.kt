package com.joshsoftware.core.firebase

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.messaging.FirebaseMessaging
import com.joshsoftware.core.model.User
import com.joshsoftware.core.util.StringUtils.capitalizeInitialsName
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FirebaseAuthManager {
    private var mAuth = FirebaseAuth.getInstance()

    suspend fun firebaseAuthWithGoogle(account: GoogleSignInAccount?): Triple<String, User, String> = suspendCoroutine { continuation ->

        account?.let {
            Timber.d("firebaseAuthWithGoogle: %s", account.id!!)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            mAuth.
            signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Timber.d("signInWithCredential:success")
                            FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                                token?.let { fcmToken ->
                                    task.result?.user?.let { fUser ->
                                        val user = User(capitalizeInitialsName(fUser.displayName), fUser.email, profileUrl = fUser.photoUrl?.toString())
                                        continuation.resume(Triple(fUser.uid, user, fcmToken))
                                    }
                                }

                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Timber.e("signInWithCredential:failure, Exception: ${task.exception}")
                            continuation.resumeWithException(task.exception as Exception)
                        }
                    }
        }
    }



}