package com.play.emojireactionchain.ui

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

@Composable
fun BannerAd(adUnitId: String) {
    AndroidView(
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                setAdUnitId(adUnitId)
                loadAd(AdRequest.Builder().build())
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

// === REWARDED ADS ===

fun loadRewardedAd(
    context: Context,
    adUnitId: String,
    onAdLoaded: (RewardedAd) -> Unit,
    onAdFailedToLoad: (LoadAdError) -> Unit
) {
    val adRequest = AdRequest.Builder().build()
    RewardedAd.load(
        context,
        adUnitId,
        adRequest,
        object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                Log.d("RewardedAd", "Ad was loaded.")
                onAdLoaded(ad)
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e("RewardedAd", "Ad failed to load: ${adError.message}")
                onAdFailedToLoad(adError)
            }
        }
    )
}

data class RewardedAdState(
    val rewardedAd: RewardedAd?,
    val loadAd: () -> Unit,
    val isLoading: Boolean
)

@Composable
fun rememberRewardedAd(adUnitId: String): RewardedAdState {
    val context = LocalContext.current
    var rewardedAd by remember { mutableStateOf<RewardedAd?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var adLoadError by remember { mutableStateOf<LoadAdError?>(null) }

    val loadAd = {
        if (!isLoading) {
            isLoading = true
            loadRewardedAd(
                context, adUnitId,
                onAdLoaded = { ad ->
                    rewardedAd = ad
                    isLoading = false
                    adLoadError = null
                },
                onAdFailedToLoad = { error ->
                    isLoading = false
                    adLoadError = error
                }
            )
        }
    }

    DisposableEffect(adUnitId) {
        loadAd()
        onDispose {
            rewardedAd?.fullScreenContentCallback = null
            rewardedAd = null
        }
    }

    return RewardedAdState(rewardedAd, loadAd, isLoading)
}

fun showRewardedAd(
    rewardedAd: RewardedAd?,
    activity: Activity,
    onUserEarnedReward: () -> Unit,
    onAdClosed: () -> Unit
) {
    if (rewardedAd != null) {
        rewardedAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d("RewardedAd", "Ad dismissed.")
                onAdClosed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e("RewardedAd", "Ad failed to show: ${adError.message}")
                onAdClosed()
            }
        }

        rewardedAd.show(activity) { rewardItem ->
            val rewardAmount = rewardItem.amount
            val rewardType = rewardItem.type
            Log.d("RewardedAd", "User earned reward: Amount: $rewardAmount, Type: $rewardType")
            onUserEarnedReward()
        }
    } else {
        Log.d("RewardedAd", "Ad was not loaded.")
        onAdClosed()
    }
}

// === INTERSTITIAL ADS ===

fun loadInterstitialAd(
    context: Context,
    adUnitId: String,
    onAdLoaded: (InterstitialAd) -> Unit,
    onAdFailedToLoad: (LoadAdError) -> Unit
) {
    val adRequest = AdRequest.Builder().build()
    InterstitialAd.load(
        context,
        adUnitId,
        adRequest,
        object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                Log.d("InterstitialAd", "Ad was loaded.")
                onAdLoaded(ad)
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e("InterstitialAd", "Ad failed to load: ${adError.message}")
                onAdFailedToLoad(adError)
            }
        }
    )
}

data class InterstitialAdState(
    val interstitialAd: InterstitialAd?,
    val loadAd: () -> Unit,
    val isLoading: Boolean
)

@Composable
fun rememberInterstitialAd(adUnitId: String): InterstitialAdState {
    val context = LocalContext.current
    var interstitialAd by remember { mutableStateOf<InterstitialAd?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var adLoadError by remember { mutableStateOf<LoadAdError?>(null) }

    val loadAd = {
        if (!isLoading) {
            isLoading = true
            loadInterstitialAd(
                context, adUnitId,
                onAdLoaded = { ad ->
                    interstitialAd = ad
                    isLoading = false
                    adLoadError = null
                },
                onAdFailedToLoad = { error ->
                    isLoading = false
                    adLoadError = error
                }
            )
        }
    }

    DisposableEffect(adUnitId) {
        loadAd()
        onDispose {
            interstitialAd?.fullScreenContentCallback = null
            interstitialAd = null
        }
    }

    return InterstitialAdState(interstitialAd, loadAd, isLoading)
}

fun showInterstitialAd(
    interstitialAd: InterstitialAd?,
    activity: Activity,
    onAdClosed: () -> Unit,
    onAdImpression: (() -> Unit)? = null
) {
    if (interstitialAd != null) {
        interstitialAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d("InterstitialAd", "Ad dismissed.")
                onAdClosed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e("InterstitialAd", "Ad failed to show: ${adError.message}")
                onAdClosed()
            }

            override fun onAdImpression() {
                Log.d("InterstitialAd", "Ad recorded an impression.")
                onAdImpression?.invoke()
            }
        }

        interstitialAd.show(activity)
    } else {
        Log.d("InterstitialAd", "Ad was not loaded.")
        onAdClosed()
    }
}

// === PREVIEW COMPOSABLES ===

@Composable
fun RewardScreen() {
    val context = LocalContext.current
    val rewardedAdState = rememberRewardedAd("ca-app-pub-3940256099942544/5224354917")
    val activity = context as? Activity ?: return
    var rewardEarned by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        if (rewardedAdState.isLoading) {
            Text("Loading rewarded ad...")
        } else if (rewardedAdState.rewardedAd == null) {
            Text("Ad not loaded. Tap button to try again.")
        }

        Button(onClick = {
            if (rewardedAdState.rewardedAd != null) {
                showRewardedAd(
                    rewardedAdState.rewardedAd,
                    activity,
                    onUserEarnedReward = { rewardEarned = true },
                    onAdClosed = { rewardedAdState.loadAd() }
                )
            } else {
                rewardedAdState.loadAd()
            }
        }) {
            Text("Watch Ad to Earn Reward")
        }

        if (rewardEarned) {
            Text("Congratulations! You earned a reward!")
        }
    }
}

@Composable
fun InterstitialAdScreen() {
    val context = LocalContext.current
    val interstitialAdState =
        rememberInterstitialAd("ca-app-pub-3940256099942544/1033173712") // Test ID
    val activity = context as? Activity ?: return
    var adShown by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        if (interstitialAdState.isLoading) {
            Text("Loading interstitial ad...")
        } else if (interstitialAdState.interstitialAd == null) {
            Text("Ad not loaded. Tap button to try again.")
        }

        Button(onClick = {
            if (interstitialAdState.interstitialAd != null) {
                showInterstitialAd(
                    interstitialAdState.interstitialAd,
                    activity,
                    onAdClosed = {
                        interstitialAdState.loadAd()
                        adShown = true
                    }
                )
            } else {
                interstitialAdState.loadAd()
            }
        }) {
            Text("Show Interstitial Ad")
        }

        if (adShown) {
            Text("Ad was shown successfully!")
        }
    }
}

@Preview
@Composable
fun BannerAdPreview() {
    BannerAd("ca-app-pub-3940256099942544/6300978111")
}

@Preview
@Composable
fun RewardedAdPreview() {
    RewardScreen()
}

@Preview
@Composable
fun InterstitialAdPreview() {
    InterstitialAdScreen()
}