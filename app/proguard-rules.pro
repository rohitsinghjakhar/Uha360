# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in ${SDK_DIR}/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.

# ----------------------------------
# Standard Android optimization rules
# ----------------------------------

-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose

# ----------------------------------
# Keep important annotations
# ----------------------------------

-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# ----------------------------------
# Firebase and Google Play Services
# ----------------------------------

-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-keepnames class com.google.firebase.**
-keepnames class com.google.android.gms.**
-keepattributes Signature
-keepattributes *Annotation*

-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**
-dontwarn org.apache.http.**
-dontwarn android.net.http.**

# For Firebase Authentication
-keep class com.google.firebase.auth.** { *; }
-keep class com.google.firebase.auth.internal.** { *; }

# For Firebase Firestore
-keep class com.google.firebase.firestore.** { *; }
-keep class com.google.firestore.** { *; }

# For Google Sign-In
-keep class com.google.android.gms.auth.** { *; }
-keep class com.google.android.gms.tasks.** { *; }

# ----------------------------------
# Application specific rules
# ----------------------------------

# Keep all classes that might be accessed via reflection
-keep public class com.uhadawnbells.uha.** { *; }

# Keep View bindings
-keep class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(***);
}

# Keep parcelable classes
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ----------------------------------
# Debugging support
# ----------------------------------

# Uncomment to preserve line numbers for debugging
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ----------------------------------
# WebView support (if needed)
# ----------------------------------

#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}