package com.kidslearning.chutki

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kidslearning.chutki.audio.HindiTtsManager
import com.kidslearning.chutki.data.ContentRepository
import com.kidslearning.chutki.data.UserPreferencesManager

class MainActivity : ComponentActivity() {

    private var ttsManager: HindiTtsManager? = null
    private lateinit var userPrefs: UserPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        ContentRepository.initialize(this)
        ttsManager = HindiTtsManager(this)
        userPrefs = UserPreferencesManager(this)

        setContent {
            ChutkiKidsApp(
                userPrefs = userPrefs,
                ttsManager = ttsManager
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsManager?.release()
        ttsManager = null
    }
}
