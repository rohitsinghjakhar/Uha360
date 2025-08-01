package com.uhadawnbells.uha.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.uhadawnbells.uha.R
import com.uhadawnbells.uha.databinding.FragmentProfileBinding
import com.uhadawnbells.uha.models.UserProfile
import com.uhadawnbells.uha.registration.EditProfileActivity
import com.uhadawnbells.uha.registration.LoginActivity
import com.uhadawnbells.uha.utils.ProfileDataMigration
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    companion object {
        private const val TAG = "ProfileFragment"
    }

    private val editProfileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            loadUserProfile()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            initializeFirebase()
            setupClickListeners()
            loadUserProfile()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onViewCreated: ${e.message}", e)
            showToast("Error initializing profile")
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
            binding.btnEditProfile.setOnClickListener {
                openEditProfile()
            }

            binding.btnEditProfileFull.setOnClickListener {
                openEditProfile()
            }

            binding.btnLogout.setOnClickListener {
                showLogoutDialog()
            }

            binding.ivProfilePicture.setOnClickListener {
                // Optional: Allow users to change profile picture
                // You can implement image picker here
                showToast("Tap Edit Profile to change picture")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up click listeners: ${e.message}", e)
        }
    }

    private fun loadUserProfile() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            showLoading(true)

            try {
                // First, migrate any missing timestamp fields
                ProfileDataMigration.migrateUserProfile(currentUser.uid, firestore)

                firestore.collection("users")
                    .document(currentUser.uid)
                    .get()
                    .addOnSuccessListener { document ->
                        showLoading(false)
                        try {
                            if (document.exists()) {
                                // Use safe conversion method
                                val userProfile = ProfileDataMigration.documentToUserProfile(document)
                                if (userProfile != null) {
                                    updateUI(userProfile)
                                } else {
                                    // Try fallback conversion
                                    val fallbackProfile = document.toObject(UserProfile::class.java)
                                    if (fallbackProfile != null && fallbackProfile.uid.isNotEmpty()) {
                                        updateUI(fallbackProfile)
                                    } else {
                                        showToast("Error loading profile data")
                                        Log.e(TAG, "Failed to convert document to UserProfile")
                                    }
                                }
                            } else {
                                showToast("Profile not found")
                                Log.w(TAG, "Profile document does not exist for user: ${currentUser.uid}")
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
            } catch (e: Exception) {
                showLoading(false)
                Log.e(TAG, "Error in loadUserProfile: ${e.message}", e)
                showToast("Error loading profile")
            }
        } else {
            // User not logged in, redirect to login
            Log.w(TAG, "User not authenticated, redirecting to login")
            redirectToLogin()
        }
    }

    private fun updateUI(userProfile: UserProfile) {
        try {
            if (!isAdded || _binding == null) {
                Log.w(TAG, "Fragment not attached or binding is null")
                return
            }

            binding.apply {
                // Use helper functions from UserProfile model
                tvFullName.text = userProfile.getDisplayValue(userProfile.fullName)
                tvAge.text = userProfile.getDisplayAge()
                tvMobile.text = userProfile.getDisplayMobile()
                tvEmail.text = userProfile.getDisplayValue(userProfile.email)
                tvState.text = userProfile.getDisplayValue(userProfile.state)
                tvClassExam.text = userProfile.getDisplayValue(userProfile.classExam)
                tvAddress.text = userProfile.getDisplayValue(userProfile.address)

                // Load profile picture if available
                if (userProfile.profilePictureUrl.isNotEmpty()) {
                    try {
                        Glide.with(this@ProfileFragment)
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
            Log.e(TAG, "Error updating UI: ${e.message}", e)
            showToast("Error updating profile display")
        }
    }

    private fun openEditProfile() {
        try {
            if (!isAdded) {
                Log.w(TAG, "Fragment not attached, cannot open edit profile")
                return
            }
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            editProfileLauncher.launch(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening edit profile: ${e.message}", e)
            showToast("Error opening edit profile")
        }
    }

    private fun showLogoutDialog() {
        try {
            if (!isAdded) {
                Log.w(TAG, "Fragment not attached, cannot show logout dialog")
                return
            }
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes") { _, _ ->
                    performLogout()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing logout dialog: ${e.message}", e)
            performLogout() // Fallback to direct logout
        }
    }

    private fun performLogout() {
        try {
            firebaseAuth.signOut()
            redirectToLogin()
        } catch (e: Exception) {
            Log.e(TAG, "Error during logout: ${e.message}", e)
            showToast("Error during logout")
        }
    }

    private fun redirectToLogin() {
        try {
            if (!isAdded) {
                Log.w(TAG, "Fragment not attached, cannot redirect to login")
                return
            }
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error redirecting to login: ${e.message}", e)
        }
    }

    private fun showLoading(show: Boolean) {
        try {
            if (!isAdded || _binding == null) {
                return
            }
            // You can implement a loading indicator here
            // For example, show/hide a progress bar
            // binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        } catch (e: Exception) {
            Log.e(TAG, "Error showing loading: ${e.message}", e)
        }
    }

    private fun showToast(message: String) {
        try {
            if (isAdded && context != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing toast: ${e.message}", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}