package com.smirnoffmg.pomodorotimer.notification

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationPermissionManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        companion object {
            private const val NOTIFICATION_PERMISSION = Manifest.permission.POST_NOTIFICATIONS
        }

        fun isNotificationPermissionGranted(): Boolean =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    NOTIFICATION_PERMISSION,
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                // For versions below API 33, check if notifications are enabled
                NotificationManagerCompat.from(context).areNotificationsEnabled()
            }

        fun areNotificationsEnabled(): Boolean = NotificationManagerCompat.from(context).areNotificationsEnabled()

        fun shouldShowPermissionRationale(activity: Activity): Boolean =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && activity is AppCompatActivity) {
                activity.shouldShowRequestPermissionRationale(NOTIFICATION_PERMISSION)
            } else {
                false
            }

        fun createPermissionLauncher(
            activity: AppCompatActivity,
            onPermissionResult: (Boolean) -> Unit,
        ): ActivityResultLauncher<String> =
            activity.registerForActivityResult(
                ActivityResultContracts.RequestPermission(),
            ) { isGranted ->
                onPermissionResult(isGranted)
            }

        fun requestNotificationPermission(permissionLauncher: ActivityResultLauncher<String>) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(NOTIFICATION_PERMISSION)
            }
        }

        fun checkAndRequestPermissionIfNeeded(
            activity: AppCompatActivity,
            onPermissionGranted: () -> Unit,
            onPermissionDenied: () -> Unit = {},
            onShowRationale: () -> Unit = {},
        ) {
            when {
                isNotificationPermissionGranted() -> {
                    onPermissionGranted()
                }
                shouldShowPermissionRationale(activity) -> {
                    onShowRationale()
                }
                else -> {
                    val permissionLauncher =
                        createPermissionLauncher(activity) { isGranted ->
                            if (isGranted) {
                                onPermissionGranted()
                            } else {
                                onPermissionDenied()
                            }
                        }
                    requestNotificationPermission(permissionLauncher)
                }
            }
        }

        data class PermissionStatus(
            val isGranted: Boolean,
            val areNotificationsEnabled: Boolean,
            val needsRuntimePermission: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU,
        )

        fun getPermissionStatus(): PermissionStatus =
            PermissionStatus(
                isGranted = isNotificationPermissionGranted(),
                areNotificationsEnabled = areNotificationsEnabled(),
                needsRuntimePermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU,
            )
    }
