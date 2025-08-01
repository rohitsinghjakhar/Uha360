package com.uhadawnbells.uha.fragments

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.uhadawnbells.uha.R
import com.uhadawnbells.uha.adapters.BannerAdapter
import com.uhadawnbells.uha.databinding.FragmentHomeBinding
import com.uhadawnbells.uha.learnwithai.LearnWithAIActivity
import com.uhadawnbells.uha.models.BannerItem
import com.uhadawnbells.uha.studentdesk.StudentDeskActivity
import com.uhadawnbells.uha.teacher.RegistrationFormActivity
import com.uhadawnbells.uha.teacher.TeacherHotlineActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import java.util.Calendar

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var bannerHandler: Handler
    private lateinit var bannerRunnable: Runnable
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    companion object {
        private const val TAG = "HomeFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = Firebase.firestore
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUserGreeting()
        setupMainFeatureCards()
        setupBannerSlider()
        animateEntrance()
    }

    private fun setupUserGreeting() {
        val currentUser = firebaseAuth.currentUser
        val greeting = getTimeBasedGreeting()

        if (currentUser != null) {
            val userName = when {
                !currentUser.displayName.isNullOrBlank() -> currentUser.displayName
                !currentUser.email.isNullOrBlank() -> currentUser.email?.substringBefore("@")
                else -> "User"
            }
            binding.tvGreeting.text = "$greeting, $userName!"
        } else {
            binding.tvGreeting.text = "$greeting, Guest!"
        }
    }

    private fun getTimeBasedGreeting(): String {
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    private fun setupMainFeatureCards() {
        // Partner Registration Card
        binding.cardProgress.setOnClickListener {
            animateCardClickWithScale(it) {
                startActivity(Intent(requireContext(), RegistrationFormActivity::class.java))
            }
        }

        // Student Desk Card
        binding.cardStudyMaterial.setOnClickListener {
            animateCardClickWithBounce(it) {
                startActivity(Intent(requireContext(), StudentDeskActivity::class.java))
            }
        }

        // AI Tutor Card
        binding.cardLearnAi.setOnClickListener {
            animateCardClickWithPulse(it) {
                startActivity(Intent(requireContext(), LearnWithAIActivity::class.java))
            }
        }

        // Teacher Hotline Card
        binding.cardTeacherHotline.setOnClickListener {
            animateCardClickWithRotation(it) {
                startActivity(Intent(requireContext(), TeacherHotlineActivity::class.java))
            }
        }
    }

    // Enhanced animation for Partner Registration Card - Scale with shadow effect
    private fun animateCardClickWithScale(view: View, action: () -> Unit) {
        val scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.92f)
        val scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.92f)
        val elevation = ObjectAnimator.ofFloat(view, "elevation", view.elevation, view.elevation * 0.5f)

        val scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 0.92f, 1.05f, 1f)
        val scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 0.92f, 1.05f, 1f)
        val elevationUp = ObjectAnimator.ofFloat(view, "elevation", view.elevation * 0.5f, view.elevation)

        val downSet = AnimatorSet()
        downSet.playTogether(scaleDownX, scaleDownY, elevation)
        downSet.duration = 150
        downSet.interpolator = AccelerateDecelerateInterpolator()

        val upSet = AnimatorSet()
        upSet.playTogether(scaleUpX, scaleUpY, elevationUp)
        upSet.duration = 300
        upSet.interpolator = OvershootInterpolator(1.2f)

        downSet.start()
        Handler(Looper.getMainLooper()).postDelayed({
            upSet.start()
            action()
        }, 150)
    }

    // Enhanced animation for Student Desk Card - Bounce effect
    private fun animateCardClickWithBounce(view: View, action: () -> Unit) {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.9f, 1.1f, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.9f, 1.1f, 1f)
        val rotation = ObjectAnimator.ofFloat(view, "rotation", 0f, -2f, 2f, 0f)

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleX, scaleY, rotation)
        animatorSet.duration = 400
        animatorSet.interpolator = BounceInterpolator()
        animatorSet.start()

        Handler(Looper.getMainLooper()).postDelayed({
            action()
        }, 200)
    }

    // Enhanced animation for AI Tutor Card - Pulse with glow effect
    private fun animateCardClickWithPulse(view: View, action: () -> Unit) {
        val scaleX1 = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.08f)
        val scaleY1 = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.08f)
        val alpha1 = ObjectAnimator.ofFloat(view, "alpha", 1f, 0.8f)

        val scaleX2 = ObjectAnimator.ofFloat(view, "scaleX", 1.08f, 0.95f)
        val scaleY2 = ObjectAnimator.ofFloat(view, "scaleY", 1.08f, 0.95f)
        val alpha2 = ObjectAnimator.ofFloat(view, "alpha", 0.8f, 1f)

        val scaleX3 = ObjectAnimator.ofFloat(view, "scaleX", 0.95f, 1f)
        val scaleY3 = ObjectAnimator.ofFloat(view, "scaleY", 0.95f, 1f)

        val pulse1 = AnimatorSet()
        pulse1.playTogether(scaleX1, scaleY1, alpha1)
        pulse1.duration = 100

        val pulse2 = AnimatorSet()
        pulse2.playTogether(scaleX2, scaleY2, alpha2)
        pulse2.duration = 150

        val pulse3 = AnimatorSet()
        pulse3.playTogether(scaleX3, scaleY3)
        pulse3.duration = 100

        pulse1.start()
        Handler(Looper.getMainLooper()).postDelayed({
            pulse2.start()
        }, 100)
        Handler(Looper.getMainLooper()).postDelayed({
            pulse3.start()
            action()
        }, 250)
    }

    // Enhanced animation for Teacher Hotline Card - Rotation with shake
    private fun animateCardClickWithRotation(view: View, action: () -> Unit) {
        val rotation1 = ObjectAnimator.ofFloat(view, "rotation", 0f, 5f)
        val rotation2 = ObjectAnimator.ofFloat(view, "rotation", 5f, -5f)
        val rotation3 = ObjectAnimator.ofFloat(view, "rotation", -5f, 0f)

        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f, 1.02f, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f, 1.02f, 1f)

        val translationX1 = ObjectAnimator.ofFloat(view, "translationX", 0f, 5f)
        val translationX2 = ObjectAnimator.ofFloat(view, "translationX", 5f, -5f)
        val translationX3 = ObjectAnimator.ofFloat(view, "translationX", -5f, 0f)

        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(
            AnimatorSet().apply { playTogether(rotation1, translationX1) },
            AnimatorSet().apply { playTogether(rotation2, translationX2) },
            AnimatorSet().apply { playTogether(rotation3, translationX3, scaleX, scaleY) }
        )
        animatorSet.duration = 300
        animatorSet.interpolator = AccelerateDecelerateInterpolator()
        animatorSet.start()

        Handler(Looper.getMainLooper()).postDelayed({
            action()
        }, 150)
    }

    private fun animateEntrance() {
        val cards = listOf(
            binding.cardProgress,
            binding.cardStudyMaterial,
            binding.cardLearnAi,
            binding.cardTeacherHotline
        )

        // Animate greeting card first
        (binding.tvGreeting.parent as? View)?.let { greetingCard ->
            greetingCard.alpha = 0f
            greetingCard.translationY = -50f
            greetingCard.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setInterpolator(OvershootInterpolator(0.8f))
                .start()
        }

        // Animate main feature cards with staggered timing
        cards.forEachIndexed { index, card ->
            card.alpha = 0f
            card.translationY = 100f
            card.scaleX = 0.8f
            card.scaleY = 0.8f

            val fadeIn = ObjectAnimator.ofFloat(card, "alpha", 0f, 1f)
            val slideUp = ObjectAnimator.ofFloat(card, "translationY", 100f, 0f)
            val scaleX = ObjectAnimator.ofFloat(card, "scaleX", 0.8f, 1f)
            val scaleY = ObjectAnimator.ofFloat(card, "scaleY", 0.8f, 1f)

            val animatorSet = AnimatorSet()
            animatorSet.playTogether(fadeIn, slideUp, scaleX, scaleY)
            animatorSet.duration = 700
            animatorSet.interpolator = OvershootInterpolator(1.0f)
            animatorSet.startDelay = (index * 150 + 300).toLong() // Start after greeting
            animatorSet.start()
        }
    }

    private fun setupBannerSlider() {
        Log.d(TAG, "Setting up banner slider...")

        firestore.collection("banners")
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Log.w(TAG, "No banners found, using test banners")
//                    createTestBanners()
                    return@addOnSuccessListener
                }

                val bannerItems = mutableListOf<BannerItem>()
                for (document in result.documents) {
                    val imageUrl = document.getString("imageUrl") ?: document.getString("image")
                    val text = document.getString("text") ?: document.getString("title") ?: "Default Title"
                    val redirectUrl = document.getString("redirectUrl") ?: ""

                    if (!imageUrl.isNullOrBlank()) {
                        bannerItems.add(
                            BannerItem(
                                imageUrl = imageUrl,
                                text = text,
                                redirectUrl = redirectUrl
                            )
                        )
                    }
                }

                if (bannerItems.isNotEmpty()) {
                    setupBannerViewPager(bannerItems)
                } else {
//                    createTestBanners()
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to load banners", exception)
//                createTestBanners()
            }
    }

    private fun setupBannerViewPager(bannerItems: List<BannerItem>) {
        val bannerAdapter = BannerAdapter(bannerItems) { bannerItem ->
            Log.d(TAG, "Banner clicked: ${bannerItem.text}")
            // Handle banner click
        }

        binding.bannerViewPager.adapter = bannerAdapter
        binding.bannerViewPager.visibility = View.VISIBLE
        binding.bannerIndicators.visibility = View.VISIBLE

        // Add smooth page transformer
        binding.bannerViewPager.setPageTransformer { page, position ->
            when {
                position < -1 -> page.alpha = 0f
                position <= 1 -> {
                    page.alpha = 1f
                    page.scaleX = 0.95f + (1 - kotlin.math.abs(position)) * 0.05f
                    page.scaleY = 0.95f + (1 - kotlin.math.abs(position)) * 0.05f
                }
                else -> page.alpha = 0f
            }
        }

        setupIndicators(bannerItems.size)
        setCurrentIndicator(0)
        setupAutoScroll(bannerItems.size)

        binding.bannerViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)
            }
        })
    }

