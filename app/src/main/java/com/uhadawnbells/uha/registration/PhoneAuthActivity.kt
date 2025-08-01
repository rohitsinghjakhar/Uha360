package com.uhadawnbells.uha.registration

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.uhadawnbells.uha.MainActivity
import com.uhadawnbells.uha.R
import com.uhadawnbells.uha.databinding.ActivityPhoneAuthBinding
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import java.util.concurrent.TimeUnit

class PhoneAuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPhoneAuthBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var storedVerificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private lateinit var loadingDialog: Dialog
    private var countDownTimer: CountDownTimer? = null
    private var isOtpSent = false

    companion object {
        private const val TAG = "PhoneAuthActivity"
        private const val RESEND_TIMEOUT = 60L // 60 seconds
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeFirebase()
        initializeLoadingDialog()
        setupClickListeners()
        setupUI()

        Log.d(TAG, "🔍 PhoneAuthActivity started")
    }

    private fun initializeFirebase() {
        try {
            auth = FirebaseAuth.getInstance()
            firestore = FirebaseFirestore.getInstance()

            // Configure Firestore settings
            val settings = com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(com.google.firebase.firestore.FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build()
            firestore.firestoreSettings = settings

            Log.d(TAG, "✅ Firebase initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Firebase initialization failed", e)
            Toast.makeText(this, "Firebase setup error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun initializeLoadingDialog() {
        loadingDialog = Dialog(this).apply {
            setContentView(R.layout.dialog_loading)
            setCancelable(false)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    private fun setupUI() {
        // Set initial UI state
        binding.tilOtp.visibility = View.GONE
        binding.btnVerifyOtp.visibility = View.GONE
        binding.btnResendOtp.visibility = View.GONE
        binding.tvTimer.visibility = View.GONE
        binding.tvOtpSentMessage.visibility = View.GONE

        // Focus on phone number input
        binding.etPhoneNumber.requestFocus()
    }

    private fun setupClickListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            Log.d(TAG, "👈 Back button clicked")
            finish()
        }

        // Send OTP
        binding.btnSendOtp.setOnClickListener {
            val phoneNumber = binding.etPhoneNumber.text.toString().trim()
            if (validatePhoneNumber(phoneNumber)) {
                val fullPhoneNumber = "+91$phoneNumber"
                Log.d(TAG, "📱 Sending OTP to: $fullPhoneNumber")
                startPhoneNumberVerification(fullPhoneNumber)
            }
        }

        // Verify OTP
        binding.btnVerifyOtp.setOnClickListener {
            val otp = binding.etOtp.text.toString().trim()
            if (validateOTP(otp) && storedVerificationId != null) {
                Log.d(TAG, "🔐 Verifying OTP: $otp")
                verifyPhoneNumberWithCode(storedVerificationId!!, otp)
            }
        }

        // Resend OTP
        binding.btnResendOtp.setOnClickListener {
            if (resendToken != null) {
                val phoneNumber = binding.etPhoneNumber.text.toString().trim()
                if (validatePhoneNumber(phoneNumber)) {
                    val fullPhoneNumber = "+91$phoneNumber"
                    Log.d(TAG, "🔄 Resending OTP to: $fullPhoneNumber")
                    resendVerificationCode(fullPhoneNumber, resendToken!!)
                }
            } else {
                showToast("Cannot resend OTP at this time. Please try again later.")
            }
        }
    }

    private fun validatePhoneNumber(phoneNumber: String): Boolean {
        // Clear previous error
        binding.tilPhoneNumber.error = null

        return when {
            phoneNumber.isEmpty() -> {
                binding.tilPhoneNumber.error = "Phone number is required"
                binding.etPhoneNumber.requestFocus()
                showToast("Please enter your phone number")
                false
            }

            phoneNumber.length != 10 -> {
                binding.tilPhoneNumber.error = "Enter valid 10-digit number"
                binding.etPhoneNumber.requestFocus()
                showToast("Phone number must be exactly 10 digits")
                false
            }

            !phoneNumber.matches(Regex("^[6-9][0-9]{9}$")) -> {
                binding.tilPhoneNumber.error = "Enter valid Indian mobile number"
                binding.etPhoneNumber.requestFocus()
                showToast("Please enter a valid Indian mobile number starting with 6-9")
                false
            }

            else -> {
                Log.d(TAG, "✅ Phone number validation passed")
                true
            }
        }
    }

    private fun validateOTP(otp: String): Boolean {
        // Clear previous error
        binding.tilOtp.error = null

        return when {
            otp.isEmpty() -> {
                binding.tilOtp.error = "OTP is required"
                binding.etOtp.requestFocus()
                showToast("Please enter the OTP")
                false
            }

            otp.length != 6 -> {
                binding.tilOtp.error = "Enter complete 6-digit OTP"
                binding.etOtp.requestFocus()
                showToast("OTP must be exactly 6 digits")
                false
            }

            !otp.matches(Regex("^[0-9]{6}$")) -> {
                binding.tilOtp.error = "OTP should contain only numbers"
                binding.etOtp.requestFocus()
                showToast("OTP should contain only numbers")
                false
            }

            else -> {
                Log.d(TAG, "✅ OTP validation passed")
                true
            }
        }
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        Log.d(TAG, "🚀 Starting phone verification for: $phoneNumber")
        showLoading(true, "Sending OTP...")

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(RESEND_TIMEOUT, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(phoneAuthCallbacks)
            .build()

        try {
            PhoneAuthProvider.verifyPhoneNumber(options)
            Log.d(TAG, "📤 Phone verification request sent")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to start phone verification", e)
            hideLoading()
            showToast("Failed to send OTP. Please try again.")
        }
    }

    private fun resendVerificationCode(
        phoneNumber: String,
        token: PhoneAuthProvider.ForceResendingToken
    ) {
        Log.d(TAG, "🔄 Resending verification code for: $phoneNumber")
        showLoading(true, "Resending OTP...")

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(RESEND_TIMEOUT, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(phoneAuthCallbacks)
            .setForceResendingToken(token)
            .build()

        try {
            PhoneAuthProvider.verifyPhoneNumber(options)
            Log.d(TAG, "📤 Resend verification request sent")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to resend verification code", e)
            hideLoading()
            showToast("Failed to resend OTP. Please try again.")
        }
    }

    private fun verifyPhoneNumberWithCode(verificationId: String, code: String) {
        Log.d(TAG, "🔐 Verifying phone number with code")
        showLoading(true, "Verifying OTP...")

        try {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            signInWithPhoneAuthCredential(credential)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to create credential", e)
            hideLoading()
            showToast("Invalid OTP format")
        }
    }

    private val phoneAuthCallbacks =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "✅ Verification completed automatically")
                hideLoading()

                // Auto-fill OTP if possible
                val smsCode = credential.smsCode
                if (!smsCode.isNullOrEmpty()) {
                    binding.etOtp.setText(smsCode)
                    Log.d(TAG, "📱 Auto-filled OTP: $smsCode")
                }

                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.e(TAG, "❌ Verification failed", e)
                hideLoading()

                val errorMessage = when (e) {
                    is FirebaseAuthInvalidCredentialsException -> {
                        binding.tilPhoneNumber.error = "Invalid phone number format"
                        "Invalid phone number format. Please check and try again."
                    }

                    is FirebaseTooManyRequestsException -> {
                        "Too many requests from this device. Please try again after some time or use a different device."
                    }

                    else -> {
                        "Verification failed: ${e.localizedMessage ?: e.message}"
                    }
                }

                showToast(errorMessage)
                Log.e(TAG, "Detailed error: ${e.message}")

                // Reset UI if verification failed
                if (isOtpSent) {
                    resetToPhoneInput()
                }
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d(TAG, "✅ OTP sent successfully")
                Log.d(TAG, "📋 Verification ID: ${verificationId.take(10)}...")

                hideLoading()

                storedVerificationId = verificationId
                resendToken = token
                isOtpSent = true

                showOtpVerificationLayout()
                startResendTimer()

                val phoneNumber = binding.etPhoneNumber.text.toString().trim()
                showToast("OTP sent to +91$phoneNumber")
                Log.d(TAG, "✅ UI updated for OTP verification")
            }
        }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        Log.d(TAG, "🔐 Signing in with phone credential")

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                hideLoading()

                if (task.isSuccessful) {
                    Log.d(TAG, "✅ Phone authentication successful")
                    val user = auth.currentUser

                    if (user != null) {
                        Log.d(TAG, "👤 User authenticated: ${user.uid}")
                        savePhoneUserToFirestore(user)
                    } else {
                        Log.e(TAG, "❌ User is null after successful phone auth")
                        showToast("Authentication error. Please try again.")
                    }
                } else {
                    Log.e(TAG, "❌ Phone authentication failed", task.exception)

                    val errorMessage = when (task.exception) {
                        is FirebaseAuthInvalidCredentialsException -> {
                            binding.tilOtp.error = "Invalid verification code"
                            "Invalid OTP. Please check and try again."
                        }

                        is FirebaseTooManyRequestsException -> {
                            "Too many failed attempts. Please try again later."
                        }

                        else -> {
                            "Authentication failed: ${task.exception?.localizedMessage ?: task.exception?.message}"
                        }
                    }

                    showToast(errorMessage)
                }
            }
    }

    private fun savePhoneUserToFirestore(user: com.google.firebase.auth.FirebaseUser) {
        Log.d(TAG, "💾 Saving phone user data to Firestore")

        val userData = hashMapOf(
            "uid" to user.uid,
            "phoneNumber" to (user.phoneNumber ?: ""),
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

        firestore.collection("users").document(user.uid)
            .set(userData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "✅ Phone user data saved to Firestore successfully")
                navigateToMain()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Failed to save phone user data to Firestore", e)
                // Still navigate to main even if Firestore save fails
                showToast("Authentication successful!")
                navigateToMain()
            }
    }

    private fun navigateToMain() {
        Log.d(TAG, "🚀 Navigating to MainActivity")

        showToast("Phone verification successful! Welcome to UHA! 🎉")

        // Add a small delay to show the success message
        binding.root.postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }, 1000)
    }

    private fun showOtpVerificationLayout() {
        Log.d(TAG, "🔄 Switching to OTP verification layout")

        // Hide phone input section
        binding.tilPhoneNumber.visibility = View.GONE
        binding.btnSendOtp.visibility = View.GONE

        // Show OTP input section
        binding.tilOtp.visibility = View.VISIBLE
        binding.btnVerifyOtp.visibility = View.VISIBLE
        binding.btnResendOtp.visibility = View.VISIBLE
        binding.tvTimer.visibility = View.VISIBLE
        binding.tvOtpSentMessage.visibility = View.VISIBLE

        val phoneNumber = binding.etPhoneNumber.text.toString().trim()
        binding.tvOtpSentMessage.text = "OTP sent to +91$phoneNumber"

        // Focus on OTP input and show keyboard
        binding.etOtp.requestFocus()

        // Optional: Show soft keyboard
        val imm =
            getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.showSoftInput(binding.etOtp, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
    }

    private fun resetToPhoneInput() {
        Log.d(TAG, "🔄 Resetting to phone input")

        // Stop timer
        countDownTimer?.cancel()

        // Reset state
        isOtpSent = false
        storedVerificationId = null
        resendToken = null

        // Show phone input section
        binding.tilPhoneNumber.visibility = View.VISIBLE
        binding.btnSendOtp.visibility = View.VISIBLE

        // Hide OTP input section
        binding.tilOtp.visibility = View.GONE
        binding.btnVerifyOtp.visibility = View.GONE
        binding.btnResendOtp.visibility = View.GONE
        binding.tvTimer.visibility = View.GONE
        binding.tvOtpSentMessage.visibility = View.GONE

        // Clear OTP field
        binding.etOtp.text?.clear()

        // Focus on phone number input
        binding.etPhoneNumber.requestFocus()
    }

    private fun startResendTimer() {
        Log.d(TAG, "⏰ Starting resend timer")

        binding.btnResendOtp.isEnabled = false
        binding.tvTimer.visibility = View.VISIBLE

        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(RESEND_TIMEOUT * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                binding.tvTimer.text = "Resend OTP in $secondsRemaining seconds"
                binding.btnResendOtp.text = "Resend in ${secondsRemaining}s"
            }

            override fun onFinish() {
                Log.d(TAG, "⏰ Resend timer finished")
                binding.tvTimer.text = "Didn't receive OTP?"
                binding.btnResendOtp.text = "Resend OTP"
                binding.btnResendOtp.isEnabled = true
            }
        }.start()
    }

    private fun showLoading(show: Boolean, message: String = "Please wait...") {
        if (show) {
            try {
                if (!loadingDialog.isShowing) {
                    loadingDialog.show()
                }
                // Update loading message if the dialog has the TextView
                loadingDialog.findViewById<android.widget.TextView>(R.id.tvLoadingMessage)?.text =
                    message
            } catch (e: Exception) {
                Log.e(TAG, "Error showing loading dialog", e)
            }
        } else {
            try {
                if (loadingDialog.isShowing) {
                    loadingDialog.dismiss()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error hiding loading dialog", e)
            }
        }

        // Update button states
        binding.btnSendOtp.isEnabled = !show
        binding.btnVerifyOtp.isEnabled = !show

        // Only enable resend if timer is not running and we have a token
        if (!show) {
            binding.btnResendOtp.isEnabled = countDownTimer == null && resendToken != null
        } else {
            binding.btnResendOtp.isEnabled = false
        }
    }

    private fun hideLoading() {
        showLoading(false)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        Log.d(TAG, "🍞 Toast: $message")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "🧹 Cleaning up PhoneAuthActivity")

        countDownTimer?.cancel()

        try {
            if (::loadingDialog.isInitialized && loadingDialog.isShowing) {
                loadingDialog.dismiss()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error dismissing dialog in onDestroy", e)
        }
    }

    override fun onPause() {
        super.onPause()
        // Don't cancel timer when activity is paused, let it continue
        Log.d(TAG, "Activity paused")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "Activity resumed")
        // Timer continues running in background, no need to restart
    }

    override fun onBackPressed() {
        if (isOtpSent) {
            // Show confirmation dialog when back is pressed during OTP verification
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Cancel OTP Verification?")
                .setMessage("Are you sure you want to cancel the OTP verification process?")
                .setPositiveButton("Yes") { _, _ ->
                    resetToPhoneInput()
                }
                .setNegativeButton("No", null)
                .show()
        } else {
            super.onBackPressed()
        }
    }
}