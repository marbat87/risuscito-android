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

//    fun setNewLocale(c: Context, language: String): Context {
//        persistLanguage(language)
//        return updateResources(c, language)
//    }

    @SuppressLint("ApplySharedPref")
    private fun persistLanguage(language: String) {
        prefs.edit {
            putString(Utility.SYSTEM_LANGUAGE, language)
        }
    }

    @Suppress("DEPRECATION")
    private fun updateResources(context: Context, language: String): Context {
        var mContext = context
        val res = mContext.resources
        val config = Configuration(res.configuration)

        Log.d(TAG, "language: $language")

        if (language.isNotEmpty()) {
            val locale = Locale(language)
            Locale.setDefault(locale)
            setSystemLocale(config, locale)
        } else {
            val mLanguage = when (getSystemLocale(res).language) {
                LANGUAGE_UKRAINIAN -> LANGUAGE_UKRAINIAN
                LANGUAGE_ENGLISH -> LANGUAGE_ENGLISH
                else -> LANGUAGE_ITALIAN
            }
            Log.d(TAG, "attachBaseContext - language set: $mLanguage")
            persistLanguage(mLanguage)
            val locale = Locale(mLanguage)
            Locale.setDefault(locale)
            setSystemLocale(config, locale)
        }// non è ancora stata impostata nessuna lingua nelle impostazioni --> setto una lingua
        // selezionabile oppure IT se non presente

        // fond dimension
        try {
            val actualScale = config.fontScale
            Log.d(TAG, "actualScale: $actualScale")
            val systemScale = Settings.System.getFloat(mContext.contentResolver, Settings.System.FONT_SCALE)
            Log.d(TAG, "systemScale: $systemScale")
            if (actualScale != systemScale) {
                config.fontScale = systemScale
            }
        } catch (e: Settings.SettingNotFoundException) {
            Log.e(TAG, "Settings.SettingNotFoundException - FUNZIONE RESIZE TESTO NON SUPPORTATA", e)
        } catch (e: NullPointerException) {
            Log.e(TAG, "NullPointerException - FUNZIONE RESIZE TESTO NON SUPPORTATA", e)
        }

//        val locale = Locale(language)
//        Locale.setDefault(locale)
//
//        setSystemLocalWrapper(config, locale)
        if (LUtils.hasJB()) {
//            config.setLocale(locale)
            mContext = mContext.createConfigurationContext(config)
        } else {
//            config.locale = locale
            res.updateConfiguration(config, res.displayMetrics)
        }
        return mContext
    }

    companion object {

        private val TAG = LocaleManager::class.java.canonicalName
        val LANGUAGE_ITALIAN = "it"
        val LANGUAGE_ENGLISH = "en"
        val LANGUAGE_UKRAINIAN = "uk"

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
        private fun setSystemLocaleMR1(config: Configuration, locale: Locale) {
            config.setLocale(locale)
        }

        fun setSystemLocale(config: Configuration, locale: Locale) {
            if (LUtils.hasN())
                setSystemLocaleMR1(config, locale)
            else
                setSystemLocaleLegacy(config, locale)
        }
    }
}