package com.uhadawnbells.uha.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object GoogleSheetsHelper {

    // Single Web App URL for all forms - replace with actual Google Apps Script Web App URL
    private const val WEB_APP_URL = "https://script.google.com/macros/s/AKfycbwmAVTFgF6VGuShhWdu1KkFKpYbodHl03x0HyZ6cHJjNJEMY5dFAREliK3pC6VGacRA/exec"

    /**
     * Submit Registration Form Data
     */
    suspend fun submitRegistrationFormData(data: HashMap<String, String>): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Ensure form_type is set for proper routing
                data["form_type"] = "Registration Form"
                submitToGoogleSheets(WEB_APP_URL, data)
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    /**
     * Submit AI Inquiry Data
     */
    suspend fun submitAiInquiryData(data: HashMap<String, String>): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Ensure inquiry_type is set for proper routing
                data["inquiry_type"] = "Know UHA AI"
                submitToGoogleSheets(WEB_APP_URL, data)
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    /**
     * Submit Teacher Booking Data
     */
    suspend fun submitTeacherBookingData(data: HashMap<String, String>): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Ensure request_type is set for proper routing
                data["request_type"] = "Teacher Hotline Booking"
                submitToGoogleSheets(WEB_APP_URL, data)
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    /**
     * Submit data to Google Sheets via Apps Script
     */
    private fun submitToGoogleSheets(webAppUrl: String, data: HashMap<String, String>): Boolean {
        return try {
            val url = URL(webAppUrl)
            val connection = url.openConnection() as HttpURLConnection

            connection.apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                setRequestProperty("User-Agent", "UHA-Android-App/1.0")
                setRequestProperty("Accept", "text/plain")
                connectTimeout = 15000
                readTimeout = 15000
                instanceFollowRedirects = true
            }

            // Build POST data
            val postData = buildPostData(data)

            // Log the submission for debugging (remove in production)
            println("Submitting to: $webAppUrl")
            println("Data: $postData")

            // Send data
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(postData)
                writer.flush()
            }

            // Check response
            val responseCode = connection.responseCode
            println("Response Code: $responseCode")

            // Read response for debugging
            val response = if (responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.bufferedReader().readText()
            } else {
                connection.errorStream?.bufferedReader()?.readText() ?: "No error message"
            }
            println("Response: $response")

            val success = responseCode == HttpURLConnection.HTTP_OK ||
                    responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                    responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                    response.contains("Success", ignoreCase = true)

            connection.disconnect()
            success

        } catch (e: Exception) {
            e.printStackTrace()
            println("Network error: ${e.message}")
            false
        }
    }

    /**
     * Build URL-encoded POST data from HashMap
     */
    private fun buildPostData(data: HashMap<String, String>): String {
        val postData = StringBuilder()

        data.forEach { (key, value) ->
            if (postData.isNotEmpty()) {
                postData.append("&")
            }
            postData.append(URLEncoder.encode(key, "UTF-8"))
            postData.append("=")
            postData.append(URLEncoder.encode(value, "UTF-8"))
        }

        return postData.toString()
    }

    /**
     * Test connection to Google Apps Script (for debugging)
     */
    suspend fun testConnection(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val testData = hashMapOf(
                    "test" to "true",
                    "timestamp" to java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date()),
                    "form_type" to "Test Connection"
                )
                submitToGoogleSheets(WEB_APP_URL, testData)
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}
