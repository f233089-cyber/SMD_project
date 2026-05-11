package com.smd.recipehub.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun registerWithEmail(email: String, password: String): FirebaseUser {
        val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        return result.user ?: error("Registration failed")
    }

    suspend fun signInWithEmail(email: String, password: String): FirebaseUser {
        val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
        return result.user ?: error("Login failed")
    }

    suspend fun signInWithGoogle(idToken: String): FirebaseUser {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        return result.user ?: error("Google sign-in failed")
    }

    fun signOut() {
        auth.signOut()
    }
}
