package it.cammino.risuscito

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.afollestad.materialdialogs.MaterialDialog
import it.cammino.risuscito.services.XmlImportService
import it.cammino.risuscito.ui.RisuscitoApplication

class ImportActivity : AppCompatActivity() {

    private val importFinishBRec = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //Implement UI change code here once notification is received
            Log.d(javaClass.name, "ACTION_FINISH")
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data = intent.data
        if (data != null) {
            Log.d(TAG, "onCreate: data = $data")
            Log.d(TAG, "onCreate: schema = " + data.scheme)
            intent.data = null
            MaterialDialog(this)
                    .show {
                        title(R.string.app_name)
                        message(R.string.dialog_import)
                        positiveButton(R.string.import_confirm) {
                            val i = Intent(this@ImportActivity, XmlImportService::class.java)
                            i.action = XmlImportService.ACTION_URL
                            i.data = data
                            XmlImportService.enqueueWork(applicationContext, i)
                        }
                        negativeButton(R.string.cancel) {
                            finish()
                        }
                        cancelable(false)
                        cancelOnTouchOutside(false)
                    }
                    .setOnKeyListener { arg0, keyCode, event ->
                        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                            arg0.dismiss()
                            finish()
                            true
                        } else
                            false
                    }
            //registra un receiver per ricevere la notifica di completamento import e potersi terminare
            LocalBroadcastManager.getInstance(applicationContext).registerReceiver(importFinishBRec, IntentFilter(
                    XmlImportService.ACTION_FINISH))
        }

    }

    public override fun onDestroy() {
        try {
            LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(importFinishBRec)
        } catch (e: IllegalArgumentException) {
            Log.e(javaClass.name, e.localizedMessage, e)
        }

        super.onDestroy()
    }

    override fun attachBaseContext(newBase: Context) {
        Log.d(TAG, "attachBaseContext")
        super.attachBaseContext(RisuscitoApplication.localeManager.useCustomConfig(newBase))
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        Log.d(TAG, "applyOverrideConfiguration")
        super.applyOverrideConfiguration(RisuscitoApplication.localeManager.updateConfigurationIfSupported(this, overrideConfiguration))
    }

    companion object {
        internal val TAG = ImportActivity::class.java.canonicalName
    }

}
