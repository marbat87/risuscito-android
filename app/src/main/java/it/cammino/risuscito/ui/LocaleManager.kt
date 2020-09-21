package it.cammino.risuscito.ui

import android.annotation.TargetApi
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build.VERSION_CODES.JELLY_BEAN_MR1
import android.os.Build.VERSION_CODES.N
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import it.cammino.risuscito.LUtils
import it.cammino.risuscito.Utility
import java.util.*


class LocaleManager(val context: Context) {

    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private var customScale = 0F

    init {
        Log.d(TAG, "init language: $language")

        if (language.isNotEmpty()) {
            Log.d(TAG, "init - settings language set: $language")
        } else {
            // non è ancora stata impostata nessuna lingua nelle impostazioni --> setto una lingua
            // selezionabile oppure IT se non presente
            val mLanguage = when (getSystemLocale(context.resources).language) {
                LANGUAGE_UKRAINIAN -> LANGUAGE_UKRAINIAN
                LANGUAGE_ENGLISH -> LANGUAGE_ENGLISH
                LANGUAGE_TURKISH -> LANGUAGE_TURKISH
                LANGUAGE_POLISH -> LANGUAGE_POLISH
                else -> LANGUAGE_ITALIAN
            }
            Log.d(TAG, "attachBaseContext - default language set: $mLanguage")
            persistLanguage(mLanguage)
        }

        var returnScale = 0F
        try {
            val actualScale = context.resources.configuration.fontScale
            Log.d(TAG, "actualScale: $actualScale")
            val systemScale = Settings.System.getFloat(context.contentResolver, Settings.System.FONT_SCALE)
            Log.d(TAG, "systemScale: $systemScale")
            if (actualScale != systemScale) {
                returnScale = systemScale
            }
        } catch (e: Settings.SettingNotFoundException) {
            Log.w(TAG, "Settings.SettingNotFoundException - FUNZIONE RESIZE TESTO NON SUPPORTATA: ${e.localizedMessage}")
        } catch (e: NullPointerException) {
            Log.w(TAG, "Settings.SettingNotFoundException - FUNZIONE RESIZE TESTO NON SUPPORTATA: ${e.localizedMessage}")
        }
        customScale = returnScale

    }

    val language: String
        get() = prefs.getString(Utility.SYSTEM_LANGUAGE, "") ?: ""

//    fun setLocale(c: Context): Context {
//        return updateResources(c)
//    }

    fun persistLanguage(language: String) {
        prefs.edit {
            putString(Utility.SYSTEM_LANGUAGE, language)
        }
    }

    fun useCustomConfig(context: Context): Context {
        Log.d(TAG, "useCustomConfig language: $language")
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = if (LUtils.hasJB()) Configuration() else Configuration(context.resources.configuration)
        if (customScale > 0F)
            config.fontScale = customScale
        setSystemLocale(config, locale)
        return context.updateConfiguration(config)
    }

    fun updateConfigurationIfSupported(overrideConfiguration: Configuration?): Configuration? {
        overrideConfiguration?.let { config ->
            if (isLocaleNotEmpty(config))
                return config
            setSystemLocale(config, Locale(language))
        }
        return overrideConfiguration
    }

//    private fun updateResources(context: Context): Context {
//        Log.d(TAG, "updateResources language: $language")
//        val res = context.resources
//        val config = Configuration(res.configuration)
//        val locale = Locale(language)
//        Locale.setDefault(locale)
//
//        // font dimension
//        try {
//            val actualScale = config.fontScale
//            Log.d(TAG, "actualScale: $actualScale")
//            val systemScale = Settings.System.getFloat(context.contentResolver, Settings.System.FONT_SCALE)
//            Log.d(TAG, "systemScale: $systemScale")
//            if (actualScale != systemScale) {
//                config.fontScale = systemScale
//            }
//        } catch (e: Settings.SettingNotFoundException) {
//            Log.w(TAG, "Settings.SettingNotFoundException - FUNZIONE RESIZE TESTO NON SUPPORTATA: ${e.localizedMessage}")
//        } catch (e: NullPointerException) {
//            Log.w(TAG, "Settings.SettingNotFoundException - FUNZIONE RESIZE TESTO NON SUPPORTATA: ${e.localizedMessage}")
//        }
//
//        setSystemLocale(config, locale)
//        return context.updateConfiguration(config)
//    }

    companion object {

        private val TAG = LocaleManager::class.java.canonicalName
        const val LANGUAGE_ITALIAN = "it"
        const val LANGUAGE_ENGLISH = "en"
        const val LANGUAGE_UKRAINIAN = "uk"
        const val LANGUAGE_TURKISH = "tr"
        const val LANGUAGE_POLISH = "pl"

        @Suppress("DEPRECATION")
        private fun updateConfigurationLegacy(context: Context, config: Configuration): Context {
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            return context
        }

        @RequiresApi(JELLY_BEAN_MR1)
        private fun updateConfigurationJB(context: Context, config: Configuration): Context {
            return context.createConfigurationContext(config)
        }

        fun Context.updateConfiguration(config: Configuration): Context {
            return if (LUtils.hasJB())
                updateConfigurationJB(this, config)
            else
                updateConfigurationLegacy(this, config)
        }

        @Suppress("DEPRECATION")
        private fun isLocaleNotEmptyLegacy(config: Configuration): Boolean {
            return config.locale != null
        }

        @TargetApi(N)
        private fun isLocaleNotEmptyN(config: Configuration): Boolean {
            return !config.locales.isEmpty
        }

        fun isLocaleNotEmpty(config: Configuration): Boolean {
            return if (LUtils.hasN())
                isLocaleNotEmptyN(config)
            else
                isLocaleNotEmptyLegacy(config)
        }

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