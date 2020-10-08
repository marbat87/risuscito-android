package it.cammino.risuscito.ui

import android.annotation.TargetApi
import android.content.Context
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


class LocaleManager(context: Context) {

    //    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private var customScale = 0F

    init {
        Log.d(TAG, "init language: ${getLanguage(context)}")

        if (getLanguage(context).isNotEmpty()) {
            Log.d(TAG, "init - settings language set: ${getLanguage(context)}")
        } else {
            // non è ancora stata impostata nessuna lingua nelle impostazioni --> setto una lingua
            // selezionabile oppure IT se non presente
            val mLanguage = when (getSystemLocale(context.resources).language) {
                LANGUAGE_UKRAINIAN -> LANGUAGE_UKRAINIAN
                LANGUAGE_ENGLISH -> if (getSystemLocale(context.resources).country.isNotEmpty() && getSystemLocale(context.resources).country == COUNTRY_PHILIPPINES)
                    LANGUAGE_ENGLISH_PHILIPPINES
                else
                    LANGUAGE_ENGLISH
                LANGUAGE_TURKISH -> LANGUAGE_TURKISH
                LANGUAGE_POLISH -> LANGUAGE_POLISH
                else -> LANGUAGE_ITALIAN
            }
            Log.d(TAG, "attachBaseContext - default language set: $mLanguage")
            persistLanguage(context, mLanguage)
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

//    val language: String
//        get() = prefs.getString(Utility.SYSTEM_LANGUAGE, "") ?: ""

    fun persistLanguage(context: Context, language: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putString(Utility.SYSTEM_LANGUAGE, language)
        }
    }

    fun getLanguage(context: Context): String {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(Utility.SYSTEM_LANGUAGE, "")
                ?: ""
    }

    fun useCustomConfig(context: Context): Context {
        Log.d(TAG, "useCustomConfig language: ${getLanguage(context)}")
        val locale = if (getLanguage(context) == LANGUAGE_ENGLISH_PHILIPPINES) Locale(LANGUAGE_ENGLISH, COUNTRY_PHILIPPINES) else Locale(getLanguage(context))
        Log.d(TAG, "useCustomConfig language: ${locale.language}")
        Log.d(TAG, "useCustomConfig country: ${locale.country}")
        Locale.setDefault(locale)
        val config = if (LUtils.hasJB()) Configuration() else Configuration(context.resources.configuration)
        if (customScale > 0F)
            config.fontScale = customScale
        setSystemLocale(config, locale)
        return context.updateConfiguration(config)
    }

    fun updateConfigurationIfSupported(context: Context, overrideConfiguration: Configuration?): Configuration? {
        overrideConfiguration?.let { config ->
            if (isLocaleNotEmpty(config))
                return config
            setSystemLocale(config, if (getLanguage(context) == LANGUAGE_ENGLISH_PHILIPPINES) Locale(LANGUAGE_ENGLISH, COUNTRY_PHILIPPINES) else Locale(getLanguage(context)))
        }
        return overrideConfiguration
    }

    companion object {

        private val TAG = LocaleManager::class.java.canonicalName
        const val LANGUAGE_ITALIAN = "it"
        const val LANGUAGE_ENGLISH = "en"
        const val LANGUAGE_UKRAINIAN = "uk"
        const val LANGUAGE_TURKISH = "tr"
        const val LANGUAGE_POLISH = "pl"
        const val LANGUAGE_ENGLISH_PHILIPPINES = "ep"

        const val COUNTRY_PHILIPPINES = "PH"

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