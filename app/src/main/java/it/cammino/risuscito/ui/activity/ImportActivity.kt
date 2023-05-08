package it.cammino.risuscito.ui.activity

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.work.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import it.cammino.risuscito.R
import it.cammino.risuscito.services.XmlImportService
import it.cammino.risuscito.ui.RisuscitoApplication
import it.cammino.risuscito.utils.extension.capitalize
import it.cammino.risuscito.viewmodels.ImportActivityViewModel

class ImportActivity : AppCompatActivity() {

    private val mViewModel: ImportActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data = intent.data
        if (data != null) {
            Log.d(TAG, "onCreate: data = $data")
            Log.d(TAG, "onCreate: schema = " + data.scheme)

            intent.data = null
            MaterialAlertDialogBuilder(this).apply {
                setTitle(R.string.app_name)
                setMessage(R.string.dialog_import)
                setPositiveButton(getString(R.string.import_confirm).capitalize(context.resources)) { _, _ ->
                    val builder = Data.Builder()
                    builder.putString(XmlImportService.TAG_IMPORT_DATA, data.toString())
                    val blurRequest = OneTimeWorkRequestBuilder<XmlImportService>()
                        .setInputData(builder.build())
                        .addTag(ImportActivityViewModel.TAG_IMPORT_JOB)
                        .build()
                    mViewModel.workManager.enqueueUniqueWork(
                        ImportActivityViewModel.TAG_IMPORT_JOB,
                        ExistingWorkPolicy.REPLACE,
                        blurRequest
                    )
                }
                setNegativeButton(getString(R.string.cancel).capitalize(context.resources)) { _, _ ->
                    finish()
                }
                setCancelable(false)

                setOnKeyListener { arg0, keyCode, event ->
                    if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                        arg0.dismiss()
                        finish()
                        true
                    } else
                        false
                }
            }.show()
            mViewModel.outputWorkInfos.observe(this, workInfosObserver())
        }

    }

    // Define the observer function
    private fun workInfosObserver(): Observer<List<WorkInfo>> {
        return Observer { listOfWorkInfo ->

            // This code could be in a Transformation in the ViewModel; they are included here
            // so that the entire process of displaying a WorkInfo is in one location.

            // If there are no matching work info, do nothing
            if (listOfWorkInfo.isNullOrEmpty()) {
                return@Observer
            }

            // We only care about the one output status.
            // Every continuation has only one worker tagged TAG_OUTPUT
            val workInfo = listOfWorkInfo[0]

            if (workInfo.state == WorkInfo.State.RUNNING)
                mViewModel.running = true

            if (workInfo.state.isFinished) {
                Log.d(TAG, "workInfo.state.isFinished")
                if (mViewModel.running) {
                    mViewModel.running = false
                    finish()
                }
            }
//            else {
//                showWorkInProgress()
//            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        Log.d(TAG, "attachBaseContext")
        super.attachBaseContext(RisuscitoApplication.localeManager.useCustomConfig(newBase))
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        Log.d(TAG, "applyOverrideConfiguration")
        super.applyOverrideConfiguration(
            RisuscitoApplication.localeManager.updateConfigurationIfSupported(
                this,
                overrideConfiguration
            )
        )
    }

    companion object {
        internal val TAG = ImportActivity::class.java.canonicalName
    }

}
