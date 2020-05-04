package it.cammino.risuscito.ui

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build.VERSION_CODES.JELLY_BEAN_MR1
import android.os.Build.VERSION_CODES.N
import android.provider.Settings
import android.util.Log
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import it.cammino.risuscito.LUtils
import it.cammino.risuscito.Utility
import java.util.*

class LocaleManager(context: Context) {

    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    val language: String
        get() = prefs.getString(Utility.SYSTEM_LANGUAGE, "") ?: ""

    fun setLocale(c: Context): Context {
        return updateResources(c, language)
    }

    @SuppressLint("ApplySharedPref")
    fun persistLanguage(language: String) {
        prefs.edit {
            putString(Utility.SYSTEM_LANGUAGE, language)
        }
    }

    @Suppress("DEPRECATION")
    private fun updateResources(context: Context, language: String): Context {
        val res = context.resources
        val config = Configuration(res.configuration)
        lateinit var locale: Locale

        Log.d(TAG, "language: $language")

        if (language.isNotEmpty()) {
            Log.d(TAG, "attachBaseContext - settings language set: $language")
            locale = Locale(language)
            Locale.setDefault(locale)
        } else {
            val mLanguage = when (getSystemLocale(res).language) {
                LANGUAGE_UKRAINIAN -> LANGUAGE_UKRAINIAN
                LANGUAGE_ENGLISH -> LANGUAGE_ENGLISH
                LANGUAGE_TURKISH -> LANGUAGE_TURKISH
                else -> LANGUAGE_ITALIAN
            }
            Log.d(TAG, "attachBaseContext - default language set: $mLanguage")
            persistLanguage(mLanguage)
            locale = Locale(mLanguage)
            Locale.setDefault(locale)
        }// non è ancora stata impostata nessuna lingua nelle impostazioni --> setto una lingua
        // selezionabile oppure IT se non presente

        // font dimension
        try {
            val actualScale = config.fontScale
            Log.d(TAG, "actualScale: $actualScale")
            val systemScale = Settings.System.getFloat(context.contentResolver, Settings.System.FONT_SCALE)
            Log.d(TAG, "systemScale: $systemScale")
            if (actualScale != systemScale) {
                config.fontScale = systemScale
            }
        } catch (e: Settings.SettingNotFoundException) {
            Log.w(TAG, "Settings.SettingNotFoundException - FUNZIONE RESIZE TESTO NON SUPPORTATA: ${e.localizedMessage}")
        } catch (e: NullPointerException) {
            Log.w(TAG, "Settings.SettingNotFoundException - FUNZIONE RESIZE TESTO NON SUPPORTATA: ${e.localizedMessage}")
        }

        setSystemLocale(config, locale)
        return if (LUtils.hasJB()) {
            config.setLayoutDirection(locale)
            res.updateConfiguration(config, res.displayMetrics)
            context.createConfigurationContext(config)
        } else {
            res.updateConfiguration(config, res.displayMetrics)
            context
        }
    }

    companion object {

        private val TAG = LocaleManager::class.java.canonicalName
        const val LANGUAGE_ITALIAN = "it"
        const val LANGUAGE_ENGLISH = "en"
        const val LANGUAGE_UKRAINIAN = "uk"
        const val LANGUAGE_TURKISH = "tr"

        @Suppress("DEPRECATION")
        private fun getSystemLocaleLegacy(config: Configuration): Locale {
            return config.locale
        }

        @TargetApi(N)
        private fun getSystemLocaleN(config: Configuration): Locale {
            return config.locales.get(0)
        }

        fun getSystemLocale(res: Resources): Locale {
            return if (LUtils.hasN())
                getSystemLocaleN(res.configuration)
            else
                getSystemLocaleLegacy(res.configuration)
        }

        @Suppress("DEPRECATION")
        private fun setSystemLocaleLegacy(config: Configuration, locale: Locale) {
            config.locale = locale
        }

        @TargetApi(JELLY_BEAN_MR1)
        private fun setSystemLocaleJB(config: Configuration, locale: Locale) {
            config.setLocale(locale)
            config.setLayoutDirection(locale)
        }

        fun setSystemLocale(config: Configuration, locale: Locale) {
            if (LUtils.hasJB())
                setSystemLocaleJB(config, locale)
            else
                setSystemLocaleLegacy(config, locale)
        }
    }
}