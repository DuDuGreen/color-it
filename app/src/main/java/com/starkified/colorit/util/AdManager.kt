package com.starkified.colorit.util

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdManager {
    private const val TAG = "AdManager"
    private const val PREFS_NAME = "ad_prefs"
    private const val KEY_LAUNCH_COUNT = "launch_count"
    private const val AD_UNIT_ID = "ca-app-pub-2441996515069078/6739618997" // Production AdMob Interstitial Ad ID

    private var interstitialAd: InterstitialAd? = null
    private var isAdEnabledForSession = false
    private var hasAdBeenShownThisSession = false

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentCount = prefs.getInt(KEY_LAUNCH_COUNT, 0)
        val newCount = currentCount + 1
        prefs.edit().putInt(KEY_LAUNCH_COUNT, newCount).apply()

        // Ad is enabled on every second launch (e.g. 2nd, 4th, 6th, etc.)
        isAdEnabledForSession = newCount % 2 == 0
        Log.d(TAG, "Launch count: $newCount, Ads enabled for this session: $isAdEnabledForSession")

        if (isAdEnabledForSession) {
            loadInterstitial(context)
        }
    }

    private fun loadInterstitial(context: Context) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    Log.d(TAG, "Interstitial ad loaded successfully")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    Log.d(TAG, "Failed to load interstitial ad: ${error.message}")
                }
            }
        )
    }

    fun showAdIfReady(activity: Activity, onAdDismissed: () -> Unit) {
        val ad = interstitialAd
        if (isAdEnabledForSession && !hasAdBeenShownThisSession && ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Ad dismissed")
                    interstitialAd = null
                    hasAdBeenShownThisSession = true
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                    Log.d(TAG, "Ad failed to show: ${error.message}")
                    interstitialAd = null
                    onAdDismissed()
                }
            }
            ad.show(activity)
        } else {
            onAdDismissed()
        }
    }
}
