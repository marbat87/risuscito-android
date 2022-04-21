package it.cammino.risuscito.ui

import android.content.Context
import androidx.multidex.MultiDexApplication
import com.google.android.material.color.DynamicColors
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.utils.ThemeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class RisuscitoApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        ThemeUtils.setDefaultNightMode(applicationContext)

        DynamicColors.applyToActivitiesIfAvailable(this, ThemeUtils.getDynamicColorOptions(this))

        val mDao = RisuscitoDatabase.getInstance(this).cantoDao()
        GlobalScope.launch(Dispatchers.IO) { mDao.getCantoById(1) }

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
