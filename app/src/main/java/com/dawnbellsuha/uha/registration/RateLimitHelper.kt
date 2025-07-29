package com.dawnbellsuha.uha.registration

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.util.concurrent.TimeUnit

object RateLimitHelper {

    private const val TAG = "RateLimitHelper"
    private const val PREFS_NAME = "auth_rate_limit"
    private const val KEY_PHONE_LAST_REQUEST = "phone_last_request"
    private const val KEY_PHONE_REQUEST_COUNT = "phone_request_count"
    private const val KEY_EMAIL_LAST_REQUEST = "email_last_request"
    private const val KEY_EMAIL_REQUEST_COUNT = "email_request_count"

    // Rate limit settings
    private const val PHONE_COOLDOWN_MINUTES = 5L // 5 minutes cooldown for phone auth
    private const val EMAIL_COOLDOWN_MINUTES = 2L // 2 minutes cooldown for email auth
    private const val MAX_PHONE_REQUESTS_PER_HOUR = 5
    private const val MAX_EMAIL_REQUESTS_PER_HOUR = 10

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Check if phone authentication request is allowed
     */
    fun canRequestPhoneOtp(context: Context): Pair<Boolean, String?> {
        val prefs = getPrefs(context)
        val lastRequestTime = prefs.getLong(KEY_PHONE_LAST_REQUEST, 0)
        val requestCount = prefs.getInt(KEY_PHONE_REQUEST_COUNT, 0)
        val currentTime = System.currentTimeMillis()

        // Reset count if more than 1 hour has passed
        if (currentTime - lastRequestTime > TimeUnit.HOURS.toMillis(1)) {
            prefs.edit()
                .putInt(KEY_PHONE_REQUEST_COUNT, 0)
                .putLong(KEY_PHONE_LAST_REQUEST, 0)
                .apply()
            return Pair(true, null)
        }

        // Check if cooldown period has passed
        val cooldownTime = TimeUnit.MINUTES.toMillis(PHONE_COOLDOWN_MINUTES)
        if (currentTime - lastRequestTime < cooldownTime) {
            val remainingTime = cooldownTime - (currentTime - lastRequestTime)
            val remainingMinutes = TimeUnit.MILLISECONDS.toMinutes(remainingTime)
            val remainingSeconds = TimeUnit.MILLISECONDS.toSeconds(remainingTime) % 60

            return Pair(false, "Please wait ${remainingMinutes}:${String.format("%02d", remainingSeconds)} before requesting another OTP")
        }

        // Check if maximum requests per hour exceeded
        if (requestCount >= MAX_PHONE_REQUESTS_PER_HOUR) {
            val remainingTime = TimeUnit.HOURS.toMillis(1) - (currentTime - lastRequestTime)
            val remainingMinutes = TimeUnit.MILLISECONDS.toMinutes(remainingTime)

            return Pair(false, "Too many OTP requests. Please try again after $remainingMinutes minutes")
        }

        return Pair(true, null)
    }

    /**
     * Record a phone authentication request
     */
    fun recordPhoneOtpRequest(context: Context) {
        val prefs = getPrefs(context)
        val currentTime = System.currentTimeMillis()
        val lastRequestTime = prefs.getLong(KEY_PHONE_LAST_REQUEST, 0)
        val requestCount = prefs.getInt(KEY_PHONE_REQUEST_COUNT, 0)

        // Reset count if more than 1 hour has passed
        val newCount = if (currentTime - lastRequestTime > TimeUnit.HOURS.toMillis(1)) {
            1
        } else {
            requestCount + 1
        }

        prefs.edit()
            .putLong(KEY_PHONE_LAST_REQUEST, currentTime)
            .putInt(KEY_PHONE_REQUEST_COUNT, newCount)
            .apply()

        Log.d(TAG, "📱 Phone OTP request recorded. Count: $newCount")
    }

