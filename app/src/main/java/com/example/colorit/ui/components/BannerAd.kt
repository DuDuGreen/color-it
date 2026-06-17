package com.example.colorit.ui.components

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

@Composable
fun BannerAd(
    modifier: Modifier = Modifier,
    adSize: AdSize = AdSize.BANNER,
    adUnitId: String = "ca-app-pub-3940256099942544/6300978111" // Default AdMob test Banner unit ID
) {
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
