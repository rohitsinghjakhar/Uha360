package com.dawnbellsuha.uha.registration

import android.app.Activity
import android.util.Log
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import java.util.concurrent.TimeUnit

object AuthManager {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private const val TAG = "AuthManager"

    init {
        // Configure Firestore settings
        try {
            val settings = com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(com.google.firebase.firestore.FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build()
            firestore.firestoreSettings = settings
            Log.d(TAG, "✅ AuthManager initialized with Firestore settings")
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Firestore settings already configured", e)
        }
    }

    // Email/Password Authentication
    fun registerWithEmail(
        email: String,
        password: String,
        userData: Map<String, Any>,
        callback: (Boolean, String?) -> Unit
    ) {
        Log.d(TAG, "📧 Registering user with email: $email")

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        Log.d(TAG, "✅ Email registration successful")

                        // Update user profile if display name is provided
                        val displayName = userData["fullName"] as? String
                        if (!displayName.isNullOrEmpty()) {
                            updateUserProfile(user, displayName) { profileSuccess ->
                                saveUserDataToFirestore(user.uid, userData) { firestoreSuccess, error ->
                                    if (firestoreSuccess) {
                                        sendEmailVerification(user) { _, _ ->
                                            callback(true, null)
                                        }
                                    } else {
                                        callback(true, "Registration successful but profile save failed: $error")
                                    }
                                }
                            }
                        } else {
                            saveUserDataToFirestore(user.uid, userData) { firestoreSuccess, error ->
                                if (firestoreSuccess) {
                                    sendEmailVerification(user) { _, _ ->
                                        callback(true, null)
                                    }
                                } else {
                                    callback(true, "Registration successful but profile save failed: $error")
                                }
                            }
                        }
                    } else {
                        callback(false, "User creation failed - user is null")
                    }
                } else {
                    Log.e(TAG, "❌ Email registration failed", task.exception)
                    callback(false, task.exception?.message)
                }
            }
    }

    fun loginWithEmail(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        Log.d(TAG, "🔐 Logging in user with email: $email")

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        Log.d(TAG, "✅ Email login successful")
                        updateLastLogin(user.uid)
                        callback(true, null)
                    } else {
                        callback(false, "Login successful but user is null")
                    }
                } else {
                    Log.e(TAG, "❌ Email login failed", task.exception)
                    callback(false, task.exception?.message)
                }
            }
    }

    fun resetPassword(email: String, callback: (Boolean, String?) -> Unit) {
        Log.d(TAG, "📧 Sending password reset email to: $email")

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "✅ Password reset email sent")
                    callback(true, null)
                } else {
                    Log.e(TAG, "❌ Failed to send password reset email", task.exception)
                    callback(false, task.exception?.message)
                }
            }
    }

    // Phone Authentication
    fun sendPhoneVerificationCode(
        activity: Activity,
        phoneNumber: String,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        Log.d(TAG, "📱 Sending phone verification code to: $phoneNumber")

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        try {
            PhoneAuthProvider.verifyPhoneNumber(options)
            Log.d(TAG, "📤 Phone verification request sent")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to send phone verification", e)
        }
    }

    fun verifyPhoneCode(
        verificationId: String,
        code: String,
        phoneNumber: String,
        callback: (Boolean, String?) -> Unit
    ) {
        Log.d(TAG, "🔐 Verifying phone code")

        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        Log.d(TAG, "✅ Phone verification successful")

                        val userData = mapOf(
                            "uid" to user.uid,
                            "phoneNumber" to phoneNumber,
                            "provider" to "phone",
                            "emailVerified" to false,
                            "phoneVerified" to true,
                            "profileComplete" to false,
                            "isActive" to true,
                            "registrationMethod" to "phone_otp",
                            "createdAt" to FieldValue.serverTimestamp(),
                            "lastLogin" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )

                        saveUserDataToFirestore(user.uid, userData) { success, error ->
                            callback(success, error)
                        }
                    } else {
                        callback(false, "Phone verification successful but user is null")
                    }
                } else {
                    Log.e(TAG, "❌ Phone verification failed", task.exception)
                    callback(false, task.exception?.message)
                }
            }
    }

    // Google Sign-In
    fun firebaseAuthWithGoogle(idToken: String, callback: (Boolean, String?) -> Unit) {
        Log.d(TAG, "🔐 Authenticating with Google")

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        Log.d(TAG, "✅ Google authentication successful")

                        val userData = mapOf(
                            "uid" to user.uid,
                            "fullName" to (user.displayName ?: ""),
                            "email" to (user.email ?: ""),
                            "provider" to "google",
                            "emailVerified" to user.isEmailVerified,
                            "phoneVerified" to false,
                            "photoUrl" to (user.photoUrl?.toString() ?: ""),
                            "profileComplete" to false,
                            "isActive" to true,
                            "registrationMethod" to "google_oauth",
                            "createdAt" to FieldValue.serverTimestamp(),
                            "lastLogin" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )

                        saveUserDataToFirestore(user.uid, userData) { success, error ->
                            callback(success, error)
                        }
                    } else {
                        callback(false, "Google authentication successful but user is null")
                    }
                } else {
                    Log.e(TAG, "❌ Google authentication failed", task.exception)
                    callback(false, task.exception?.message)
                }
            }
    }

    // Helper Functions
    private fun saveUserDataToFirestore(
        userId: String,
        userData: Map<String, Any>,
        callback: (Boolean, String?) -> Unit
    ) {
        Log.d(TAG, "💾 Saving user data to Firestore for user: $userId")

        firestore.collection("users").document(userId)
            .set(userData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "✅ User data saved to Firestore successfully")
                callback(true, null)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Failed to save user data to Firestore", e)
                callback(false, e.message)
            }
    }

    private fun updateUserProfile(
        user: FirebaseUser,
        displayName: String,
        callback: (Boolean) -> Unit
    ) {
        Log.d(TAG, "👤 Updating user profile with display name: $displayName")

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build()

        user.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "✅ User profile updated successfully")
                    callback(true)
                } else {
                    Log.w(TAG, "⚠️ Profile update failed", task.exception)
                    callback(false)
                }
            }
    }

    private fun sendEmailVerification(
        user: FirebaseUser,
        callback: (Boolean, String?) -> Unit
    ) {
        Log.d(TAG, "📧 Sending email verification to: ${user.email}")

        user.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "✅ Email verification sent successfully")
                    callback(true, null)
                } else {
                    Log.w(TAG, "⚠️ Failed to send email verification", task.exception)
                    callback(false, task.exception?.message)
                }
            }
    }

    private fun updateLastLogin(userId: String) {
        Log.d(TAG, "⏰ Updating last login for user: $userId")

        val updates = mapOf(
            "lastLogin" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        firestore.collection("users").document(userId)
            .update(updates)
            .addOnSuccessListener {
                Log.d(TAG, "✅ Last login updated successfully")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "⚠️ Failed to update last login", e)
            }
    }

    // Public utility functions
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    fun signOut(callback: (() -> Unit)? = null) {
        Log.d(TAG, "🚪 Signing out user")
        auth.signOut()
        callback?.invoke()
    }

    fun deleteAccount(callback: (Boolean, String?) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            Log.d(TAG, "🗑️ Deleting user account: ${user.uid}")

            // First delete from Firestore
            firestore.collection("users").document(user.uid)
                .delete()
                .addOnSuccessListener {
                    Log.d(TAG, "✅ User data deleted from Firestore")

                    // Then delete from Firebase Auth
                    user.delete()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d(TAG, "✅ User account deleted successfully")
                                callback(true, null)
                            } else {
                                Log.e(TAG, "❌ Failed to delete user account", task.exception)
                                callback(false, task.exception?.message)
                            }
                        }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "❌ Failed to delete user data from Firestore", e)
                    callback(false, e.message)
                }
        } else {
            callback(false, "No user currently signed in")
        }
    }

    fun updateUserData(
        userId: String,
        updates: Map<String, Any>,
        callback: (Boolean, String?) -> Unit
    ) {
        Log.d(TAG, "📝 Updating user data for: $userId")

        val updatesWithTimestamp = updates.toMutableMap()
        updatesWithTimestamp["updatedAt"] = FieldValue.serverTimestamp()

        firestore.collection("users").document(userId)
            .update(updatesWithTimestamp)
            .addOnSuccessListener {
                Log.d(TAG, "✅ User data updated successfully")
                callback(true, null)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Failed to update user data", e)
                callback(false, e.message)
            }
    }

    fun getUserData(userId: String, callback: (Map<String, Any>?, String?) -> Unit) {
        Log.d(TAG, "📖 Fetching user data for: $userId")

        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    Log.d(TAG, "✅ User data fetched successfully")
                    callback(document.data, null)
                } else {
                    Log.w(TAG, "⚠️ User document does not exist")
                    callback(null, "User data not found")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Failed to fetch user data", e)
                callback(null, e.message)
            }
    }

    fun isUserSignedIn(): Boolean {
        return auth.currentUser != null
    }

    fun isEmailVerified(): Boolean {
        return auth.currentUser?.isEmailVerified ?: false
    }

    fun reloadUser(callback: (Boolean) -> Unit) {
        val user = auth.currentUser
        user?.reload()?.addOnCompleteListener { task ->
            callback(task.isSuccessful)
        } ?: callback(false)
    }
}