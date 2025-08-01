package com.uhadawnbells.uha.registration

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.uhadawnbells.uha.R
import com.uhadawnbells.uha.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var loadingDialog: Dialog

    companion object {
        private const val TAG = "ForgotPasswordActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        Log.d(TAG, "🔍 ForgotPasswordActivity started")

        // Initialize loading dialog
        initializeLoadingDialog()
        setupClickListeners()
        setupUI()
    }

    private fun initializeLoadingDialog() {
        loadingDialog = Dialog(this).apply {
            setContentView(R.layout.dialog_loading)
            setCancelable(false)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    private fun setupUI() {
        // Focus on email input
        binding.etEmail.requestFocus()

        // Set up initial UI state
        hideAllMessages()

        // Update instructions text
        binding.tvResetInstructions.text = "Enter your registered email address and we'll send you a link to reset your password."
    }

    private fun setupClickListeners() {
        binding.btnResetPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (validateEmail(email)) {
                // Check rate limiting first
                val canRequest = RateLimitHelper.canRequestEmailAuth(this)
                if (!canRequest.first) {
                    showErrorMessage(canRequest.second ?: "Too many requests. Please try again later.")
                    return@setOnClickListener
                }

                // Record the request
                RateLimitHelper.recordEmailAuthRequest(this)

                sendPasswordResetEmail(email)
            }
        }

        binding.tvBackToLogin.setOnClickListener {
            Log.d(TAG, "👈 Back to login clicked")
            finish()
        }

        binding.btnBack.setOnClickListener {
            Log.d(TAG, "👈 Back button clicked")
            finish()
        }
    }

    private fun validateEmail(email: String): Boolean {
        // Clear previous error
        binding.tilEmail.error = null
        hideAllMessages()

        return when {
            email.isEmpty() -> {
                binding.tilEmail.error = "Email cannot be empty"
                binding.etEmail.requestFocus()
                showErrorMessage("Please enter your email address")
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.tilEmail.error = "Please enter a valid email address"
                binding.etEmail.requestFocus()
                showErrorMessage("Please enter a valid email address")
                false
            }
            else -> {
                Log.d(TAG, "✅ Email validation passed")
                true
            }
        }
    }

    private fun sendPasswordResetEmail(email: String) {
        Log.d(TAG, "📧 Sending password reset email to: $email")

        showLoading(true)

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                showLoading(false)

                if (task.isSuccessful) {
                    Log.d(TAG, "✅ Password reset email sent successfully")
                    showSuccessMessage(email)
                } else {
                    Log.e(TAG, "❌ Failed to send password reset email", task.exception)
                    handleResetError(task.exception)
                }
            }
    }

    private fun handleResetError(exception: Exception?) {
        val errorMessage = when {
            exception?.message?.contains("user-not-found") == true -> {
                binding.tilEmail.error = "Email not registered"
                "No account found with this email address. Please check your email or create a new account."
            }
            exception?.message?.contains("invalid-email") == true -> {
                binding.tilEmail.error = "Invalid email format"
                "Invalid email format. Please enter a valid email address."
            }
            exception?.message?.contains("network") == true ->
                "Network error. Please check your internet connection and try again."
            exception?.message?.contains("too-many-requests") == true ->
                "Too many password reset attempts. Please try again later."
            else -> "Failed to send reset email: ${exception?.message}"
        }

        showErrorMessage(errorMessage)
    }

    private fun showSuccessMessage(email: String) {
        hideAllMessages()

        // Update success message with email
        binding.tvSuccessMessage.text = "We've sent a password reset link to:\n\n$email\n\nPlease check your inbox and spam folder. The link will expire in 1 hour."

        // Show success card
        binding.cvSuccessMessage.visibility = View.VISIBLE

        // Disable the reset button temporarily
        binding.btnResetPassword.isEnabled = false
        binding.btnResetPassword.text = "Email Sent ✓"

        // Re-enable button after 30 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            binding.btnResetPassword.isEnabled = true
            binding.btnResetPassword.text = "Send Reset Link"
        }, 30000)

        // Auto-dismiss success message after 10 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            if (binding.cvSuccessMessage.visibility == View.VISIBLE) {
                binding.cvSuccessMessage.visibility = View.GONE
            }
        }, 10000)

        // Clear the email field
        binding.etEmail.text?.clear()

        showToast("Reset email sent successfully!")
    }

    private fun showErrorMessage(message: String) {
        hideAllMessages()

        // Update error message
        binding.tvErrorMessage.text = message

        // Show error card
        binding.cvErrorMessage.visibility = View.VISIBLE

        // Auto-dismiss error message after 5 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            if (binding.cvErrorMessage.visibility == View.VISIBLE) {
                binding.cvErrorMessage.visibility = View.GONE
            }
        }, 5000)

        showToast(message)
    }

    private fun hideAllMessages() {
        binding.cvSuccessMessage.visibility = View.GONE
        binding.cvErrorMessage.visibility = View.GONE
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            try {
                if (!loadingDialog.isShowing) {
                    loadingDialog.show()
                }
                // Update loading message
                loadingDialog.findViewById<android.widget.TextView>(R.id.tvLoadingMessage)?.text = "Sending reset email..."
            } catch (e: Exception) {
                Log.e(TAG, "Error showing loading dialog", e)
            }

            // Show progress bar in layout
            binding.progressBar.visibility = View.VISIBLE
            binding.btnResetPassword.isEnabled = false
            binding.btnResetPassword.text = "Sending..."
        } else {
            try {
                if (loadingDialog.isShowing) {
                    loadingDialog.dismiss()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error hiding loading dialog", e)
            }

            // Hide progress bar
            binding.progressBar.visibility = View.GONE
            binding.btnResetPassword.isEnabled = true
            binding.btnResetPassword.text = "Send Reset Link"
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        Log.d(TAG, "🍞 Toast: $message")
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (::loadingDialog.isInitialized && loadingDialog.isShowing) {
                loadingDialog.dismiss()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error dismissing dialog in onDestroy", e)
        }
    }
}