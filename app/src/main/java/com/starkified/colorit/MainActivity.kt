package com.starkified.colorit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.starkified.colorit.ui.navigation.AppNavGraph
import com.starkified.colorit.ui.theme.ColorItTheme
import com.starkified.colorit.util.SoundHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var soundHelper: SoundHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ColorItTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavGraph(soundHelper = soundHelper)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        com.starkified.colorit.utils.AudioManager.resumeMusic()
    }

    override fun onPause() {
        super.onPause()
        com.starkified.colorit.utils.AudioManager.pauseMusic()
    }
}
