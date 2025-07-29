package com.dawnbellsuha.uha.activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dawnbellsuha.uha.MainActivity
import com.dawnbellsuha.uha.R
import com.dawnbellsuha.uha.databinding.ActivitySplashBinding
import com.dawnbellsuha.uha.registration.LoginActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var auth: FirebaseAuth
    private val animationHandler = Handler(Looper.getMainLooper())
    private var isUserChecked = false

    private companion object {
        const val MIN_SPLASH_DURATION = 2500L // Reduced from 3700 to 2500ms
        const val EXIT_ANIM_DURATION = 600L // Slightly reduced from 800ms
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Set the theme before super.onCreate()
        setTheme(R.style.Theme_UHA_Splash)
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        window.statusBarColor = android.graphics.Color.TRANSPARENT

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        startEntranceAnimations()

        // Check user state after animation duration
        animationHandler.postDelayed({
            checkUserState()
        }, MIN_SPLASH_DURATION)
    }

    private fun startEntranceAnimations() {
        binding.contentCard.alpha = 0f
        binding.contentCard.scaleX = 0.8f
        binding.contentCard.scaleY = 0.8f
        binding.appTitle.alpha = 0f
        binding.appTagline.alpha = 0f

        AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(binding.gradientOverlay, "alpha", 0f, 0.9f).apply {
                    duration = 500
                },
                AnimatorSet().apply {
                    playTogether(
                        ObjectAnimator.ofFloat(binding.contentCard, "alpha", 0f, 1f),
                        ObjectAnimator.ofFloat(binding.contentCard, "scaleX", 0.8f, 1f),
                        ObjectAnimator.ofFloat(binding.contentCard, "scaleY", 0.8f, 1f)
                    )
                    duration = 600
                    startDelay = 300
                },
                ObjectAnimator.ofFloat(binding.appTitle, "alpha", 0f, 1f).apply {
                    duration = 500
                    startDelay = 700
                },
                ObjectAnimator.ofFloat(binding.appTagline, "alpha", 0f, 1f).apply {
                    duration = 400
                    startDelay = 1000
                }
            )
            start()
        }
    }

    private fun checkUserState() {
        if (isUserChecked) return
        isUserChecked = true

        val currentUser = auth.currentUser
        val destination = if (currentUser != null) MainActivity::class.java else LoginActivity::class.java

        // Small delay to ensure smooth transition
        animationHandler.postDelayed({
            startExitAnimations(destination)
        }, 300)
    }

    private fun startExitAnimations(destination: Class<*>) {
        AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(binding.contentCard, "alpha", 1f, 0f),
                ObjectAnimator.ofFloat(binding.contentCard, "scaleX", 1f, 1.1f),
                ObjectAnimator.ofFloat(binding.contentCard, "scaleY", 1f, 1.1f),
                ObjectAnimator.ofFloat(binding.gradientOverlay, "alpha", 0.9f, 0f)
            )
            duration = EXIT_ANIM_DURATION
            start()
        }

        animationHandler.postDelayed({
            navigateTo(destination)
        }, EXIT_ANIM_DURATION)
    }

    private fun navigateTo(destination: Class<*>) {
        startActivity(Intent(this, destination).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        animationHandler.removeCallbacksAndMessages(null)
    }
}