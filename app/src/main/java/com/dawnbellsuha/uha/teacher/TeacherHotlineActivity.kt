package com.dawnbellsuha.uha.teacher

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.dawnbellsuha.uha.R
import com.dawnbellsuha.uha.databinding.ActivityTeacherHotlineBinding
import com.dawnbellsuha.uha.utils.GoogleSheetsHelper
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class TeacherHotlineActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherHotlineBinding
    private var selectedGrade = ""
    private var selectedSubject = ""
    private var selectedSessionType = ""
    private var selectedDateTime = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherHotlineBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupSpinners()
        setupClickListeners()
        animateViews()
    }

    private fun setupUI() {
        // Set status bar color
        window.statusBarColor = ContextCompat.getColor(this, R.color.teacher_gradient_start)

        // Setup toolbar if needed
        supportActionBar?.apply {
            title = "Teacher Hotline"
            setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setupSpinners() {
        setupGradeSpinner()
        setupSubjectSpinner()
        setupSessionTypeSpinner()
    }

    private fun setupGradeSpinner() {
        val grades = arrayOf(
            "Select Grade/Class",
            "Pre-K (3-4 years)",
            "Kindergarten (5-6 years)",
            "Grade 1 (6-7 years)",
            "Grade 2 (7-8 years)",
            "Grade 3 (8-9 years)",
            "Grade 4 (9-10 years)",
            "Grade 5 (10-11 years)",
            "Grade 6 (11-12 years)",
            "Grade 7 (12-13 years)",
            "Grade 8 (13-14 years)",
            "Grade 9 (14-15 years)",
            "Grade 10 (15-16 years)",
            "Grade 11 (16-17 years)",
            "Grade 12 (17-18 years)"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, grades).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.spinnerGrade.apply {
            this.adapter = adapter
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedGrade = if (position > 0) grades[position] else ""
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    selectedGrade = ""
                }
            }
        }
    }

    private fun setupSubjectSpinner() {
        val subjects = arrayOf(
            "Select Subject",
            "Mathematics",
            "English Language Arts",
            "Science",
            "Social Studies",
            "Hindi",
            "Physics",
            "Chemistry",
            "Biology",
            "Computer Science",
            "History",
            "Geography",
            "Art & Craft",
            "Music",
            "Physical Education",
            "Other"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, subjects).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.spinnerSubject.apply {
            this.adapter = adapter
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedSubject = if (position > 0) subjects[position] else ""
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    selectedSubject = ""
                }
            }
        }
    }

    private fun setupSessionTypeSpinner() {
        val sessionTypes = arrayOf(
            "Select Session Type",
            "One-on-One Tutoring",
            "Group Session (2-4 students)",
            "Homework Help",
            "Test Preparation",
            "Concept Clarification",
            "Project Guidance",
            "Study Skills Development",
            "Career Guidance",
            "Subject Matter Expert",
            "Mental Health Coach",
            "Examination Trainer",
            "Jobs Abroad Consultant",
            "Interview coach",
            "Job Placement Consultant"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sessionTypes).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.spinnerSessionType.apply {
            this.adapter = adapter
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedSessionType = if (position > 0) sessionTypes[position] else ""
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    selectedSessionType = ""
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnBookSession.setOnClickListener {
            if (validateForm()) {
                submitBookingRequest()
            }
        }

        binding.etPreferredTime.setOnClickListener {
            showDateTimePicker()
        }
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)

                TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)

                        val dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
                        selectedDateTime = dateFormat.format(calendar.time)
                        binding.etPreferredTime.setText(selectedDateTime)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis()
            show()
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        // Validate student name
        if (binding.etStudentName.text.toString().trim().isEmpty()) {
            binding.tilStudentName.error = "Student name is required"
            isValid = false
        } else {
            binding.tilStudentName.error = null
        }

        // Validate parent name
        if (binding.etParentName.text.toString().trim().isEmpty()) {
            binding.tilParentName.error = "Parent/Guardian name is required"
            isValid = false
        } else {
            binding.tilParentName.error = null
        }

        // Validate contact number
        val phone = binding.etContactNumber.text.toString().trim()
        if (phone.isEmpty()) {
            binding.tilContactNumber.error = "Contact number is required"
            isValid = false
        } else if (phone.length < 10) {
            binding.tilContactNumber.error = "Please enter a valid phone number"
            isValid = false
        } else {
            binding.tilContactNumber.error = null
        }

        // Validate email if provided
        val email = binding.etContactEmail.text.toString().trim()
        if (email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilContactEmail.error = "Please enter a valid email"
            isValid = false
        } else {
            binding.tilContactEmail.error = null
        }

        // Check required dropdowns
        if (selectedGrade.isEmpty()) {
            showError("Please select student's grade/class")
            isValid = false
        }

        if (selectedSubject.isEmpty()) {
            showError("Please select a subject")
            isValid = false
        }

        if (selectedSessionType.isEmpty()) {
            showError("Please select session type")
            isValid = false
        }

        return isValid
    }

    private fun submitBookingRequest() {
        showLoading(true)

        val bookingData = hashMapOf(
            "timestamp" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
            "student_name" to binding.etStudentName.text.toString().trim(),
            "parent_name" to binding.etParentName.text.toString().trim(),
            "contact_number" to binding.etContactNumber.text.toString().trim(),
            "contact_email" to binding.etContactEmail.text.toString().trim(),
            "student_grade" to selectedGrade,
            "subject" to selectedSubject,
            "session_type" to selectedSessionType,
            "preferred_datetime" to selectedDateTime,
            "learning_goals" to binding.etLearningGoals.text.toString().trim(),
            "additional_notes" to binding.etAdditionalNotes.text.toString().trim(),
            "booking_status" to "Pending Confirmation",
            "request_type" to "Teacher Hotline Booking"
        )

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val success = withContext(Dispatchers.IO) {
                    GoogleSheetsHelper.submitTeacherBookingData(bookingData)
                }

                showLoading(false)

                if (success) {
                    showSuccessMessage()
                    clearForm()
                } else {
                    showError("Failed to submit booking request. Please try again.")
                }
            } catch (e: Exception) {
                showLoading(false)
                showError("Network error. Please check your connection and try again.")
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBarHotline.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnBookSession.isEnabled = !show
        binding.btnBookSession.text = if (show) "Booking..." else "Book Teaching Session"
    }

    private fun showSuccessMessage() {
        Snackbar.make(
            binding.root,
            "Session booking request submitted successfully! Our teacher will contact you soon to confirm the session details.",
            Snackbar.LENGTH_LONG
        ).apply {
            setBackgroundTint(ContextCompat.getColor(this@TeacherHotlineActivity, R.color.success_color))
            setTextColor(ContextCompat.getColor(this@TeacherHotlineActivity, R.color.white))
            show()
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).apply {
            setBackgroundTint(ContextCompat.getColor(this@TeacherHotlineActivity, R.color.error_color))
            setTextColor(ContextCompat.getColor(this@TeacherHotlineActivity, R.color.white))
            show()
        }
    }

    private fun clearForm() {
        binding.apply {
            etStudentName.text?.clear()
            etParentName.text?.clear()
            etContactNumber.text?.clear()
            etContactEmail.text?.clear()
            spinnerGrade.setSelection(0)
            spinnerSubject.setSelection(0)
            spinnerSessionType.setSelection(0)
            etPreferredTime.setText("")
            etLearningGoals.text?.clear()
            etAdditionalNotes.text?.clear()
        }
        selectedGrade = ""
        selectedSubject = ""
        selectedSessionType = ""
        selectedDateTime = ""
    }

    private fun animateViews() {
        // Animate form card entrance
        binding.hotlineFormCard.apply {
            alpha = 0f
            translationY = 100f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setStartDelay(200)
                .start()
        }

        // Animate header card
        binding.hotlineHeaderCard.apply {
            alpha = 0f
            scaleX = 0.8f
            scaleY = 0.8f
            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(500)
                .start()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}