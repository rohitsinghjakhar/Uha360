package com.dawnbellsuha.uha.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.dawnbellsuha.uha.R
import com.dawnbellsuha.uha.adapters.OnboardingAdapter
import com.dawnbellsuha.uha.adapters.OnboardingItem
import com.dawnbellsuha.uha.databinding.ActivityOnboardingBinding
import com.google.android.material.tabs.TabLayoutMediator

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPref = getSharedPreferences("UHA_PREFS", MODE_PRIVATE)

        val onboardingItems = listOf(
            OnboardingItem(
                R.drawable.ic_onboarding_notes,
                "Comprehensive Notes",
                "Access well-structured notes for all subjects and classes",
                R.color.light_blue_bg
            ),
            OnboardingItem(
                R.drawable.ic_onboarding_quizzes,
                "Interactive Quizzes",
                "Test your knowledge with our adaptive quiz system",
                R.color.light_green_bg
            ),
            OnboardingItem(
                R.drawable.ic_onboarding_live,
                "Live Sessions",
                "Connect with teachers and peers in real-time learning sessions",
                R.color.light_purple_bg
            )
        )

        val adapter = OnboardingAdapter(onboardingItems)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.dotsIndicator, binding.viewPager) { _, _ -> }.attach()

        binding.btnNext.setOnClickListener {
            val currentItem = binding.viewPager.currentItem
            if (currentItem < onboardingItems.lastIndex) {
                binding.viewPager.currentItem = currentItem + 1
            } else {
                sharedPref.edit().putBoolean("is_first_time", false).apply()
                navigateToAuth()
            }
        }

        binding.btnSkip.setOnClickListener {
            sharedPref.edit().putBoolean("is_first_time", false).apply()
            navigateToAuth()
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.btnNext.text =
                    if (position == onboardingItems.lastIndex) "Get Started" else "Next"
            }
        })
    }

    private fun navigateToAuth() {
        startActivity(Intent(this, AuthActivity::class.java))
        finish()
    }
}
