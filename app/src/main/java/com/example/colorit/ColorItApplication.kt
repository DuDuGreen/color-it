package com.example.colorit

import android.app.Application
import com.example.colorit.utils.AudioManager
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ColorItApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AudioManager.init(this)
        MobileAds.initialize(this) {}
    }
}
