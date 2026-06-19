package com.starkified.colorit

import android.app.Application
import com.starkified.colorit.utils.AudioManager
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ColorItApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AudioManager.init(this)

        // Configure child-directed treatment & G-rating restrictions for COPPA / Google Play Families Policy compliance
        val requestConfiguration = MobileAds.getRequestConfiguration().toBuilder()
            .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
            .setTagForUnderAgeOfConsent(RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE)
            .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
            .build()
        MobileAds.setRequestConfiguration(requestConfiguration)

        MobileAds.initialize(this) {
            com.starkified.colorit.util.AdManager.init(this)
        }
    }
}
