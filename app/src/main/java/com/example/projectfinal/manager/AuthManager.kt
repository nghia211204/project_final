package com.example.projectfinal.manager

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

object AuthManager {

    private val auth: FirebaseAuth = Firebase.auth


    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }


    fun logout() {
        auth.signOut()
    }
}