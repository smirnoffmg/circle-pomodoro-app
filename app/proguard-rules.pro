# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep Room database classes
-keep class com.smirnoffmg.pomodorotimer.data.local.db.** { *; }
-keep class com.smirnoffmg.pomodorotimer.data.local.db.entity.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager { *; }

# Keep WorkManager classes
-keep class androidx.work.impl.** { *; }

# Keep Compose classes
-keep class androidx.compose.** { *; }

# Keep Firebase classes
-keep class com.google.firebase.** { *; }

# Keep Glance widget classes
-keep class androidx.glance.** { *; }

# Keep domain models
-keep class com.smirnoffmg.pomodorotimer.domain.model.** { *; }

# Keep repository interfaces
-keep interface com.smirnoffmg.pomodorotimer.domain.repository.** { *; }

# Keep use case classes
-keep class com.smirnoffmg.pomodorotimer.domain.usecase.** { *; }
