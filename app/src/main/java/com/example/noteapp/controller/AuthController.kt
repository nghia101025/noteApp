package com.example.noteapp.controller

import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.tasks.await

class AuthController(private val database: DatabaseReference) {
    suspend fun loginOrRegister(userId: String): Boolean {
        if (userId.length != 4 || !userId.all { it.isDigit() }) {
            return false
        }

        return try {
            val userRef = database.child("users").child(userId)
            val snapshot = userRef.get().await()

            if (!snapshot.exists()) {
                userRef.setValue(true).await()
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}