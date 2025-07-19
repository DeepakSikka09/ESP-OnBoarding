package com.ecomexpress.oneBoarding

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class OneBoarding : Application() {

    override fun onCreate() {
        super.onCreate()
        Firebase.initialize(this)
        FirebaseAnalytics.getInstance(this)
        FirebaseApp.initializeApp(this)


    }
}