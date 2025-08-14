package com.example.projectfinal.manager

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object BookmarkManager {

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private val localBookmarks = mutableSetOf<Int>()

    fun fetchBookmarksForCurrentUser(onComplete: () -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            localBookmarks.clear()
            onComplete()
            return
        }

        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                localBookmarks.clear()
                if (document != null && document.exists()) {
                    val bookmarkedIds = document.get("bookmarkedMovieIds") as? List<Long>
                    bookmarkedIds?.forEach { localBookmarks.add(it.toInt()) }
                    Log.d("BookmarkManager", "Fetched ${localBookmarks.size} bookmarks from Firestore.")
                }
                onComplete()
            }
            .addOnFailureListener { exception ->
                Log.w("BookmarkManager", "Error getting bookmarks.", exception)
                onComplete()
            }
    }

    fun addBookmark(movieId: Int) {
        val user = auth.currentUser ?: return
        localBookmarks.add(movieId)

        val userDocRef = db.collection("users").document(user.uid)
        userDocRef.update("bookmarkedMovieIds", FieldValue.arrayUnion(movieId.toLong()))
            .addOnFailureListener {
                userDocRef.set(mapOf("bookmarkedMovieIds" to listOf(movieId.toLong())))
            }
    }

    fun removeBookmark(movieId: Int) {
        val user = auth.currentUser ?: return
        localBookmarks.remove(movieId)

        val userDocRef = db.collection("users").document(user.uid)
        userDocRef.update("bookmarkedMovieIds", FieldValue.arrayRemove(movieId.toLong()))
    }

    fun isBookmarked(movieId: Int): Boolean {
        return localBookmarks.contains(movieId)
    }

    fun getBookmarks(): Set<Int> {
        return localBookmarks
    }

    fun clearLocalBookmarks() {
        localBookmarks.clear()
        Log.d("BookmarkManager", "Local bookmarks cleared.")
    }
}