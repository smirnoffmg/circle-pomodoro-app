
// Declare all plugins here with apply false, do not apply in root
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    id("com.google.gms.google-services") version "4.4.3" apply false
    alias(libs.plugins.hilt.android) apply false // Hilt
    alias(libs.plugins.ksp) apply false // KSP
    alias(libs.plugins.room) apply false
}
