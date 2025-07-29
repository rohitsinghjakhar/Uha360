package com.dawnbellsuha.uha.utils


import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.dawnbellsuha.uha.R
import java.util.*

object ProfileUtils {

    /**
     * Generate initials from full name
     */
    fun getInitials(fullName: String): String {
        if (fullName.isBlank()) return "U"

        val names = fullName.trim().split(" ")
        return when {
            names.size == 1 -> names[0].take(1).uppercase()
            names.size >= 2 -> "${names[0].take(1)}${names[1].take(1)}".uppercase()
            else -> "U"
        }
    }

    /**
     * Create a circular drawable with initials
     */
    fun createInitialsDrawable(context: Context, initials: String, size: Int = 120): Drawable {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background circle
        val backgroundPaint = Paint().apply {
            color = ContextCompat.getColor(context, R.color.bright_blue)
            isAntiAlias = true
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, backgroundPaint)

        // Text
        val textPaint = Paint().apply {
            color = ContextCompat.getColor(context, R.color.white)
            textSize = size * 0.4f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        val textBounds = Rect()
        textPaint.getTextBounds(initials, 0, initials.length, textBounds)
        val textHeight = textBounds.height()

        canvas.drawText(
            initials,
            size / 2f,
            size / 2f + textHeight / 2f,
            textPaint
        )

        return BitmapDrawable(context.resources, bitmap)
    }

    /**
     * Validate email format
     */
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Validate phone number format (Indian format)
     */
    fun isValidPhoneNumber(phone: String): Boolean {
        val cleanPhone = phone.replace("[^\\d]".toRegex(), "")
        return cleanPhone.length == 10 && (cleanPhone.startsWith("6") ||
                cleanPhone.startsWith("7") || cleanPhone.startsWith("8") ||
                cleanPhone.startsWith("9"))
    }

    /**
     * Format phone number for display
     */
    fun formatPhoneNumber(phone: String): String {
        val cleanPhone = phone.replace("[^\\d]".toRegex(), "")
        return if (cleanPhone.length == 10) {
            "+91 ${cleanPhone.substring(0, 5)} ${cleanPhone.substring(5)}"
        } else {
            "+91 $phone"
        }
    }

    /**
     * Validate age
     */
    fun isValidAge(age: String): Boolean {
        val ageInt = age.toIntOrNull()
        return ageInt != null && ageInt in 10..100
    }

    /**
     * Get list of Indian states
     */
    fun getIndianStates(): List<String> {
        return listOf(
            "Select State",
            "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh",
            "Goa", "Gujarat", "Haryana", "Himachal Pradesh", "Jharkhand", "Karnataka",
            "Kerala", "Madhya Pradesh", "Maharashtra", "Manipur", "Meghalaya", "Mizoram",
            "Nagaland", "Odisha", "Punjab", "Rajasthan", "Sikkim", "Tamil Nadu",
            "Telangana", "Tripura", "Uttar Pradesh", "Uttarakhand", "West Bengal",
            "Delhi", "Chandigarh", "Dadra and Nagar Haveli", "Daman and Diu",
            "Lakshadweep", "Puducherry", "Andaman and Nicobar Islands", "Ladakh", "Jammu and Kashmir"
        )
    }

    /**
     * Get list of classes and exams
     */
    fun getClassExams(): List<String> {
        return listOf(
            "Select Class/Exam",
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
    }

    /**
     * Get greeting based on time of day
     */
    fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..20 -> "Good Evening"
            else -> "Good Night"
        }
    }

    /**
     * Get random motivational quote for students
     */
    fun getMotivationalQuote(): String {
        val quotes = listOf(
            "Success is not final, failure is not fatal: it is the courage to continue that counts.",
            "The only impossible journey is the one you never begin.",
            "Education is the most powerful weapon which you can use to change the world.",
            "The future belongs to those who believe in the beauty of their dreams.",
            "Success is the sum of small efforts repeated day in and day out.",
            "Don't watch the clock; do what it does. Keep going.",
            "The expert in anything was once a beginner.",
            "It always seems impossible until it's done.",
            "You are never too old to set another goal or to dream a new dream.",
            "Believe you can and you're halfway there."
        )
        return quotes.random()
    }

    /**
     * Calculate completion percentage of profile
     */
    fun calculateProfileCompletion(userProfile: com.dawnbellsuha.uha.models.UserProfile): Int {
        var completedFields = 0
        val totalFields = 8

        if (userProfile.fullName.isNotBlank()) completedFields++
        if (userProfile.age.isNotBlank()) completedFields++
        if (userProfile.mobile.isNotBlank()) completedFields++
        if (userProfile.email.isNotBlank()) completedFields++
        if (userProfile.state.isNotBlank() && userProfile.state != "Select State") completedFields++
        if (userProfile.classExam.isNotBlank() && userProfile.classExam != "Select Class/Exam") completedFields++
        if (userProfile.address.isNotBlank()) completedFields++
        if (userProfile.profilePictureUrl.isNotBlank()) completedFields++

        return (completedFields * 100) / totalFields
    }

    /**
     * Get color based on profile completion percentage
     */
    fun getCompletionColor(context: Context, percentage: Int): Int {
        return when {
            percentage >= 80 -> ContextCompat.getColor(context, R.color.success_color)
            percentage >= 50 -> ContextCompat.getColor(context, R.color.warning_color)
            else -> ContextCompat.getColor(context, R.color.error_color)
        }
    }
}