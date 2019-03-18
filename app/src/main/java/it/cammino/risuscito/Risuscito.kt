package it.cammino.risuscito

import android.annotation.TargetApi
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.common.SignInButton
import com.google.android.material.snackbar.Snackbar
import com.michaelflisar.changelog.ChangelogBuilder
import it.cammino.risuscito.utils.ThemeUtils
import kotlinx.android.synthetic.main.activity_risuscito.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest

class Risuscito : Fragment(), EasyPermissions.PermissionCallbacks {

    private var mMainActivity: MainActivity? = null
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
        mMainActivity!!.enableBottombar(false)

        Log.d(
                TAG,
                "onCreateView: signed in = " + PreferenceManager.getDefaultSharedPreferences(context)
                        .getBoolean(Utility.SIGNED_IN, false))
        checkStoragePermissions()

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mMainActivity!!.setupToolbarTitle(R.string.activity_homepage)
        sign_in_button!!.setSize(SignInButton.SIZE_WIDE)
        mMainActivity!!.setTabVisible(false)

        imageView1.setOnClickListener { mMainActivity!!.drawer!!.openDrawer() }

        sign_in_button.visibility = if (PreferenceManager.getDefaultSharedPreferences(context)
                        .getBoolean(Utility.SIGNED_IN, false))
            View.INVISIBLE
        else
            View.VISIBLE

        sign_in_button.setOnClickListener {
            mMainActivity!!.setShowSnackbar()
            mMainActivity!!.signIn()
        }

        Log.d(TAG, "getVersionCodeWrapper(): ${getVersionCodeWrapper()}")

        ChangelogBuilder()
                .withUseBulletList(true) // true if you want to show bullets before each changelog row, false otherwise
                .withMinVersionToShow(getVersionCodeWrapper())     // provide a number and the log will only show changelog rows for versions equal or higher than this number
                .withManagedShowOnStart(context!!.getSharedPreferences("com.michaelflisar.changelog", 0).getInt("changelogVersion", -1) != -1)  // library will take care to show activity/dialog only if the changelog has new infos and will only show this new infos
                .withTitle(getString(R.string.dialog_change_title)) // provide a custom title if desired, default one is "Changelog <VERSION>"
                .withOkButtonLabel(getString(R.string.ok)) // provide a custom ok button text if desired, default one is "OK"
                .buildAndShowDialog(mMainActivity, ThemeUtils.isDarkMode(mMainActivity!!)) // second parameter defines, if the dialog has a dark or light theme
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(activity!!).registerReceiver(signInVisibility, IntentFilter(BROADCAST_SIGNIN_VISIBLE))
        LocalBroadcastManager.getInstance(activity!!).registerReceiver(signInVisibility, IntentFilter(BROADCAST_SIGNIN_VISIBLE))
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(activity!!).unregisterReceiver(signInVisibility)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

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
        PreferenceManager.getDefaultSharedPreferences(context).edit { putString(Utility.SAVE_LOCATION, "0") }
        Snackbar.make(rootView!!, getString(R.string.external_storage_denied), Snackbar.LENGTH_SHORT)
                .show()
    }

    @TargetApi(Build.VERSION_CODES.P)
    private fun getVersionCodeP(): Int {
        return activity!!
                .packageManager
                .getPackageInfo(activity!!.packageName, 0)
                .longVersionCode.toInt()
    }

    @Suppress("DEPRECATION")
    private fun getVersionCodeLegacy(): Int {
        return activity!!
                .packageManager
                .getPackageInfo(activity!!.packageName, 0)
                .versionCode
    }

    private fun getVersionCodeWrapper(): Int {
        return if (LUtils.hasP())
            getVersionCodeP()
        else
            getVersionCodeLegacy()
    }

    companion object {
        private val TAG = Risuscito::class.java.canonicalName
        const val BROADCAST_SIGNIN_VISIBLE = "it.cammino.risuscito.signin.SIGNIN_VISIBLE"
        const val DATA_VISIBLE = "it.cammino.risuscito.signin.data.DATA_VISIBLE"
    }
}
