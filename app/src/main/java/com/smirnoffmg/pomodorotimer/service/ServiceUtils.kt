package com.smirnoffmg.pomodorotimer.service

import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * Utility class for service operations with API compatibility
 * 
 * Follows SOLID principles:
 * - Single Responsibility: Only handles service starting
 * - Open/Closed: Easy to extend for new service types
 * 
 * Follows DRY principle by centralizing service starting logic
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
}
