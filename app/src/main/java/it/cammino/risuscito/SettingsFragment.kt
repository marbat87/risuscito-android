package it.cammino.risuscito

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.preference.ListPreference
import android.support.v7.preference.Preference
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat
import it.cammino.risuscito.ui.ThemeableActivity
import kotlinx.android.synthetic.main.activity_main.*
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

        mMainActivity = activity as MainActivity?
        if (mMainActivity != null) mMainActivity!!.setupToolbarTitle(R.string.title_activity_settings)

        activity!!.material_tabs.visibility = View.GONE
        mMainActivity!!.enableFab(false)
//        if (!mMainActivity!!.isOnTablet) mMainActivity!!.enableBottombar(false)
        mMainActivity!!.enableBottombar(false)

        val listPreference = findPreference("memoria_salvataggio_scelta") as ListPreference

        loadStorageList(
                EasyPermissions.hasPermissions(context!!, Manifest.permission.WRITE_EXTERNAL_STORAGE))

        listPreference.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            listPreference.entries = mEntries
            val pref = PreferenceManager.getDefaultSharedPreferences(activity)
            val saveLocation = pref.getString(Utility.SAVE_LOCATION, "0")
            listPreference.setDefaultValue(saveLocation)
            listPreference.entryValues = mEntryValues
            false
        }

//        val darkTheme = findPreference("dark_mode") as SwitchPreferenceCompat
//        darkTheme.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, _ ->
//            if (mMainActivity != null) mMainActivity!!.recreate()
//            true
//        }

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
        if (s.equals("new_primary_color", ignoreCase = true)) {
            Log.d(TAG, "onSharedPreferenceChanged: new_primary_color" + sharedPreferences.getInt(s, 0))
            activity!!.recreate()
        }
        if (s.equals("new_accent_color", ignoreCase = true)) {
            Log.d(TAG, "onSharedPreferenceChanged: new_accent_color" + sharedPreferences.getInt(s, 0))
            activity!!.recreate()
        }
        if (s.equals("dark_mode", ignoreCase = true)) {
            Log.d(TAG, "onSharedPreferenceChanged: dark_mode" + sharedPreferences.getBoolean(s, false))
            activity!!.recreate()
        }
        if (s == Utility.SYSTEM_LANGUAGE) {
            Log.d(
                    TAG,
                    "onSharedPreferenceChanged: cur lang " + ThemeableActivity.getSystemLocalWrapper(resources.configuration)
                            .language)
            Log.d(TAG, "onSharedPreferenceChanged: cur set " + sharedPreferences.getString(s, "")!!)
            if (!ThemeableActivity.getSystemLocalWrapper(resources.configuration)
                            .language
                            .equals(sharedPreferences.getString(s, "it")!!, ignoreCase = true)) {
                val i = activity!!.baseContext
                        .packageManager
                        .getLaunchIntentForPackage(activity!!.baseContext.packageName)
                if (i != null) {
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    i.putExtra(Utility.DB_RESET, true)
                    val currentLang = ThemeableActivity.getSystemLocalWrapper(resources.configuration)
                            .language
                    i.putExtra(
                            Utility.CHANGE_LANGUAGE,
                            currentLang + "-" + sharedPreferences.getString(s, ""))
                }
                startActivity(i)
            }
        }
        if (s == Utility.SCREEN_ON) checkScreenAwake()
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
        }
    }

    // controlla se l'app deve mantenere lo schermo acceso
    private fun checkScreenAwake() {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        val screenOn = pref.getBoolean(Utility.SCREEN_ON, false)
        if (screenOn)
            activity!!.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        else
            activity!!.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    companion object {
        private val TAG = SettingsFragment::class.java.canonicalName
    }

}
