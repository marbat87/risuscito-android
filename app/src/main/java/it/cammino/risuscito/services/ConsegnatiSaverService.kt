package it.cammino.risuscito.services

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.Consegnato

class ConsegnatiSaverService : IntentService("ConsegnatiSaver") {

    /**
     * This method is invoked on the worker thread with a request to process.
     * Only one Intent is processed at a time, but the processing happens on a
     * worker thread that runs independently from other application logic.
     * So, if this code takes a long time, it will hold up other requests to
     * the same IntentService, but it will not hold up anything else.
     * When all requests have been handled, the IntentService stops itself,
     * so you should not call [.stopSelf].
     *
     * @param intent The value passed to [               ][Context.startService].
     */
    override fun onHandleIntent(intent: Intent?) {
        startSaving(intent)
    }

    private fun startSaving(intent: Intent?) {
        val ids = intent!!.getIntegerArrayListExtra(IDS_CONSEGNATI)
        var i = 0
        val mDao = RisuscitoDatabase.getInstance(applicationContext).consegnatiDao()
        mDao.emptyConsegnati()

        for (id in ids) {
            val tempConsegnato = Consegnato()
            tempConsegnato.idConsegnato = ++i
            tempConsegnato.idCanto = id
            try {
                mDao.insertConsegnati(tempConsegnato)
                Log.d(TAG, "Sending broadcast notification: $BROADCAST_SINGLE_COMPLETED")
                Log.d(TAG, "Sending broadcast notification: $BROADCAST_SINGLE_COMPLETED - DONE = $i - $id")
                val intentBroadcast = Intent(BROADCAST_SINGLE_COMPLETED)
                intentBroadcast.putExtra(DATA_DONE, i)
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intentBroadcast)
            } catch (e: Exception) {
                Log.e(javaClass.toString(), "ERRORE INSERT:")
                e.printStackTrace()
            }

        }
        Log.d(TAG, "Sending broadcast notification: $BROADCAST_SAVING_COMPLETED")
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(Intent(BROADCAST_SAVING_COMPLETED))
    }

    companion object {
        internal val TAG = ConsegnatiSaverService::class.java.canonicalName
        const val BROADCAST_SAVING_COMPLETED = "it.cammino.risuscito.services.broadcast.SAVING_COMPLETED"
        const val BROADCAST_SINGLE_COMPLETED = "it.cammino.risuscito.services.broadcast.SINGLE_COMPLETED"
        const val DATA_DONE = "it.cammino.risuscito.services.data.DATA_DONE"
        const val IDS_CONSEGNATI = "it.cammino.risuscito.services.data.IDS_CONSEGNATI"
    }

}