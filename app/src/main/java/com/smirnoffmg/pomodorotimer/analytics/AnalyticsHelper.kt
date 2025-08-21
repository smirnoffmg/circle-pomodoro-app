package com.smirnoffmg.pomodorotimer.analytics

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val firebaseAnalytics = FirebaseAnalytics.getInstance(context)

    fun logScreenView(screenName: String) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        }
    }
}
