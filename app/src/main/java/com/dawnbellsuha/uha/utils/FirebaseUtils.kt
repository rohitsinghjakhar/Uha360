package com.dawnbellsuha.uha.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

object FirebaseUtils {
    // Authentication
    val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    val currentUserId: String? get() = firebaseAuth.currentUser?.uid

    // Realtime Database
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val userRef: DatabaseReference = database.getReference("users")

    // Storage
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    val profileImagesRef: StorageReference = storage.reference.child("profile_images")
}