package it.cammino.risuscito

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import com.takisoft.preferencex.PreferenceFragmentCompat
import com.takisoft.preferencex.SimpleMenuPreference
import it.cammino.risuscito.Utility.CHANGE_LANGUAGE
import it.cammino.risuscito.Utility.DB_RESET
import it.cammino.risuscito.Utility.NIGHT_MODE
import it.cammino.risuscito.Utility.PRIMARY_COLOR
import it.cammino.risuscito.Utility.SAVE_LOCATION
import it.cammino.risuscito.Utility.SCREEN_ON
import it.cammino.risuscito.Utility.SECONDARY_COLOR
import it.cammino.risuscito.Utility.SYSTEM_LANGUAGE
import it.cammino.risuscito.ui.LocaleManager.Companion.getSystemLocale
import it.cammino.risuscito.utils.ThemeUtils
import pub.devrel.easypermissions.EasyPermissions

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var mEntries: Array<String>
    private lateinit var mEntryValues: Array<String>
    internal var mMainActivity: MainActivity? = null

    override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mMainActivity = activity as? MainActivity
        mMainActivity?.setupToolbarTitle(R.string.title_activity_settings)

        mMainActivity?.setTabVisible(false)
        mMainActivity?.enableFab(false)
        mMainActivity?.enableBottombar(false)

        val listPreference = findPreference("memoria_salvataggio_scelta") as? ListPreference

        loadStorageList(
                EasyPermissions.hasPermissions(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE))

        listPreference?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val listPref = it as? ListPreference
            listPref?.entries = mEntries
            val pref = PreferenceManager.getDefaultSharedPreferences(context)
            val saveLocation = pref.getString(SAVE_LOCATION, "0")
            listPref?.setDefaultValue(saveLocation)
            listPref?.entryValues = mEntryValues
            false
        }

        val countingPreference = findPreference(NIGHT_MODE) as? SimpleMenuPreference
        countingPreference?.summary = ThemeUtils.getNightModeText(requireContext())

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, s: String) {
        Log.d(TAG, "onSharedPreferenceChanged: $s")
        if (s == PRIMARY_COLOR) {
            Log.d(TAG, "onSharedPreferenceChanged: PRIMARY_COLOR" + sharedPreferences.getInt(s, 0))
            activity?.recreate()
        }
        if (s == SECONDARY_COLOR) {
            Log.d(TAG, "onSharedPreferenceChanged: SECONDARY_COLOR" + sharedPreferences.getInt(s, 0))
            activity?.recreate()
        }
        if (s == NIGHT_MODE) {
            Log.d(TAG, "onSharedPreferenceChanged: dark_mode" + sharedPreferences.getString(s, "0"))
            ThemeUtils.setDefaultNightMode(requireContext())
            val countingPreference = findPreference(NIGHT_MODE) as? SimpleMenuPreference
            countingPreference?.summary = ThemeUtils.getNightModeText(requireContext())
        }
        if (s == SYSTEM_LANGUAGE) {
            Log.d(
                    TAG,
                    "onSharedPreferenceChanged: cur lang " + getSystemLocale(resources)
                            .language)
            Log.d(TAG, "onSharedPreferenceChanged: cur set ${sharedPreferences.getString(s, "")}")
            if (!getSystemLocale(resources)
                            .language
                            .equals(sharedPreferences.getString(s, "it"), ignoreCase = true)) {
                val mIntent = activity?.baseContext?.packageManager?.getLaunchIntentForPackage(requireActivity().baseContext.packageName)
                mIntent?.let {
                    it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    it.putExtra(DB_RESET, true)
                    val currentLang = getSystemLocale(resources).language
                    it.putExtra(
                            CHANGE_LANGUAGE,
                            currentLang + "-" + sharedPreferences.getString(s, ""))
                    startActivity(it)
                }
            }
        }
        if (s == SCREEN_ON) LUtils.getInstance(requireActivity()).checkScreenAwake()
    }

    private fun loadStorageList(external: Boolean) {
        Log.d(
                TAG,
                "loadStorageList: WRITE_EXTERNAL_STORAGE "
                        + Utility.isExternalStorageWritable
                        + " / "
                        + external)
        if (Utility.isExternalStorageWritable && external) {
            mEntries = resources.getStringArray(R.array.save_location_sd_entries)
            mEntryValues = resources.getStringArray(R.array.save_location_sd_values)
        } else {
            mEntries = resources.getStringArray(R.array.save_location_nosd_entries)
            mEntryValues = resources.getStringArray(R.array.save_location_nosd_values)
            PreferenceManager.getDefaultSharedPreferences(context).edit { putString(SAVE_LOCATION, "0") }
        }
    }

    companion object {
        private val TAG = SettingsFragment::class.java.canonicalName
    }

}
