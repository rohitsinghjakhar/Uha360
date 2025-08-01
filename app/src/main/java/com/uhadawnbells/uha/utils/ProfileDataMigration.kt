package com.uhadawnbells.uha.utils

import com.uhadawnbells.uha.models.UserProfile
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

object ProfileDataMigration {

    /**
     * Safely convert DocumentSnapshot to UserProfile handling missing or null timestamp fields
     */
    fun documentToUserProfile(document: DocumentSnapshot): UserProfile? {
        return try {
            val data = document.data ?: return null
            val currentTime = System.currentTimeMillis()

            UserProfile(
                uid = data["uid"] as? String ?: "",
                fullName = data["fullName"] as? String ?: "",
                age = data["age"] as? String ?: "",
                mobile = data["mobile"] as? String ?: "",
                email = data["email"] as? String ?: "",
                state = data["state"] as? String ?: "",
                classExam = data["classExam"] as? String ?: "",
                address = data["address"] as? String ?: "",
                profilePictureUrl = data["profilePictureUrl"] as? String ?: "",
                createdAt = (data["createdAt"] as? Long) ?: currentTime,
                updatedAt = (data["updatedAt"] as? Long) ?: currentTime
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Update existing documents that might have missing timestamp fields
     */
    fun migrateUserProfile(userId: String, firestore: FirebaseFirestore) {
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val data = document.data
                    if (data != null) {
                        val currentTime = System.currentTimeMillis()
                        val updates = mutableMapOf<String, Any>()

                        // Add missing createdAt
                        if (!data.containsKey("createdAt") || data["createdAt"] == null) {
                            updates["createdAt"] = currentTime
                        }

                        // Add missing updatedAt
                        if (!data.containsKey("updatedAt") || data["updatedAt"] == null) {
                            updates["updatedAt"] = currentTime
                        }

                        // Update document if there are missing fields
                        if (updates.isNotEmpty()) {
                            firestore.collection("users").document(userId)
                                .update(updates)
                        }
                    }
                }
            }
    }
}