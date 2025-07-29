package com.dawnbellsuha.uha

import android.app.Application
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.FirebaseApp

class App : Application() {

    companion object {
        private const val TAG = "AppClass"
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Initialize Google APIs
        initializeGoogleApi()
    }

    private fun initializeGoogleApi() {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this)
        if (resultCode == ConnectionResult.SUCCESS) {
            Log.d(TAG, "Google Play Services are available.")
        } else {
            Log.e(TAG, "Google Play Services are unavailable or need an update. Result code: $resultCode")
            // Optionally, you can try to make them available if they are not.
            // googleApiAvailability.makeGooglePlayServicesAvailable(this)
        }
    }
}