
// Declare all plugins here with apply false, do not apply in root
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.google.gms.google.services) apply false
    alias(libs.plugins.google.firebase.crashlytics) apply false
    alias(libs.plugins.hilt.android) apply false // Hilt
    alias(libs.plugins.ksp) apply false         // KSP
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.room) apply false
}
