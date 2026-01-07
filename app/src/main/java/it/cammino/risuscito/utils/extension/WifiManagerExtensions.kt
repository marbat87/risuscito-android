package it.cammino.risuscito.utils.extension

import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.WifiLock
import android.os.Build
import androidx.annotation.RequiresApi
import it.cammino.risuscito.utils.OSUtils

const val LOCK_TAG = "risuscito_lock"

fun WifiManager.createWifiLockRisuscito(): WifiLock {
    return if (OSUtils.hasQ()) createWifiLockQ()
    else createWifiLockLegacy()
}

@RequiresApi(Build.VERSION_CODES.Q)
fun WifiManager.createWifiLockQ(): WifiLock {
    return createWifiLock(WifiManager.WIFI_MODE_FULL_LOW_LATENCY, LOCK_TAG)
}

@Suppress("DEPRECATION")
fun WifiManager.createWifiLockLegacy(): WifiLock {
    return createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, LOCK_TAG)
}