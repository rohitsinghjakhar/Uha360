package com.dawnbellsuha.uha.registration

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dawnbellsuha.uha.MainActivity
import com.dawnbellsuha.uha.R
import com.dawnbellsuha.uha.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val RC_GOOGLE_SIGN_IN = 9001
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase components
        initializeFirebase()

        // Configure Google Sign-In
        configureGoogleSignIn()

        // Set up click listeners
        setupClickListeners()

        Log.d(TAG, "LoginActivity initialized")
    }

    private fun initializeFirebase() {
        try {
            auth = Firebase.auth
            firestore = FirebaseFirestore.getInstance()

            val settings = com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(com.google.firebase.firestore.FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build()
            firestore.firestoreSettings = settings

            Log.d(TAG, "Firebase initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Firebase initialization failed", e)
            showToast("Firebase setup error: ${e.message}")
        }
    }

    private fun configureGoogleSignIn() {
        try {
            // Double-check the web client ID
            val webClientId = getString(R.string.default_web_client_id)

            // Add validation
            if (webClientId.isEmpty() || webClientId == "YOUR_WEB_CLIENT_ID") {
                throw IllegalStateException("Invalid Web Client ID configured")
            }

            Log.d(TAG, "Using Web Client ID: ${webClientId.take(5)}...")

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(this, gso)
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In configuration failed", e)
            showToast("Google Sign-In setup failed: ${e.message}")
        }
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            loginWithEmail()
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        binding.btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }

        binding.btnPhoneSignIn.setOnClickListener {
            startActivity(Intent(this, PhoneAuthActivity::class.java))
        }
    }

    private fun loginWithEmail() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (!validateInputs(email, password)) return

        showLoading(true)

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                showLoading(false)

                if (task.isSuccessful) {
                    handleLoginSuccess(auth.currentUser)
                } else {
                    handleLoginError(task.exception)
                }
            }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        binding.tilEmail.error = null
        binding.tilPassword.error = null

        return when {
            email.isEmpty() -> {
                binding.tilEmail.error = "Email is required"
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.tilEmail.error = "Please enter a valid email"
                false
            }
            password.isEmpty() -> {
                binding.tilPassword.error = "Password is required"
                false
            }
            password.length < 6 -> {
                binding.tilPassword.error = "Password must be at least 6 characters"
                false
            }
            else -> true
        }
    }

    private fun handleLoginError(exception: Exception?) {
        val errorMessage = when {
            exception?.message?.contains("user-not-found") == true -> "No account found. Please sign up."
            exception?.message?.contains("wrong-password") == true -> "Incorrect password."
            exception?.message?.contains("invalid-email") == true -> "Invalid email format."
            exception?.message?.contains("user-disabled") == true -> "Account disabled."
            exception?.message?.contains("network") == true -> "Network error."
            exception?.message?.contains("too-many-requests") == true -> "Too many attempts. Try later."
            else -> "Login failed: ${exception?.message}"
        }

        showToast(errorMessage)
    }

    private fun signInWithGoogle() {
        try {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)


        } catch (e: Exception) {
            showToast("Failed to start Google Sign-In. Please try again.")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            try {
                val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
                handleGoogleSignInResult(task)
            } catch (e: Exception) {
                Log.e(TAG, "Google sign-in failed", e)
                showToast("Google sign-in failed. Please try again.")
            }
        }
    }

    private fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            account?.let {
                Log.d(TAG, "Google sign-in successful: ${it.email}")
                firebaseAuthWithGoogle(it.idToken!!)
            } ?: run {
                showToast("Google sign-in failed: No account returned")
            }
        } catch (e: ApiException) {
            Log.e(TAG, "Google sign-in failed with code: ${e.statusCode}", e)
            handleGoogleSignInError(e)
        }
    }

    private fun handleGoogleSignInError(e: ApiException) {
        val errorMessage = when (e.statusCode) {
            CommonStatusCodes.API_NOT_CONNECTED -> {
                // This typically means the app is misconfigured
                Log.e(TAG, "API_NOT_CONNECTED - Check Google Sign-In configuration")
                "Google Sign-In configuration error. Please check app setup."
            }
            CommonStatusCodes.DEVELOPER_ERROR -> {
                // This typically means the app is misconfigured
                Log.e(TAG, "DEVELOPER_ERROR - Check OAuth configuration")
                "Developer configuration error. Please check Google Sign-In setup."
            }
            CommonStatusCodes.NETWORK_ERROR -> "Network error. Please check connection."
            CommonStatusCodes.INVALID_ACCOUNT -> "Invalid Google account."
            CommonStatusCodes.SIGN_IN_REQUIRED -> "Please sign in to your Google account."
            CommonStatusCodes.TIMEOUT -> "Connection timeout. Try again."
            CommonStatusCodes.CANCELED -> "Sign in canceled."
            else -> "Google sign-in failed: ${e.message}"
        }

        showToast(errorMessage)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        showLoading(true)

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                showLoading(false)

                if (task.isSuccessful) {
                    handleLoginSuccess(auth.currentUser)

                } else {
                    handleLoginError(task.exception)
                }
            }
            .addOnSuccessListener { navigateToMain() } // Add this line to navigate on success
    }

    private fun handleLoginSuccess(user: com.google.firebase.auth.FirebaseUser?) {
        user?.let {
            Log.d(TAG, "Firebase auth successful for: ${it.email}")
            saveUserToFirestore(it)
        } ?: run {
            Log.e(TAG, "User is null after successful auth")
            showToast("Authentication error. Please try again.")
        }
    }

    private fun saveUserToFirestore(user: com.google.firebase.auth.FirebaseUser) {
        val userData = hashMapOf(
            "uid" to user.uid,
            "fullName" to (user.displayName ?: ""),
            "email" to (user.email ?: ""),
            "provider" to "google",
            "emailVerified" to user.isEmailVerified,
            "phoneVerified" to false,
            "photoUrl" to (user.photoUrl?.toString() ?: ""),
            "lastLogin" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
            "profileComplete" to false,
            "isActive" to true,
            "registrationMethod" to "google_oauth"
        )

        firestore.collection("users").document(user.uid)
            .set(userData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "User data saved to Firestore")
                // Removed navigateToMain() from here as it's now handled in firebaseAuthWithGoogle's success listener
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to save user data", e)
                // Removed navigateToMain() from here as it's now handled in firebaseAuthWithGoogle's success listener
            }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !show
        binding.btnGoogleSignIn.isEnabled = !show
        binding.btnPhoneSignIn.isEnabled = !show
        binding.tvRegister.isEnabled = !show
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onStart() {
        super.onStart()
        // Comment out or remove the automatic navigation in onStart
        // if you want the user to explicitly sign in each time.
        // auth.currentUser?.let {
        //     Log.d(TAG, "User already signed in: ${it.email}")
        //     navigateToMain()
        // }
    }
}