package com.cs501.pantrypal

import android.app.Application
import com.google.firebase.FirebaseApp

/**
 * 应用程序类，用于初始化Firebase
 */
class PantryPalApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // 初始化Firebase
        FirebaseApp.initializeApp(this)
    }
}