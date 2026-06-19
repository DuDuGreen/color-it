package com.starkified.colorit.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

// Toggle this flag to show/hide ads across the entire app
const val SHOW_ADS = true

@Composable
fun BannerAd(
    modifier: Modifier = Modifier,
    adSize: AdSize = AdSize.BANNER,
    adUnitId: String = "ca-app-pub-2441996515069078/9501513706" // Production AdMob Banner unit ID
) {
    if (!SHOW_ADS) {
        androidx.compose.foundation.layout.Box(modifier = modifier)
        return
    }

    val context = LocalContext.current
    val adView = remember {
        AdView(context).apply {
            setAdSize(adSize)
            this.adUnitId = adUnitId
        }
    }

    DisposableEffect(adView) {
        adView.loadAd(AdRequest.Builder().build())
        onDispose {
            adView.destroy()
        }
    }

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { adView }
    )
}
