package it.cammino.risuscito.services

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.Consegnato

class ConsegnatiSaverService : JobIntentService() {

    override fun onHandleWork(intent: Intent) {
        startSaving(intent)
    }

    private fun startSaving(intent: Intent?) {
        val ids = intent?.getIntegerArrayListExtra(IDS_CONSEGNATI)
        var i = 0
        val mDao = RisuscitoDatabase.getInstance(applicationContext).consegnatiDao()
        val consegnati = ArrayList<Consegnato>()
        ids?.let {
            for (id in it) {
                val tempConsegnato = Consegnato()
                tempConsegnato.idConsegnato = ++i
                tempConsegnato.idCanto = id
                tempConsegnato.numPassaggio = mDao.getNumPassaggio(id)
                consegnati.add(tempConsegnato)
                try {
                    mDao.insertConsegnati(tempConsegnato)
                    Log.d(TAG, "Sending broadcast notification: $BROADCAST_SINGLE_COMPLETED")
                    Log.d(TAG, "Sending broadcast notification: $BROADCAST_SINGLE_COMPLETED - DONE = $i - $id")
                    val intentBroadcast = Intent(BROADCAST_SINGLE_COMPLETED)
                    intentBroadcast.putExtra(DATA_DONE, i)
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intentBroadcast)
                } catch (e: Exception) {
                    Log.e(TAG, "ERRORE INSERT:", e)
                    FirebaseCrashlytics.getInstance().recordException(e)
                }

            }
        }
        mDao.emptyConsegnati()
        mDao.insertConsegnati(consegnati)
        Log.d(TAG, "Sending broadcast notification: $BROADCAST_SAVING_COMPLETED")
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(Intent(BROADCAST_SAVING_COMPLETED))
    }

    companion object {
        internal val TAG = ConsegnatiSaverService::class.java.canonicalName
        private const val JOB_ID = 1000
        const val BROADCAST_SAVING_COMPLETED = "it.cammino.risuscito.services.broadcast.SAVING_COMPLETED"
        const val BROADCAST_SINGLE_COMPLETED = "it.cammino.risuscito.services.broadcast.SINGLE_COMPLETED"
        const val DATA_DONE = "it.cammino.risuscito.services.data.DATA_DONE"
        const val IDS_CONSEGNATI = "it.cammino.risuscito.services.data.IDS_CONSEGNATI"

        /**
         * Convenience method for enqueuing work in to this service.
         */
        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, ConsegnatiSaverService::class.java, JOB_ID, work)
        }
    }

}