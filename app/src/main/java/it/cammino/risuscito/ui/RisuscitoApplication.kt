package it.cammino.risuscito.ui

import android.content.Context
import androidx.multidex.MultiDexApplication
import com.google.android.material.color.DynamicColors
import it.cammino.risuscito.utils.LocaleManager
import it.cammino.risuscito.utils.extension.dynamicColorOptions
import it.cammino.risuscito.utils.extension.setDefaultNightMode


class RisuscitoApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        applicationContext.setDefaultNightMode()

        DynamicColors.applyToActivitiesIfAvailable(this, dynamicColorOptions)

    }

    override fun attachBaseContext(base: Context) {
        localeManager = LocaleManager(base)
        super.attachBaseContext(localeManager.useCustomConfig(base))
    }

    companion object {
        internal val TAG = RisuscitoApplication::class.java.canonicalName
        lateinit var localeManager: LocaleManager
    }
}
