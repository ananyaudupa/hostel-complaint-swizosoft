package com.swizosoft.hostelcomplaints.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.swizosoft.hostelcomplaints.models.User
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun getCurrentUser() = auth.currentUser

    suspend fun getUserDetails(uid: String): User? {
        return try {
            val snapshot = firestore.collection("users").document(uid).get().await()
            snapshot.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveUserDetails(user: User) {
        firestore.collection("users").document(user.uid).set(user).await()
    }

    fun logout() {
        auth.signOut()
    }
}
