package com.dawnbellsuha.uha.registration

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.dawnbellsuha.uha.R
import com.dawnbellsuha.uha.databinding.ActivityEditProfileBinding
import com.dawnbellsuha.uha.models.UserProfile
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private var currentUserProfile: UserProfile? = null
    private var selectedImageUri: Uri? = null

    companion object {
        private const val TAG = "EditProfileActivity"
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                selectedImageUri = uri
                Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .circleCrop()
                    .into(binding.ivProfilePicture)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading selected image: ${e.message}", e)
                showToast("Error loading selected image")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            binding = ActivityEditProfileBinding.inflate(layoutInflater)
            setContentView(binding.root)

            initializeFirebase()
            setupClickListeners()
            setupSpinners()
            loadCurrentProfile()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}", e)
            showToast("Error initializing edit profile")
            finish()
        }
    }

    private fun initializeFirebase() {
        try {
            firebaseAuth = FirebaseAuth.getInstance()
            firestore = FirebaseFirestore.getInstance()
            storage = FirebaseStorage.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firebase: ${e.message}", e)
            throw e
        }
    }

    private fun setupClickListeners() {
        try {
            binding.btnBack.setOnClickListener {
                onBackPressed()
            }

            binding.btnSaveProfile.setOnClickListener {
                saveProfile()
            }

            binding.ivProfilePicture.setOnClickListener {
                showImagePickerDialog()
            }

            binding.btnChangeProfilePicture.setOnClickListener {
                showImagePickerDialog()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up click listeners: ${e.message}", e)
        }
    }

    private fun setupSpinners() {
        try {
            // State AutoComplete
            val states = arrayOf(
                "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh",
                "Goa", "Gujarat", "Haryana", "Himachal Pradesh", "Jharkhand", "Karnataka",
                "Kerala", "Madhya Pradesh", "Maharashtra", "Manipur", "Meghalaya", "Mizoram",
                "Nagaland", "Odisha", "Punjab", "Rajasthan", "Sikkim", "Tamil Nadu",
                "Telangana", "Tripura", "Uttar Pradesh", "Uttarakhand", "West Bengal",
                "Delhi", "Chandigarh", "Dadra and Nagar Haveli", "Daman and Diu",
                "Lakshadweep", "Puducherry", "Andaman and Nicobar Islands", "Ladakh", "Jammu and Kashmir"
            )

            val stateAdapter = ArrayAdapter(this, R.layout.dropdown_item, states)
            binding.autoCompleteState.setAdapter(stateAdapter)

            // Class/Exam AutoComplete
            val classExams = arrayOf(
                "Class 6", "Class 7", "Class 8", "Class 9", "Class 10",
                "Class 11 - Science", "Class 12 - Science",
                "Class 11 - Commerce", "Class 12 - Commerce",
                "Class 11 - Arts", "Class 12 - Arts",
                "JEE Main", "JEE Advanced", "NEET UG", "NEET PG",
                "BITSAT", "VITEEE", "SRMJEEE", "KIITEE", "COMEDK",
                "MHT CET", "KCET", "EAMCET", "WBJEE", "GUJCET",
                "KEAM", "AP EAMCET", "TS EAMCET", "OJEE", "BCECE",
                "CUET", "CLAT", "AILET", "LSAT India",
                "CAT", "XAT", "SNAP", "NMAT", "CMAT", "MAT",
                "GATE", "GRE", "GMAT", "IELTS", "TOEFL"
            )

            val classExamAdapter = ArrayAdapter(this, R.layout.dropdown_item, classExams)
            binding.autoCompleteClassExam.setAdapter(classExamAdapter)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up spinners: ${e.message}", e)
        }
    }

    private fun loadCurrentProfile() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            showLoading(true)

            firestore.collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    showLoading(false)
                    try {
                        if (document.exists()) {
                            currentUserProfile = document.toObject(UserProfile::class.java)
                            // Validate that the profile was properly loaded
                            if (currentUserProfile != null && currentUserProfile!!.uid.isNotEmpty()) {
                                populateFields(currentUserProfile!!)
                            } else {
                                Log.w(TAG, "Profile document exists but conversion failed or uid is empty")
                                showToast("Error loading profile data")
                            }
                        } else {
                            Log.w(TAG, "Profile document does not exist")
                            showToast("Profile not found")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing profile document: ${e.message}", e)
                        showToast("Error processing profile data")
                    }
                }
                .addOnFailureListener { exception ->
                    showLoading(false)
                    Log.e(TAG, "Error loading profile: ${exception.message}", exception)
                    showToast("Error loading profile: ${exception.message}")
                }
        } else {
            Log.w(TAG, "User not authenticated")
            showToast("User not authenticated")
            finish()
        }
    }

    private fun populateFields(userProfile: UserProfile) {
        try {
            binding.apply {
                etFullName.setText(userProfile.fullName)
                etAge.setText(userProfile.age)
                etMobile.setText(userProfile.mobile)
                etEmail.setText(userProfile.email)
                etAddress.setText(userProfile.address)

                // Set AutoCompleteTextView selections safely
                if (userProfile.state.isNotEmpty()) {
                    autoCompleteState.setText(userProfile.state, false)
                }
                if (userProfile.classExam.isNotEmpty()) {
                    autoCompleteClassExam.setText(userProfile.classExam, false)
                }

                // Load profile picture
                if (userProfile.profilePictureUrl.isNotEmpty()) {
                    try {
                        Glide.with(this@EditProfileActivity)
                            .load(userProfile.profilePictureUrl)
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .error(R.drawable.ic_profile_placeholder)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .circleCrop()
                            .into(ivProfilePicture)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error loading profile image: ${e.message}", e)
                        ivProfilePicture.setImageResource(R.drawable.ic_profile_placeholder)
                    }
                } else {
                    ivProfilePicture.setImageResource(R.drawable.ic_profile_placeholder)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error populating fields: ${e.message}", e)
            showToast("Error loading profile data")
        }
    }

    private fun showImagePickerDialog() {
        try {
            val options = arrayOf("Choose from Gallery", "Remove Photo")
            MaterialAlertDialogBuilder(this)
                .setTitle("Profile Picture")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> imagePickerLauncher.launch("image/*")
                        1 -> {
                            selectedImageUri = null
                            binding.ivProfilePicture.setImageResource(R.drawable.ic_profile_placeholder)
                        }
                    }
                }
                .show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing image picker dialog: ${e.message}", e)
            showToast("Error opening image picker")
        }
    }

    private fun saveProfile() {
        if (validateFields()) {
            showLoading(true)

            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                if (selectedImageUri != null) {
                    uploadImageAndSaveProfile(currentUser.uid)
                } else {
                    saveProfileToFirestore(currentUser.uid, currentUserProfile?.profilePictureUrl ?: "")
                }
            } else {
                showLoading(false)
                showToast("User not authenticated")
            }
        }
    }

    private fun validateFields(): Boolean {
        try {
            binding.apply {
                // Clear previous errors
                tilFullName.error = null
                tilAge.error = null
                tilMobile.error = null
                tilEmail.error = null
                tilState.error = null
                tilClassExam.error = null
                tilAddress.error = null

                if (etFullName.text.toString().trim().isEmpty()) {
                    tilFullName.error = "Full name is required"
                    etFullName.requestFocus()
                    return false
                }

                if (etAge.text.toString().trim().isEmpty()) {
                    tilAge.error = "Age is required"
                    etAge.requestFocus()
                    return false
                }

                val age = etAge.text.toString().toIntOrNull()
                if (age == null || age < 10 || age > 100) {
                    tilAge.error = "Please enter a valid age (10-100)"
                    etAge.requestFocus()
                    return false
                }

                val mobile = etMobile.text.toString().trim()
                if (mobile.length != 10 || !mobile.all { it.isDigit() }) {
                    tilMobile.error = "Please enter a valid 10-digit mobile number"
                    etMobile.requestFocus()
                    return false
                }

                val email = etEmail.text.toString().trim()
                if (email.isEmpty()) {
                    tilEmail.error = "Email is required"
                    etEmail.requestFocus()
                    return false
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    tilEmail.error = "Please enter a valid email address"
                    etEmail.requestFocus()
                    return false
                }

                if (autoCompleteState.text.toString().trim().isEmpty()) {
                    tilState.error = "Please select your state"
                    autoCompleteState.requestFocus()
                    return false
                }

                if (autoCompleteClassExam.text.toString().trim().isEmpty()) {
                    tilClassExam.error = "Please select your class/exam"
                    autoCompleteClassExam.requestFocus()
                    return false
                }

                if (etAddress.text.toString().trim().isEmpty()) {
                    tilAddress.error = "Address is required"
                    etAddress.requestFocus()
                    return false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error validating fields: ${e.message}", e)
            showToast("Error validating input")
            return false
        }

        return true
    }

    private fun uploadImageAndSaveProfile(userId: String) {
        val imageRef = storage.reference.child("profile_pictures/$userId.jpg")

        selectedImageUri?.let { uri ->
            imageRef.putFile(uri)
                .addOnProgressListener { taskSnapshot ->
                    // Optional: Show upload progress
                    val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                    Log.d(TAG, "Upload progress: $progress%")
                }
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        saveProfileToFirestore(userId, downloadUrl.toString())
                    }.addOnFailureListener { exception ->
                        showLoading(false)
                        Log.e(TAG, "Error getting download URL: ${exception.message}", exception)
                        showToast("Error getting image URL: ${exception.message}")
                    }
                }
                .addOnFailureListener { exception ->
                    showLoading(false)
                    Log.e(TAG, "Error uploading image: ${exception.message}", exception)
                    showToast("Error uploading image: ${exception.message}")
                }
        }
    }

    private fun saveProfileToFirestore(userId: String, profilePictureUrl: String) {
        try {
            val currentTime = System.currentTimeMillis()
            val updatedProfile = UserProfile(
                uid = userId,
                fullName = binding.etFullName.text.toString().trim(),
                age = binding.etAge.text.toString().trim(),
                mobile = binding.etMobile.text.toString().trim(),
                email = binding.etEmail.text.toString().trim(),
                state = binding.autoCompleteState.text.toString().trim(),
                classExam = binding.autoCompleteClassExam.text.toString().trim(),
                address = binding.etAddress.text.toString().trim(),
                profilePictureUrl = profilePictureUrl,
                createdAt = currentUserProfile?.createdAt ?: currentTime,
                updatedAt = currentTime
            )

            // Validate profile before saving
            if (!updatedProfile.isProfileComplete()) {
                showLoading(false)
                showToast("Please fill all required fields")
                return
            }

            firestore.collection("users")
                .document(userId)
                .set(updatedProfile)
                .addOnSuccessListener {
                    showLoading(false)
                    showToast("Profile updated successfully")
                    setResult(RESULT_OK)
                    finish()
                }
                .addOnFailureListener { exception ->
                    showLoading(false)
                    Log.e(TAG, "Error saving profile: ${exception.message}", exception)
                    showToast("Error saving profile: ${exception.message}")
                }
        } catch (e: Exception) {
            showLoading(false)
            Log.e(TAG, "Error creating profile object: ${e.message}", e)
            showToast("Error preparing profile data")
        }
    }

    private fun showLoading(show: Boolean) {
        try {
            binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
            binding.btnSaveProfile.isEnabled = !show

            // Optional: Disable other interactive elements during loading
            binding.btnBack.isEnabled = !show
            binding.ivProfilePicture.isEnabled = !show
            binding.btnChangeProfilePicture.isEnabled = !show
        } catch (e: Exception) {
            Log.e(TAG, "Error showing loading: ${e.message}", e)
        }
    }

    private fun showToast(message: String) {
        try {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing toast: ${e.message}", e)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        try {
            super.onBackPressed()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onBackPressed: ${e.message}", e)
            finish()
        }
    }
}