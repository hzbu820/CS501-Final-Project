package com.cs501.pantrypal

import android.app.Application
import com.google.firebase.FirebaseApp

class PantryPalApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}