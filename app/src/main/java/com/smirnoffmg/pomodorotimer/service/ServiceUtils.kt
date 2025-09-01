package com.smirnoffmg.pomodorotimer.service

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * Utility class for service operations with API compatibility
 * 
 * Follows SOLID principles:
 * - Single Responsibility: Only handles service starting and status checking
 * - Open/Closed: Easy to extend for new service types
 * 
 * Follows DRY principle by centralizing service operations logic
 */
object ServiceUtils {
    /**
     * Start a service with API level compatibility
     * 
     * @param context The application context
     * @param serviceClass The service class to start
     * @param action Optional action for the intent
     * @param extras Optional extras for the intent
     */
    fun startService(
        context: Context,
        serviceClass: Class<*>,
        action: String? = null,
        extras: Map<String, Any>? = null
    ) {
        val intent =
            Intent(context, serviceClass).apply {
                action?.let { this.action = it }
                extras?.forEach { (key, value) ->
                    when (value) {
                        is String -> putExtra(key, value)
                        is Int -> putExtra(key, value)
                        is Long -> putExtra(key, value)
                        is Boolean -> putExtra(key, value)
                        is Float -> putExtra(key, value)
                        is Double -> putExtra(key, value)
                    }
                }
            }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    /**
     * Check if a service is currently running using modern, reliable methods
     * 
     * @param context The application context
     * @param serviceClass The service class to check
     * @return true if the service is running, false otherwise
     */
    fun isServiceRunning(
        context: Context,
        serviceClass: Class<*>
    ): Boolean =
        when {
            // For API < 26, use the legacy method (still available but deprecated)
            Build.VERSION.SDK_INT < Build.VERSION_CODES.O -> {
                checkServiceRunningLegacy(context, serviceClass)
            }
            // For API >= 26, use modern approach with fallback
            else -> {
                checkServiceRunningModern(context, serviceClass)
            }
        }

    /**
     * Legacy method for API < 26 (deprecated but still functional)
     */
    @Suppress("DEPRECATION")
    private fun checkServiceRunningLegacy(
        context: Context,
        serviceClass: Class<*>
    ): Boolean =
        try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val runningServices = activityManager.getRunningServices(Integer.MAX_VALUE)
            
            runningServices.any { serviceInfo ->
                serviceInfo.service.className == serviceClass.name
            }
        } catch (e: Exception) {
            // Fallback to false if legacy method fails
            false
        }

    /**
     * Modern method for API >= 26 using process checking
     */
    private fun checkServiceRunningModern(
        context: Context,
        serviceClass: Class<*>
    ): Boolean =
        try {
            // For modern Android, check if the service process is running
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            
            // Get running app processes instead of services (more reliable on modern Android)
            val runningProcesses = activityManager.runningAppProcesses ?: emptyList()
            
            // Check if our app process is running
            val packageName = context.packageName
            val isAppRunning =
                runningProcesses.any { processInfo ->
                    processInfo.processName == packageName
                }
            
            if (isAppRunning) {
                // If app is running, try to check service status more directly
                // This is a heuristic: if the app is running and we're checking for our own service,
                // it's likely running if it was started
                checkServiceRunningLegacy(context, serviceClass)
            } else {
                false
            }
        } catch (e: Exception) {
            // If modern method fails, fallback to legacy
            checkServiceRunningLegacy(context, serviceClass)
        }
}