    /**
     * Check if email authentication request is allowed
     */
    fun canRequestEmailAuth(context: Context): Pair<Boolean, String?> {
        val prefs = getPrefs(context)
        val lastRequestTime = prefs.getLong(KEY_EMAIL_LAST_REQUEST, 0)
        val requestCount = prefs.getInt(KEY_EMAIL_REQUEST_COUNT, 0)
        val currentTime = System.currentTimeMillis()

        // Reset count if more than 1 hour has passed
        if (currentTime - lastRequestTime > TimeUnit.HOURS.toMillis(1)) {
            prefs.edit()
                .putInt(KEY_EMAIL_REQUEST_COUNT, 0)
                .putLong(KEY_EMAIL_LAST_REQUEST, 0)
                .apply()
            return Pair(true, null)
        }

        // Check if cooldown period has passed
        val cooldownTime = TimeUnit.MINUTES.toMillis(EMAIL_COOLDOWN_MINUTES)
        if (currentTime - lastRequestTime < cooldownTime) {
            val remainingTime = cooldownTime - (currentTime - lastRequestTime)
            val remainingSeconds = TimeUnit.MILLISECONDS.toSeconds(remainingTime)

            return Pair(false, "Please wait $remainingSeconds seconds before trying again")
        }

        // Check if maximum requests per hour exceeded
        if (requestCount >= MAX_EMAIL_REQUESTS_PER_HOUR) {
            val remainingTime = TimeUnit.HOURS.toMillis(1) - (currentTime - lastRequestTime)
            val remainingMinutes = TimeUnit.MILLISECONDS.toMinutes(remainingTime)

            return Pair(false, "Too many login attempts. Please try again after $remainingMinutes minutes")
        }

        return Pair(true, null)
    }

    /**
     * Record an email authentication request
     */
    fun recordEmailAuthRequest(context: Context) {
        val prefs = getPrefs(context)
        val currentTime = System.currentTimeMillis()
        val lastRequestTime = prefs.getLong(KEY_EMAIL_LAST_REQUEST, 0)
        val requestCount = prefs.getInt(KEY_EMAIL_REQUEST_COUNT, 0)

        // Reset count if more than 1 hour has passed
        val newCount = if (currentTime - lastRequestTime > TimeUnit.HOURS.toMillis(1)) {
            1
        } else {
            requestCount + 1
        }

        prefs.edit()
            .putLong(KEY_EMAIL_LAST_REQUEST, currentTime)
            .putInt(KEY_EMAIL_REQUEST_COUNT, newCount)
            .apply()

        Log.d(TAG, "📧 Email auth request recorded. Count: $newCount")
    }

    /**
     * Clear rate limit data (use for testing or when user successfully authenticates)
     */
    fun clearRateLimitData(context: Context) {
        getPrefs(context).edit().clear().apply()
        Log.d(TAG, "🧹 Rate limit data cleared")
    }

    /**
     * Get remaining cooldown time for phone OTP in seconds
     */
    fun getPhoneOtpCooldownRemaining(context: Context): Long {
        val prefs = getPrefs(context)
        val lastRequestTime = prefs.getLong(KEY_PHONE_LAST_REQUEST, 0)
        val currentTime = System.currentTimeMillis()
        val cooldownTime = TimeUnit.MINUTES.toMillis(PHONE_COOLDOWN_MINUTES)

        if (lastRequestTime == 0L) return 0L

        val elapsed = currentTime - lastRequestTime
        return if (elapsed < cooldownTime) {
            TimeUnit.MILLISECONDS.toSeconds(cooldownTime - elapsed)
        } else {
            0L
        }
    }

    /**
     * Check if device might be temporarily blocked by Firebase
     */
    fun shouldShowAlternativeOptions(context: Context): Boolean {
        val prefs = getPrefs(context)
        val phoneRequestCount = prefs.getInt(KEY_PHONE_REQUEST_COUNT, 0)
        val emailRequestCount = prefs.getInt(KEY_EMAIL_REQUEST_COUNT, 0)

        return phoneRequestCount >= MAX_PHONE_REQUESTS_PER_HOUR ||
                emailRequestCount >= MAX_EMAIL_REQUESTS_PER_HOUR
    }

    /**
     * Get user-friendly message for rate limiting
     */
    fun getRateLimitMessage(context: Context): String? {
        val phoneCheck = canRequestPhoneOtp(context)
        val emailCheck = canRequestEmailAuth(context)

        return when {
            !phoneCheck.first && !emailCheck.first ->
                "Multiple authentication attempts detected. Please try again later or contact support."
            !phoneCheck.first ->
                phoneCheck.second
            !emailCheck.first ->
                emailCheck.second
            else -> null
        }
    }
}