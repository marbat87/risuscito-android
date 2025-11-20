package it.cammino.risuscito.ui.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Observer
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import it.cammino.risuscito.R
import it.cammino.risuscito.services.XmlImportService
import it.cammino.risuscito.ui.composable.dialogs.SimpleAlertDialog
import it.cammino.risuscito.ui.composable.dialogs.SimpleDialogTag
import it.cammino.risuscito.ui.composable.theme.RisuscitoTheme
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

            setContent {

                val showAlertDialog = remember { mutableStateOf(true) }

                RisuscitoTheme {
                    if (showAlertDialog.value) {
                        SimpleAlertDialog(
                            onDismissRequest = {
                                showAlertDialog.value = false
                                finish()
                            },
                            onConfirmation = { _ ->
                                showAlertDialog.value = false
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
                            },
                            dialogTitle = stringResource(R.string.app_name),
                            dialogText = stringResource(R.string.dialog_import),
                            iconRes = R.drawable.file_open_24px,
                            confirmButtonText = stringResource(R.string.import_confirm).capitalize(
                                LocalContext.current
                            ),
                            dismissButtonText = stringResource(R.string.cancel).capitalize(
                                LocalContext.current
                            ),
                            dialogTag = SimpleDialogTag.FILE_IMPORT
                        )
                    }
                }
            }

//            MaterialAlertDialogBuilder(this).apply {
//                setTitle(R.string.app_name)
//                setMessage(R.string.dialog_import)
//                setPositiveButton(getString(R.string.import_confirm).capitalize(context)) { _, _ ->
//                    val builder = Data.Builder()
//                    builder.putString(XmlImportService.TAG_IMPORT_DATA, data.toString())
//                    val blurRequest = OneTimeWorkRequestBuilder<XmlImportService>()
//                        .setInputData(builder.build())
//                        .addTag(ImportActivityViewModel.TAG_IMPORT_JOB)
//                        .build()
//                    mViewModel.workManager.enqueueUniqueWork(
//                        ImportActivityViewModel.TAG_IMPORT_JOB,
//                        ExistingWorkPolicy.REPLACE,
//                        blurRequest
//                    )
//                }
//                setNegativeButton(getString(R.string.cancel).capitalize(context)) { _, _ ->
//                    finish()
//                }
//                setCancelable(false)
//
//                setOnKeyListener { arg0, keyCode, event ->
//                    if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
//                        arg0.dismiss()
//                        finish()
//                        true
//                    } else
//                        false
//                }
//            }.show()
            mViewModel.outputWorkInfos.observe(this, workInfosObserver())
        }

    }

    // Define the observer function
    private fun workInfosObserver(): Observer<List<WorkInfo>> {
        return Observer { listOfWorkInfo ->

            // This code could be in a Transformation in the ViewModel; they are included here
            // so that the entire process of displaying a WorkInfo is in one location.

            // If there are no matching work info, do nothing
            if (listOfWorkInfo.isEmpty()) {
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
        }
    }

    companion object {
        internal val TAG = ImportActivity::class.java.canonicalName
    }

}
