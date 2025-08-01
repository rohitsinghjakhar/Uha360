package com.uhadawnbells.uha.models

data class UserProfile(
    val uid: String = "",
    val fullName: String = "",
    val age: String = "",
    val mobile: String = "",
    val email: String = "",
    val state: String = "",
    val classExam: String = "",
    val address: String = "",
    val profilePictureUrl: String = "",
    val createdAt: Long? = null,
    val updatedAt: Long? = null
) {
    // No-argument constructor for Firebase
    constructor() : this("", "", "", "", "", "", "", "", "", null, null)

    // Helper functions to get timestamps with defaults
    fun getCreatedAtTime(): Long = createdAt ?: System.currentTimeMillis()
    fun getUpdatedAtTime(): Long = updatedAt ?: System.currentTimeMillis()

    // Helper functions to check if fields are empty/valid
    fun isProfileComplete(): Boolean {
        return uid.isNotEmpty() &&
                fullName.isNotEmpty() &&
                age.isNotEmpty() &&
                mobile.isNotEmpty() &&
                email.isNotEmpty() &&
                state.isNotEmpty() &&
                classExam.isNotEmpty() &&
                address.isNotEmpty()
    }

    fun getDisplayAge(): String = if (age.isNotEmpty()) "$age years" else "N/A"
    fun getDisplayMobile(): String = if (mobile.isNotEmpty()) "+91 $mobile" else "N/A"
    fun getDisplayValue(value: String): String = if (value.isNotEmpty()) value else "N/A"
}