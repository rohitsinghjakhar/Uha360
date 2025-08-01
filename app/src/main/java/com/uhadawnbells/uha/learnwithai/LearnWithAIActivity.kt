package com.uhadawnbells.uha.learnwithai

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.uhadawnbells.uha.databinding.ActivityLearnWithAiBinding
import com.uhadawnbells.uha.utils.GoogleSheetsHelper
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import com.uhadawnbells.uha.R

class LearnWithAIActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLearnWithAiBinding
    private var selectedAgeGroup = ""
    private var selectedInterestLevel = ""

    private val ageGroups = arrayOf(
        "Select Age Group",
        "5-10 years (Elementary)",
        "11-14 years (Middle School)",
        "15-18 years (High School)",
        "19-25 years (College/University)",
        "26-35 years (Young Professional)",
        "36-50 years (Professional)",
        "50+ years (Senior)"
    )

    private val interestLevels = arrayOf(
        "Select Interest Level",
        "Very High - Want to implement immediately",
        "High - Actively exploring AI learning",
        "Moderate - Curious about AI possibilities",
        "Low - Just getting started with AI",
        "Professional - For educational research",
        "Academic - For study/thesis purposes"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLearnWithAiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupSpinners()
        setupClickListeners()
        animateViews()
    }

    private fun setupUI() {
        // Set status bar color
        window.statusBarColor = ContextCompat.getColor(this, R.color.ai_gradient_start)

        // Setup toolbar
        supportActionBar?.apply {
            title = "Learn with AI"
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
    }

    private fun setupSpinners() {
        setupAgeGroupSpinner()
        setupInterestLevelSpinner()
    }

    private fun setupAgeGroupSpinner() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            ageGroups
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.spinnerAgeGroup.apply {
            this.adapter = adapter
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedAgeGroup = if (position > 0) ageGroups[position] else ""
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    selectedAgeGroup = ""
                }
            }
        }
    }

    private fun setupInterestLevelSpinner() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            interestLevels
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.spinnerInterestLevel.apply {
            this.adapter = adapter
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedInterestLevel = if (position > 0) interestLevels[position] else ""
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    selectedInterestLevel = ""
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSubmitAiInquiry.setOnClickListener {
            if (validateForm()) {
                submitAiInquiry()
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        // Validate name
        if (binding.etAiName.text.toString().trim().isEmpty()) {
            binding.tilAiName.error = "Name is required"
            isValid = false
        } else {
            binding.tilAiName.error = null
        }

        // Validate email
        val email = binding.etAiEmail.text.toString().trim()
        if (email.isEmpty()) {
            binding.tilAiEmail.error = "Email is required"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilAiEmail.error = "Please enter a valid email"
            isValid = false
        } else {
            binding.tilAiEmail.error = null
        }

        // Validate phone if provided
        val phone = binding.etAiPhone.text.toString().trim()
        if (phone.isNotEmpty() && phone.length < 10) {
            binding.tilAiPhone.error = "Please enter a valid phone number"
            isValid = false
        } else {
            binding.tilAiPhone.error = null
        }

        // Check age group
        if (selectedAgeGroup.isEmpty()) {
            showError("Please select your age group")
            isValid = false
        }

        // Check interest level
        if (selectedInterestLevel.isEmpty()) {
            showError("Please select your interest level")
            isValid = false
        }

        return isValid
    }

    private fun submitAiInquiry() {
        showLoading(true)

        val aiInquiryData = hashMapOf(
            "timestamp" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
            "name" to binding.etAiName.text.toString().trim(),
            "email" to binding.etAiEmail.text.toString().trim(),
            "phone" to binding.etAiPhone.text.toString().trim(),
            "age_group" to selectedAgeGroup,
            "interest_level" to selectedInterestLevel,
            "education_background" to binding.etEducationBackground.text.toString().trim(),
            "ai_features_interest" to binding.etAiFeatures.text.toString().trim(),
            "learning_goals" to binding.etLearningGoals.text.toString().trim(),
            "use_case" to binding.etUseCase.text.toString().trim(),
            "ai_experience" to binding.etAiExperience.text.toString().trim(),
            "questions" to binding.etQuestions.text.toString().trim(),
            "inquiry_type" to "Know UHA AI",
            "status" to "New Inquiry",
            "form_source" to "Learn with AI Activity"
        )

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val success = withContext(Dispatchers.IO) {
                    GoogleSheetsHelper.submitAiInquiryData(aiInquiryData)
                }

                showLoading(false)

                if (success) {
                    showSuccessMessage()
                    clearForm()
                } else {
                    showError("Failed to submit inquiry. Please try again.")
                }
            } catch (e: Exception) {
                showLoading(false)
                showError("Network error. Please check your connection and try again.")
                e.printStackTrace()
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBarAi.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnSubmitAiInquiry.isEnabled = !show
        binding.btnSubmitAiInquiry.text = if (show) "Submitting..." else "Learn About UHA AI"
    }

    private fun showSuccessMessage() {
        Snackbar.make(
            binding.root,
            "✅ Your AI inquiry has been submitted! Our team will contact you with detailed information about UHA AI.",
            Snackbar.LENGTH_LONG
        ).apply {
            setBackgroundTint(ContextCompat.getColor(this@LearnWithAIActivity, R.color.success_color))
            setTextColor(ContextCompat.getColor(this@LearnWithAIActivity, R.color.white))
            setAction("OK") { dismiss() }
            setActionTextColor(ContextCompat.getColor(this@LearnWithAIActivity, R.color.white))
            show()
        }

    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).apply {
            setBackgroundTint(ContextCompat.getColor(this@LearnWithAIActivity, R.color.error_color))
            setTextColor(ContextCompat.getColor(this@LearnWithAIActivity, R.color.white))
            setAction("RETRY") {
                if (validateForm()) {
                    submitAiInquiry()
                }
            }
            setActionTextColor(ContextCompat.getColor(this@LearnWithAIActivity, R.color.white))
            show()
        }
    }

    private fun clearForm() {
        binding.apply {
            etAiName.text?.clear()
            etAiEmail.text?.clear()
            etAiPhone.text?.clear()
            spinnerAgeGroup.setSelection(0)
            spinnerInterestLevel.setSelection(0)
            etEducationBackground.text?.clear()
            etAiFeatures.text?.clear()
            etLearningGoals.text?.clear()
            etUseCase.text?.clear()
            etAiExperience.text?.clear()
            etQuestions.text?.clear()

            // Clear any error states
            tilAiName.error = null
            tilAiEmail.error = null
            tilAiPhone.error = null
        }
        selectedAgeGroup = ""
        selectedInterestLevel = ""
    }

    private fun animateViews() {
        // Animate header card entrance
        binding.aiHeaderCard.apply {
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
        binding.aiFormCard.apply {
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