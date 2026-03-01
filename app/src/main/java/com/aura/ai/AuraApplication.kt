package com.aura.ai

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AuraApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Enable heavy model loading optimization
        val runtime = Runtime.getRuntime()
        runtime.gc()
    }
}