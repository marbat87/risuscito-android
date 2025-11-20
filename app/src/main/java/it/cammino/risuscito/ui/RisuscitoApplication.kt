package it.cammino.risuscito.ui

import android.content.Context
import androidx.multidex.MultiDexApplication
import com.google.android.material.color.DynamicColors
import com.michaelflisar.composechangelog.statesaver.preferences.ChangelogStateSaverPreferences
import com.michaelflisar.composechangelog.statesaver.preferences.create
import it.cammino.risuscito.utils.LocaleManager
import it.cammino.risuscito.utils.extension.dynamicColorOptions
import it.cammino.risuscito.utils.extension.setDefaultNightMode


class RisuscitoApplication : MultiDexApplication() {

    val changelogStateSaver: ChangelogStateSaverPreferences by lazy {
        ChangelogStateSaverPreferences.create(this)
    }
    override fun onCreate() {
        super.onCreate()

        applicationContext.setDefaultNightMode()

        DynamicColors.applyToActivitiesIfAvailable(this, dynamicColorOptions)

    }

    override fun attachBaseContext(base: Context) {
        localeManager = LocaleManager(base)
        super.attachBaseContext(base)
    }

    companion object {
        internal val TAG = RisuscitoApplication::class.java.canonicalName
        lateinit var localeManager: LocaleManager
    }
}
