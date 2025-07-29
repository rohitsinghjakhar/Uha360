// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    //classpath("com.google.gms:google-services:4.4.3")
    id("com.google.gms.google-services") version "4.4.3" apply false // Downgraded to 4.4.1
    id("androidx.navigation.safeargs.kotlin") version "2.9.2" apply false // Downgraded to 2.7.7
}