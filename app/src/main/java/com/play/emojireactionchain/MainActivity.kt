package com.play.emojireactionchain

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.android.gms.ads.MobileAds
import com.play.emojireactionchain.ui.EmojiGameApp
import com.play.emojireactionchain.ui.theme.EmojiGameTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val backgroundScope = CoroutineScope(Dispatchers.IO)
        backgroundScope.launch {
            // Initialize the Google Mobile Ads SDK on a background thread.
            MobileAds.initialize(this@MainActivity) {}
        }
//        val testDeviceIds = Arrays.asList("D88961EAEF99FFD783871BE31FD76D95")
//        val configuration = RequestConfiguration.Builder()
//            .setTestDeviceIds(testDeviceIds)
//            .build()
//        MobileAds.setRequestConfiguration(configuration)
        enableEdgeToEdge()
        setContent {
            EmojiGameTheme {
                EmojiGameApp()
            }
        }
    }
}