package com.smirnoffmg.pomodorotimer.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsHelper
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val firebaseAnalytics = FirebaseAnalytics.getInstance(context)

        fun logScreenView(screenName: String) {
            val bundle =
                Bundle().apply {
                    putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
                }
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
        }
    }
