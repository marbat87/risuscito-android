package it.cammino.risuscito.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.work.WorkInfo
import androidx.work.WorkManager

class ImportActivityViewModel(application: Application) : AndroidViewModel(application) {

    // New instance variable for the WorkInfo
    internal val outputWorkInfos: LiveData<List<WorkInfo>>
    internal var running = false

    internal val workManager = WorkManager.getInstance(application)

    init {
        outputWorkInfos = workManager.getWorkInfosByTagLiveData(TAG_IMPORT_JOB)
    }

    companion object {
        internal const val TAG_IMPORT_JOB = "LIST_IMPORT_JOB"
    }

}
