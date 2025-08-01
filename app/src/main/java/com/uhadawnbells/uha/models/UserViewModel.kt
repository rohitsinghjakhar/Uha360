package com.uhadawnbells.uha.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

class UserViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    private val firestore = Firebase.firestore

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _loadingState = MutableLiveData<Boolean>(false)
    val loadingState: LiveData<Boolean> = _loadingState

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        val userId = auth.currentUser?.uid ?: run {
            _user.value = null
            return
        }

        _loadingState.value = true
        viewModelScope.launch {
            try {
                val document = firestore.collection("users").document(userId).get().await()
                if (document.exists()) {
                    _user.value = document.toObject(User::class.java)
                } else {
                    _error.value = "User data not found"
                }
            } catch (e: Exception) {
                _error.value = "Failed to load user: ${e.message}"
            } finally {
                _loadingState.value = false
            }
        }
    }

    fun updateUser(updatedUser: User) {
        val userId = auth.currentUser?.uid ?: run {
            _error.value = "Not authenticated"
            return
        }

        _loadingState.value = true
        viewModelScope.launch {
            try {
                // Ensure we don't overwrite critical fields
                val userMap = hashMapOf(
                    "name" to updatedUser.name,
                    "age" to updatedUser.age,
                    "mobile" to updatedUser.mobile,
                    "state" to updatedUser.state,
                    "classOrExam" to updatedUser.classOrExam,
                    "address" to updatedUser.address,
                    "lastLogin" to updatedUser.lastLogin
                )

                firestore.collection("users").document(userId)
                    .update(userMap.toMap())
                    .await()

                // Refresh the user data
                loadCurrentUser()
            } catch (e: Exception) {
                _error.value = "Update failed: ${e.message}"
            } finally {
                _loadingState.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                auth.signOut()
                _user.value = null
            } catch (e: Exception) {
                _error.value = "Logout failed: ${e.message}"
            }
        }
    }

    fun refreshUserData() {
        loadCurrentUser()
    }

    companion object {
        // Helper extension function to convert timestamp to Date
        private fun Any?.toDate(): Date? {
            return when (this) {
                is com.google.firebase.Timestamp -> this.toDate()
                else -> null
            }
        }
    }
}