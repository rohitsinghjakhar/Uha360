package com.dawnbellsuha.uha.models

import java.util.*

data class User(
    val uid: String = "",
    val name: String = "",
    val age: Int = 0,
    val mobile: String = "",
    val state: String = "",
    val classOrExam: String = "",
    val address: String = "",
    val email: String = "",
    val emailVerified: Boolean = false,
    val createdAt: Date = Date(),
    val lastLogin: Date = Date(),
    val provider: String = "email"
) {
    // Add this if you need empty constructor for Firestore
    constructor() : this("", "", 0, "", "", "", "", "", false, Date(), Date(), "email")
}