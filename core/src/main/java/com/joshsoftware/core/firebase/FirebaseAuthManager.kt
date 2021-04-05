package com.joshsoftware.core.firebase

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.joshsoftware.core.model.User
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FirebaseAuthManager {
    private var mAuth = FirebaseAuth.getInstance()

    suspend fun firebaseAuthWithGoogle(account: GoogleSignInAccount?): Pair<String, User> = suspendCoroutine { continuation ->

        account?.let {
            Timber.d("firebaseAuthWithGoogle: %s", account.id!!)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Timber.d("signInWithCredential:success")
                            task.result?.user?.let {
                                continuation.resume(it.uid to User(it.displayName, it.email))
                            }
                            task.result?.user?.getIdToken(true)?.addOnSuccessListener {
                                it.token?.let { token ->
                                    Timber.i("Token $token")
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