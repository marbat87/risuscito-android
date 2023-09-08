package it.cammino.risuscito.utils

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.os.Build.VERSION_CODES.N
import android.provider.Settings
import android.util.Log
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import it.cammino.risuscito.utils.extension.systemLocale
import java.util.*


class LocaleManager(context: Context) {

    private var customScale = 0F

    init {
        Log.d(TAG, "init language: ${getLanguage(context)}")

        if (getLanguage(context).isNotEmpty()) {
            Log.d(TAG, "init - settings language set: ${getLanguage(context)}")
        } else {
            // non è ancora stata impostata nessuna lingua nelle impostazioni --> setto una lingua
            // selezionabile oppure IT se non presente
            setDefaultSystemLanguage(context)
        }

        var returnScale = 0F
        try {
            val actualScale = context.resources.configuration.fontScale
            Log.d(TAG, "actualScale: $actualScale")
            val systemScale =
                Settings.System.getFloat(context.contentResolver, Settings.System.FONT_SCALE)
            Log.d(TAG, "systemScale: $systemScale")
            if (actualScale != systemScale) {
                returnScale = systemScale
            }
        } catch (e: Settings.SettingNotFoundException) {
            Log.w(
                TAG,
                "Settings.SettingNotFoundException - FUNZIONE RESIZE TESTO NON SUPPORTATA: ${e.localizedMessage}"
            )
        } catch (e: NullPointerException) {
            Log.w(
                TAG,
                "Settings.SettingNotFoundException - FUNZIONE RESIZE TESTO NON SUPPORTATA: ${e.localizedMessage}"
            )
        }
        customScale = returnScale

    }

    fun setDefaultSystemLanguage(context: Context) {
        val mLanguage = when (context.resources.systemLocale.language) {
            LANGUAGE_UKRAINIAN -> LANGUAGE_UKRAINIAN
            LANGUAGE_ENGLISH -> if (context.resources.systemLocale.country.isNotEmpty()
                && context.resources.systemLocale.country == COUNTRY_PHILIPPINES
            )
                LANGUAGE_ENGLISH_PHILIPPINES
            else
                LANGUAGE_ENGLISH
            LANGUAGE_TURKISH -> LANGUAGE_TURKISH
            LANGUAGE_POLISH -> LANGUAGE_POLISH
            else -> LANGUAGE_ITALIAN
        }
        Log.d(TAG, "setDefaultSystemLanguage - default language set: $mLanguage")
        persistLanguage(context, mLanguage)
    }

    fun persistLanguage(context: Context, language: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putString(Utility.SYSTEM_LANGUAGE, language)
        }
    }

    fun getLanguage(context: Context): String {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(Utility.SYSTEM_LANGUAGE, StringUtils.EMPTY).orEmpty()
    }

    fun useCustomConfig(context: Context): Context {
        Log.d(TAG, "useCustomConfig language: ${getLanguage(context)}")
        val locale = if (getLanguage(context) == LANGUAGE_ENGLISH_PHILIPPINES) Locale(
            LANGUAGE_ENGLISH,
            COUNTRY_PHILIPPINES
        ) else Locale(getLanguage(context))
        Log.d(TAG, "useCustomConfig language: ${locale.language}")
        Log.d(TAG, "useCustomConfig country: ${locale.country}")
        Locale.setDefault(locale)
        val config = Configuration()
        if (customScale > 0F)
            config.fontScale = customScale
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return context.createConfigurationContext(config)
    }

    fun updateConfigurationIfSupported(
        context: Context,
        overrideConfiguration: Configuration?
    ): Configuration? {
        overrideConfiguration?.let { config ->
            if (isLocaleNotEmpty(config))
                return config
            val locale = if (getLanguage(context) == LANGUAGE_ENGLISH_PHILIPPINES) Locale(
                LANGUAGE_ENGLISH,
                COUNTRY_PHILIPPINES
            ) else Locale(getLanguage(context))
            config.setLocale(locale)
            config.setLayoutDirection(locale)
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
        private fun isLocaleNotEmptyLegacy(config: Configuration): Boolean {
            return config.locale != null
        }

        @TargetApi(N)
        private fun isLocaleNotEmptyN(config: Configuration): Boolean {
            return !config.locales.isEmpty
        }

        fun isLocaleNotEmpty(config: Configuration): Boolean {
            return if (OSUtils.hasN())
                isLocaleNotEmptyN(config)
            else
                isLocaleNotEmptyLegacy(config)
        }

    }
}