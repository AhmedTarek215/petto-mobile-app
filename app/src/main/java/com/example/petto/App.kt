package com.example.petto

import android.app.Application
import com.google.firebase.FirebaseApp
import com.jakewharton.threetenabp.AndroidThreeTen

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        FirebaseApp.initializeApp(this)
    }
}