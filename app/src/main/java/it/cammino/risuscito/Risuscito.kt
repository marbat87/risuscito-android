package it.cammino.risuscito

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import com.google.android.gms.common.SignInButton
import it.cammino.risuscito.dialogs.SimpleDialogFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_risuscito.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest

class Risuscito : Fragment(), SimpleDialogFragment.SimpleCallback, EasyPermissions.PermissionCallbacks {

    private var mMainActivity: MainActivity? = null
    private var thisVersion: String? = null
    private var rootView: View? = null
    private val signInVisibility = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Implement UI change code here once notification is received
            try {
                Log.d(javaClass.name, "BROADCAST_SIGNIN_VISIBLE")
                Log.d(
                        javaClass.name,
                        "DATA_VISIBLE: " + intent.getBooleanExtra(DATA_VISIBLE, false))
                sign_in_button?.visibility = if (intent.getBooleanExtra(DATA_VISIBLE, false)) View.VISIBLE else View.INVISIBLE
            } catch (e: IllegalArgumentException) {
                Log.e(javaClass.name, e.localizedMessage, e)
            }

        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.activity_risuscito, container, false)

        mMainActivity = activity as MainActivity?

        mMainActivity!!.enableFab(false)
//        if (!mMainActivity!!.isOnTablet) {
        mMainActivity!!.enableBottombar(false)
//        }

        val sp = PreferenceManager.getDefaultSharedPreferences(activity)

        // get version numbers
        val lastVersion = sp.getString(VERSION_KEY, NO_VERSION)
        //        String thisVersion;
        Log.d("Changelog", "lastVersion: " + lastVersion!!)
        try {
            thisVersion = activity!!
                    .packageManager
                    .getPackageInfo(activity!!.packageName, 0)
                    .versionName
        } catch (e: NameNotFoundException) {
            thisVersion = NO_VERSION
            Log.d("Changelog", "could not get version name from manifest!")
            e.printStackTrace()
        }

        Log.d("Changelog", "thisVersion: " + thisVersion!!)

        if (thisVersion != lastVersion) {
            SimpleDialogFragment.Builder(
                    (activity as AppCompatActivity?)!!, this@Risuscito, "CHANGELOG")
                    .title(R.string.dialog_change_title)
                    .setCustomView(R.layout.dialog_changelogview)
                    .positiveButton(android.R.string.ok)
                    .setHasCancelListener()
                    .setCanceable()
                    .show()
        }

        Log.d(
                TAG,
                "onCreateView: signed in = " + PreferenceManager.getDefaultSharedPreferences(activity)
                        .getBoolean(Utility.SIGNED_IN, false))
        checkStoragePermissions()

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mMainActivity!!.setupToolbarTitle(R.string.activity_homepage)
        sign_in_button!!.setSize(SignInButton.SIZE_WIDE)
        activity!!.material_tabs.visibility = View.GONE

        imageView1.setOnClickListener { mMainActivity!!.drawer!!.openDrawer() }

        sign_in_button.visibility = if (PreferenceManager.getDefaultSharedPreferences(activity)
                        .getBoolean(Utility.SIGNED_IN, false))
            View.INVISIBLE
        else
            View.VISIBLE

        sign_in_button.setOnClickListener {
            mMainActivity!!.setShowSnackbar()
            mMainActivity!!.signIn()
        }
    }

    override fun onResume() {
        super.onResume()
//        activity!!.registerReceiver(signInVisibility, IntentFilter(BROADCAST_SIGNIN_VISIBLE))
        LocalBroadcastManager.getInstance(activity!!).registerReceiver(signInVisibility, IntentFilter(BROADCAST_SIGNIN_VISIBLE))
        LocalBroadcastManager.getInstance(activity!!).registerReceiver(signInVisibility, IntentFilter(BROADCAST_SIGNIN_VISIBLE))
        val fragment = SimpleDialogFragment.findVisible((activity as AppCompatActivity?)!!, "CHANGELOG")
        fragment?.setmCallback(this@Risuscito)
    }

    override fun onDestroy() {
        super.onDestroy()
//        activity!!.unregisterReceiver(signInVisibility)
        LocalBroadcastManager.getInstance(activity!!).unregisterReceiver(signInVisibility)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPositive(tag: String) {
        Log.d(TAG, "onPositive: $tag")
        when (tag) {
            "CHANGELOG" -> {
                PreferenceManager.getDefaultSharedPreferences(activity).edit { putString(VERSION_KEY, thisVersion) }
            }
        }
    }

    override fun onNegative(tag: String) {}

    override fun onNeutral(tag: String) {}

    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    @AfterPermissionGranted(Utility.WRITE_STORAGE_RC)
    private fun checkStoragePermissions() {
        Log.d(TAG, "checkStoragePermissions: ")
        if (!EasyPermissions.hasPermissions(
                        context!!, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            EasyPermissions.requestPermissions(
                    PermissionRequest.Builder(
                            this,
                            Utility.WRITE_STORAGE_RC,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            .setRationale(R.string.external_storage_pref_rationale)
                            .build())
        }
    }

    override fun onPermissionsGranted(requestCode: Int, list: List<String>) {
        // Some permissions have been
        Log.d(TAG, "onPermissionsGranted: ")
        Snackbar.make(rootView!!, getString(R.string.permission_ok), Snackbar.LENGTH_SHORT).show()
    }

    override fun onPermissionsDenied(requestCode: Int, list: List<String>) {
        // Some permissions have been denied
        Log.d(TAG, "onPermissionsDenied: ")
//        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
//        editor.putString(Utility.SAVE_LOCATION, "0")
//        editor.apply()
        PreferenceManager.getDefaultSharedPreferences(context).edit { putString(Utility.SAVE_LOCATION, "0") }
        Snackbar.make(rootView!!, getString(R.string.external_storage_denied), Snackbar.LENGTH_SHORT)
                .show()
    }

    companion object {
        private val TAG = Risuscito::class.java.canonicalName
        const val BROADCAST_SIGNIN_VISIBLE = "it.cammino.risuscito.signin.SIGNIN_VISIBLE"
        const val DATA_VISIBLE = "it.cammino.risuscito.signin.data.DATA_VISIBLE"
        private const val VERSION_KEY = "PREFS_VERSION_KEY"
        private const val NO_VERSION = ""
    }
}
