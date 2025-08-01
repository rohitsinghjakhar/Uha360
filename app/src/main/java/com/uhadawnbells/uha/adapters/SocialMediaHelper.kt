package com.uhadawnbells.uha.adapters

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.uhadawnbells.uha.R
import com.google.android.material.snackbar.Snackbar
import androidx.core.net.toUri

class SocialMediaHelper(private val context: AppCompatActivity) {

    fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            context.startActivity(intent)
            showSnackbar(context.getString(R.string.opening_social_media))
        } catch (e: Exception) {
            showSnackbar(context.getString(R.string.error_social_link))
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(
            context.findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_SHORT
        ).show()
    }
}