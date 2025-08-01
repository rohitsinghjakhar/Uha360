package com.uhadawnbells.uha.teacher

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.uhadawnbells.uha.R
import com.uhadawnbells.uha.databinding.ActivityRegistrationFormBinding
import com.uhadawnbells.uha.utils.GoogleSheetsHelper
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class RegistrationFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationFormBinding
    private var selectedCategory = ""
    private val registrationCategories = arrayOf(
        "Select Registration Category",
        "Teacher",
        "Educational Institution",
        "NGO",
        "Government Agency",
        "General Category"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupSpinner()
        setupClickListeners()
        animateViews()
    }

    private fun setupUI() {
        // Set status bar color
        window.statusBarColor = ContextCompat.getColor(this, R.color.primary_light)

        // Setup toolbar
        supportActionBar?.apply {
            title = "Registration Form"
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            registrationCategories
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.spinnerRegistrationCategory.apply {
            this.adapter = adapter
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedCategory = if (position > 0) registrationCategories[position] else ""
                    updateFormBasedOnCategory(selectedCategory)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    selectedCategory = ""
                }
            }
        }
    }

    private fun updateFormBasedOnCategory(category: String) {
        // Update hint text and required fields based on registration category
        when (category) {
            "Teacher" -> {
                binding.tilOrganization.hint = "School/Institution Name"
                binding.tilPosition.hint = "Teaching Position/Subject"
                binding.tilExperience.hint = "Teaching Experience & Subjects Taught"

                // Make organization required for teachers
                binding.tilOrganization.isHelperTextEnabled = true
                binding.tilOrganization.helperText = "Required for teachers"
            }
            "Educational Institution" -> {
                binding.tilOrganization.hint = "Institution Name *"
                binding.tilPosition.hint = "Your Role/Position"
                binding.tilExperience.hint = "Institution Background & Programs"

                binding.tilOrganization.isHelperTextEnabled = true
                binding.tilOrganization.helperText = "Required field"
            }
            "NGO" -> {
                binding.tilOrganization.hint = "NGO Name *"
                binding.tilPosition.hint = "Your Role in NGO"
                binding.tilExperience.hint = "NGO Mission & Your Involvement"

                binding.tilOrganization.isHelperTextEnabled = true
                binding.tilOrganization.helperText = "Required field"
            }
            "Government Agency" -> {
                binding.tilOrganization.hint = "Agency/Department Name *"
                binding.tilPosition.hint = "Designation/Position"
                binding.tilExperience.hint = "Role & Responsibilities"

                binding.tilOrganization.isHelperTextEnabled = true
                binding.tilOrganization.helperText = "Required field"
            }
            "General Category" -> {
                binding.tilOrganization.hint = "Organization/Company Name"
                binding.tilPosition.hint = "Position/Role"
                binding.tilExperience.hint = "Background & Interest in Education"

                binding.tilOrganization.isHelperTextEnabled = false
                binding.tilOrganization.helperText = ""
            }
            else -> {
                // Default state
                binding.tilOrganization.hint = "Organization/Institution Name"
                binding.tilPosition.hint = "Position/Designation"
                binding.tilExperience.hint = "Experience/Background"

                binding.tilOrganization.isHelperTextEnabled = false
                binding.tilOrganization.helperText = ""
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSubmitRegistration.setOnClickListener {
            if (validateForm()) {
                submitRegistration()
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        // Check registration category
        if (selectedCategory.isEmpty()) {
            showError("Please select a registration category")
            isValid = false
        }

        // Validate full name
        if (binding.etFullName.text.toString().trim().isEmpty()) {
            binding.tilFullName.error = "Full name is required"
            isValid = false
        } else {
            binding.tilFullName.error = null
        }

        // Validate email
        val email = binding.etEmail.text.toString().trim()
        if (email.isEmpty()) {
            binding.tilEmail.error = "Email is required"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Please enter a valid email"
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        // Validate phone
        val phone = binding.etPhone.text.toString().trim()
        if (phone.isEmpty()) {
            binding.tilPhone.error = "Phone number is required"
            isValid = false
        } else if (phone.length < 10) {
            binding.tilPhone.error = "Please enter a valid phone number"
            isValid = false
        } else {
            binding.tilPhone.error = null
        }

        // Validate organization for required categories
        if (selectedCategory in listOf("Teacher", "Educational Institution", "NGO", "Government Agency") &&
            binding.etOrganization.text.toString().trim().isEmpty()) {
            binding.tilOrganization.error = "Organization name is required for $selectedCategory"
            isValid = false
        } else {
            binding.tilOrganization.error = null
        }

        // Validate location
        if (binding.etLocation.text.toString().trim().isEmpty()) {
            binding.tilLocation.error = "Location is required"
            isValid = false
        } else {
            binding.tilLocation.error = null
        }

        return isValid
    }

    private fun submitRegistration() {
        showLoading(true)

        val registrationData = hashMapOf(
            "timestamp" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
            "registration_category" to selectedCategory,
            "full_name" to binding.etFullName.text.toString().trim(),
            "email" to binding.etEmail.text.toString().trim(),
            "phone" to binding.etPhone.text.toString().trim(),
            "organization" to binding.etOrganization.text.toString().trim(),
            "position" to binding.etPosition.text.toString().trim(),
            "experience" to binding.etExperience.text.toString().trim(),
            "location" to binding.etLocation.text.toString().trim(),
            "hear_about_us" to binding.etHearAbout.text.toString().trim(),
            "message" to binding.etMessage.text.toString().trim(),
            "status" to "Pending Review",
            "form_type" to "Registration Form"
        )

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val success = withContext(Dispatchers.IO) {
                    GoogleSheetsHelper.submitRegistrationFormData(registrationData)
                }

                showLoading(false)

                if (success) {
                    showSuccessMessage()
                    clearForm()
                } else {
                    showError("Failed to submit registration. Please try again.")
                }
            } catch (e: Exception) {
                showLoading(false)
                showError("Network error. Please check your connection and try again.")
                e.printStackTrace()
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnSubmitRegistration.isEnabled = !show
        binding.btnSubmitRegistration.text = if (show) "Submitting..." else "Submit Registration"
    }

    private fun showSuccessMessage() {
        Snackbar.make(
            binding.root,
            "Registration submitted successfully! We'll contact you soon.",
            Snackbar.LENGTH_LONG
        ).apply {
            setBackgroundTint(ContextCompat.getColor(this@RegistrationFormActivity, R.color.success_color))
            setTextColor(ContextCompat.getColor(this@RegistrationFormActivity, R.color.white))
            setAction("OK") { dismiss() }
            setActionTextColor(ContextCompat.getColor(this@RegistrationFormActivity, R.color.white))
            show()
        }

    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).apply {
            setBackgroundTint(ContextCompat.getColor(this@RegistrationFormActivity, R.color.error_color))
            setTextColor(ContextCompat.getColor(this@RegistrationFormActivity, R.color.white))
            setAction("RETRY") {
                if (validateForm()) {
                    submitRegistration()
                }
            }
            setActionTextColor(ContextCompat.getColor(this@RegistrationFormActivity, R.color.white))
            show()
        }
    }

    private fun clearForm() {
        binding.apply {
            spinnerRegistrationCategory.setSelection(0)
            etFullName.text?.clear()
            etEmail.text?.clear()
            etPhone.text?.clear()
            etOrganization.text?.clear()
            etPosition.text?.clear()
            etExperience.text?.clear()
            etLocation.text?.clear()
            etHearAbout.text?.clear()
            etMessage.text?.clear()

            // Clear any error states
            tilFullName.error = null
            tilEmail.error = null
            tilPhone.error = null
            tilOrganization.error = null
            tilLocation.error = null
        }
        selectedCategory = ""
    }

    private fun animateViews() {
        // Animate header card entrance
        binding.headerCard.apply {
            alpha = 0f
            scaleX = 0.8f
            scaleY = 0.8f
            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(500)
                .setInterpolator(android.view.animation.OvershootInterpolator())
                .start()
        }

        // Animate form card entrance
        binding.formCard.apply {
            alpha = 0f
            translationY = 100f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setStartDelay(200)
                .setInterpolator(android.view.animation.DecelerateInterpolator())
                .start()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Add custom transition
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}