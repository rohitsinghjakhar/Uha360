package com.dawnbellsuha.uha.registration

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dawnbellsuha.uha.MainActivity
import com.dawnbellsuha.uha.databinding.ActivityRegisterBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    companion object {
        private const val TAG = "RegisterActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeFirebase()
        setupClickListeners()
        FirebaseApp.initializeApp(this)

    }

    private fun initializeFirebase() {
        try {
            auth = FirebaseAuth.getInstance()
            firestore = FirebaseFirestore.getInstance()

            // Configure Firestore settings for better reliability
            val settings = com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(com.google.firebase.firestore.FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build()
            firestore.firestoreSettings = settings

        } catch (e: Exception) {
            Toast.makeText(this, "Setup error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            registerUser()
        }

        binding.tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun registerUser() {
        val fullName = binding.etFullName.text.toString().trim()
        val age = binding.etAge.text.toString().trim()
        val mobile = binding.etMobile.text.toString().trim()
        val state = binding.etState.text.toString().trim()
        val classExam = binding.etClassExam.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (!validateAllInputs(fullName, age, mobile, state, classExam, address, email, password)) {
            return
        }

        showLoading(true)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        // Update user profile
                        updateUserProfile(user, fullName) { profileSuccess ->
                            if (profileSuccess) {
                                // Save to Firestore
                                saveUserToFirestore(user, fullName, age, mobile, state, classExam, address, email)
                            } else {
                                showLoading(false)
                                Toast.makeText(
                                    this,
                                    "Profile update failed. Contact support.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    } else {
                        showLoading(false)
                        Toast.makeText(this, "Registration error: User is null", Toast.LENGTH_LONG).show()
                    }
                } else {
                    showLoading(false)
                    handleRegistrationError(task.exception)
                }
            }
    }

    private fun updateUserProfile(user: FirebaseUser, fullName: String, callback: (Boolean) -> Unit) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(fullName)
            .build()

        user.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "User profile updated.")
                    callback(true)
                } else {
                    Log.e(TAG, "Failed to update user profile", task.exception)
                    callback(false)
                }
            }
    }

    private fun saveUserToFirestore(
        user: FirebaseUser,
        fullName: String,
        age: String,
        mobile: String,
        state: String,
        classExam: String,
        address: String,
        email: String
    ) {
        val userData = hashMapOf(
            "uid" to user.uid,
            "fullName" to fullName,
            "email" to email,
            "age" to (age.toIntOrNull() ?: 0),
            "mobile" to mobile,
            "state" to state,
            "classExam" to classExam,
            "address" to address,
            "provider" to "email",
            "emailVerified" to user.isEmailVerified,
            "phoneVerified" to false,
            "profileComplete" to true,
            "isActive" to true,
            "registrationMethod" to "email_password",
            "createdAt" to FieldValue.serverTimestamp(),
            "lastLogin" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        firestore.collection("users").document(user.uid)
            .set(userData)
            .addOnSuccessListener {
                Log.d(TAG, "User data saved successfully")
                showLoading(false)
                showSuccessAndNavigate()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to save user data", e)
                showLoading(false)
                Toast.makeText(
                    this,
                    "Registration successful but failed to save profile. Contact support.",
                    Toast.LENGTH_LONG
                ).show()
                // Still navigate to main since auth succeeded
                navigateToMain()
            }
    }

    private fun validateAllInputs(
        fullName: String, age: String, mobile: String,
        state: String, classExam: String, address: String,
        email: String, password: String
    ): Boolean {

        // Clear all errors
        clearAllErrors()

        var isValid = true

        // Full name validation
        if (fullName.isEmpty()) {
            binding.tilFullName.error = "Full name is required"
            if (isValid) binding.etFullName.requestFocus()
            isValid = false
        } else if (fullName.length < 2) {
            binding.tilFullName.error = "Name must be at least 2 characters"
            if (isValid) binding.etFullName.requestFocus()
            isValid = false
        } else if (!fullName.matches(Regex("^[a-zA-Z\\s]+$"))) {
            binding.tilFullName.error = "Name should contain only letters"
            if (isValid) binding.etFullName.requestFocus()
            isValid = false
        }

        // Age validation
        if (age.isEmpty()) {
            binding.tilAge.error = "Age is required"
            if (isValid) binding.etAge.requestFocus()
            isValid = false
        } else {
            val ageInt = age.toIntOrNull()
            if (ageInt == null || ageInt < 5 || ageInt > 100) {
                binding.tilAge.error = "Enter valid age (5-100)"
                if (isValid) binding.etAge.requestFocus()
                isValid = false
            }
        }

        // Mobile validation
        if (mobile.isEmpty()) {
            binding.tilMobile.error = "Mobile number is required"
            if (isValid) binding.etMobile.requestFocus()
            isValid = false
        } else if (!mobile.matches(Regex("^[6-9][0-9]{9}$"))) {
            binding.tilMobile.error = "Enter valid 10-digit Indian mobile number"
            if (isValid) binding.etMobile.requestFocus()
            isValid = false
        }

        // State validation
        if (state.isEmpty()) {
            binding.tilState.error = "State is required"
            if (isValid) binding.etState.requestFocus()
            isValid = false
        } else if (state.length < 2) {
            binding.tilState.error = "Enter valid state name"
            if (isValid) binding.etState.requestFocus()
            isValid = false
        }

        // Class/Exam validation
        if (classExam.isEmpty()) {
            binding.tilClassExam.error = "Class/Exam is required"
            if (isValid) binding.etClassExam.requestFocus()
            isValid = false
        }

        // Address validation
        if (address.isEmpty()) {
            binding.tilAddress.error = "Address is required"
            if (isValid) binding.etAddress.requestFocus()
            isValid = false
        } else if (address.length < 4) {
            binding.tilAddress.error = "Please enter complete address (minimum 4 characters)"
            if (isValid) binding.etAddress.requestFocus()
            isValid = false
        }

        // Email validation
        if (email.isEmpty()) {
            binding.tilEmail.error = "Email is required"
            if (isValid) binding.etEmail.requestFocus()
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Enter valid email address"
            if (isValid) binding.etEmail.requestFocus()
            isValid = false
        }

        // Password validation
        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            if (isValid) binding.etPassword.requestFocus()
            isValid = false
        } else if (password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            if (isValid) binding.etPassword.requestFocus()
            isValid = false
        } else if (!password.matches(Regex(".*[A-Za-z].*")) || !password.matches(Regex(".*[0-9].*"))) {
            binding.tilPassword.error = "Password must contain both letters and numbers"
            if (isValid) binding.etPassword.requestFocus()
            isValid = false
        }

        return isValid
    }

    private fun clearAllErrors() {
        binding.tilFullName.error = null
        binding.tilAge.error = null
        binding.tilMobile.error = null
        binding.tilState.error = null
        binding.tilClassExam.error = null
        binding.tilAddress.error = null
        binding.tilEmail.error = null
        binding.tilPassword.error = null
    }

    private fun handleRegistrationError(exception: Exception?) {
        val errorMessage = when {
            exception?.message?.contains("email-already-in-use") == true ->
                "Email already registered. Please use different email or sign in."
            exception?.message?.contains("weak-password") == true ->
                "Password is too weak. Use a stronger password with letters and numbers."
            exception?.message?.contains("invalid-email") == true ->
                "Invalid email format."
            exception?.message?.contains("network") == true ->
                "Network error. Check your connection."
            exception?.message?.contains("too-many-requests") == true ->
                "Too many registration attempts. Please try again later."
            else -> "Registration failed: ${exception?.message}"
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }





    private fun showSuccessAndNavigate() {
        Toast.makeText(this, "Registration successful! Welcome to UHA 🎉", Toast.LENGTH_LONG).show()
        navigateToMain()
    }

    private fun navigateToMain() {
        Log.d(TAG, "🚀 Navigating to MainActivity")
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }


    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !show
        binding.tvLogin.isEnabled = !show
        binding.btnRegister.text = if (show) "Creating Account..." else "Create Account"
    }
}