//    private fun createTestBanners() {
//        val testBanners = listOf(
//            BannerItem(
//                imageUrl = "https://images.unsplash.com/photo-1619296330981-b882d7e93425?q=80&w=870&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
//                text = "Welcome to UHA Learning",
//                redirectUrl = "https://uha360.in/"
//            ),
//            BannerItem(
//                imageUrl = "https://images.unsplash.com/photo-1619296330981-b882d7e93425?q=80&w=870&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
//                text = "Welcome to UHA Learning",
//                redirectUrl = "https://uha360.in/"
//            ),
//            BannerItem(
//                imageUrl = "https://plus.unsplash.com/premium_photo-1682888442461-e6eab82a056d?q=80&w=953&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
//                text = "Study with AI",
//                redirectUrl = "https://uha360.in/"
//            )
//        )
//        setupBannerViewPager(testBanners)
//    }

    private fun setupAutoScroll(itemCount: Int) {
        bannerHandler = Handler(Looper.getMainLooper())
        bannerRunnable = object : Runnable {
            override fun run() {
                if (itemCount > 1 && isAdded && view != null) {
                    val currentItem = binding.bannerViewPager.currentItem
                    val nextItem = (currentItem + 1) % itemCount
                    binding.bannerViewPager.setCurrentItem(nextItem, true)
                    bannerHandler.postDelayed(this, 4000)
                }
            }
        }
        if (itemCount > 1) {
            bannerHandler.postDelayed(bannerRunnable, 4000)
        }
    }

    private fun setupIndicators(count: Int) {
        binding.bannerIndicators.removeAllViews()
        if (count <= 1) return

        val layoutParams = LinearLayout.LayoutParams(
            dpToPx(12f).toInt(),
            dpToPx(12f).toInt()
        )
        layoutParams.setMargins(dpToPx(2f).toInt(), 0, dpToPx(2f).toInt(), 0)
    }

    private fun setCurrentIndicator(position: Int) {
        val childCount = binding.bannerIndicators.childCount
        for (i in 0 until childCount) {
            val child = binding.bannerIndicators.getChildAt(i)
            child?.background = ContextCompat.getDrawable(
                requireContext(),
                if (i == position) R.drawable.bg_indicator_active else R.drawable.bg_indicator_inactive
            )
        }
    }

    private fun dpToPx(dp: Float): Float {
        return dp * resources.displayMetrics.density
    }

    override fun onPause() {
        super.onPause()
        if (::bannerHandler.isInitialized && ::bannerRunnable.isInitialized) {
            bannerHandler.removeCallbacks(bannerRunnable)
        }
    }

    override fun onResume() {
        super.onResume()
        if (::bannerHandler.isInitialized && ::bannerRunnable.isInitialized) {
            bannerHandler.postDelayed(bannerRunnable, 4000)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::bannerHandler.isInitialized && ::bannerRunnable.isInitialized) {
            bannerHandler.removeCallbacks(bannerRunnable)
        }
        _binding = null
    }
}