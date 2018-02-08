package it.cammino.risuscito

import android.Manifest
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.preference.ListPreference
import android.support.v7.preference.Preference
import android.support.v7.preference.SwitchPreferenceCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompatDividers
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.EasyPermissions

class SettingsFragment : PreferenceFragmentCompatDividers() {

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
        if (!mMainActivity!!.isOnTablet) mMainActivity!!.enableBottombar(false)

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

        val darkTheme = findPreference("dark_mode") as SwitchPreferenceCompat
        darkTheme.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, _ ->
            if (mMainActivity != null) mMainActivity!!.recreate()
            true
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    private fun loadStorageList(external: Boolean) {
        Log.d(
                TAG,
                "loadStorageList: WRITE_EXTERNAL_STORAGE "
                        + Utility.isExternalStorageWritable()
                        + " / "
                        + external)
        if (Utility.isExternalStorageWritable() && external) {
            mEntries = resources.getStringArray(R.array.save_location_sd_entries)
            mEntryValues = resources.getStringArray(R.array.save_location_sd_values)
        } else {
            mEntries = resources.getStringArray(R.array.save_location_nosd_entries)
            mEntryValues = resources.getStringArray(R.array.save_location_nosd_values)
        }
    }

    companion object {
        private val TAG = SettingsFragment::class.java.canonicalName
    }

}
