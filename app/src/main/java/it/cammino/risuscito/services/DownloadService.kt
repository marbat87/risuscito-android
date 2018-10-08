package it.cammino.risuscito.services

import android.app.IntentService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.util.Log
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

class DownloadService : IntentService("DownloadService") {

    private val cancelBRec = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            isCancelled = true
        }
    }

    internal var isCancelled: Boolean = false

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
        isCancelled = false
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(cancelBRec, IntentFilter(ACTION_CANCEL))
        startSaving(intent)
    }

    private fun startSaving(intent: Intent?) {
        val uri = intent!!.data!!.toString()
        val mPath = intent.getStringExtra(DATA_DESTINATION_FILE)
        Log.d(TAG, "startSaving DATA $uri")
        Log.d(TAG, "startSaving: DATA_DESTINATION_FILE $mPath")

        val pm = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                javaClass.name)
        wakelock.acquire(30000)

        try {
            var input: InputStream? = null
            var output: OutputStream? = null
            var connection: HttpURLConnection? = null
            try {
                val url = URL(uri)
                connection = url.openConnection() as HttpURLConnection
                connection.connect()

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(cancelBRec)
                    val erroreMessage = ("Server returned HTTP " + connection.responseCode
                            + " " + connection.responseMessage)
                    Log.e(TAG, "Sending broadcast notification: $BROADCAST_DOWNLOAD_ERROR")
                    Log.e(TAG, "Sending broadcast notification: $DATA_ERROR: $erroreMessage")
                    val intentBroadcast = Intent(BROADCAST_DOWNLOAD_ERROR)
                    intentBroadcast.putExtra(DATA_ERROR, erroreMessage)
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intentBroadcast)
                    return
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                val fileLength = connection.contentLength

                // download the file
                input = connection.inputStream
                output = FileOutputStream(intent.getStringExtra(DATA_DESTINATION_FILE))
                //                    Log.i(PaginaRenderActivity.this.getClass().toString(), "URL[1]:" + sUrl[1]);

                val data = ByteArray(4096)
                var total: Long = 0
                var count = input!!.read(data)
                while (count != -1) {
                    // allow canceling with back button
                    if (isCancelled) {
                        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(cancelBRec)
                        try {
                            @Suppress("UNNECESSARY_SAFE_CALL")
                            output?.close()
                            @Suppress("UNNECESSARY_SAFE_CALL")
                            input?.close()
                            val fileToDelete = File(mPath)
                            fileToDelete.delete()
                        } catch (ignored: IOException) {
                            Log.e(javaClass.toString(), ignored.localizedMessage, ignored)
                        }

                        @Suppress("UNNECESSARY_SAFE_CALL")
                        connection?.disconnect()

                        Log.d(TAG, "Sending broadcast notification: $BROADCAST_DOWNLOAD_CANCELLED")
                        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(Intent(BROADCAST_DOWNLOAD_CANCELLED))
                        return
                    }
                    total += count.toLong()
                    // publishing the progress....
                    if (fileLength > 0) {// only if total length is known
                        val progress = total.toInt() * 100 / fileLength
                        Log.v(TAG, "Sending broadcast notification: $BROADCAST_DOWNLOAD_PROGRESS")
                        Log.v(TAG, "Sending broadcast notification: $DATA_PROGRESS: $progress")
                        val intentBroadcast = Intent(BROADCAST_DOWNLOAD_PROGRESS)
                        intentBroadcast.putExtra(DATA_PROGRESS, progress)
                        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intentBroadcast)
                    }
                    output.write(data, 0, count)
                    count = input.read(data)
                }
            } catch (e: Exception) {
                LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(cancelBRec)
                Log.e(javaClass.toString(), e.localizedMessage, e)
                Log.e(TAG, "Sending broadcast notification: $BROADCAST_DOWNLOAD_ERROR")
                Log.e(TAG, "Sending broadcast notification: " + DATA_ERROR + ": " + e.toString())
                val intentBroadcast = Intent(BROADCAST_DOWNLOAD_ERROR)
                intentBroadcast.putExtra(DATA_ERROR, e.toString())
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intentBroadcast)
                return
            } finally {
                try {
                    output?.close()
                    input?.close()
                } catch (ignored: IOException) {
                    Log.e(javaClass.toString(), ignored.localizedMessage, ignored)
                }

                connection?.disconnect()
            }
        } finally {
            if (wakelock.isHeld)
                wakelock.release()
        }
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(cancelBRec)
        Log.d(TAG, "Sending broadcast notification: $BROADCAST_DOWNLOAD_COMPLETED")
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(Intent(BROADCAST_DOWNLOAD_COMPLETED))
    }

    companion object {
        internal val TAG = DownloadService::class.java.name
        const val ACTION_DOWNLOAD = "it.cammino.risuscito.services.action.ACTION_DOWNLOAD"
        const val ACTION_CANCEL = "it.cammino.risuscito.services.action.ACTION_CANCEL"
        const val BROADCAST_DOWNLOAD_ERROR = "it.cammino.risuscito.services.broadcast.BROADCAST_DOWNLOAD_ERROR"
        const val BROADCAST_DOWNLOAD_COMPLETED = "it.cammino.risuscito.services.broadcast.BROADCAST_DOWNLOAD_COMPLETED"
        const val BROADCAST_DOWNLOAD_CANCELLED = "it.cammino.risuscito.services.broadcast.BROADCAST_DOWNLOAD_CANCELLED"
        const val BROADCAST_DOWNLOAD_PROGRESS = "it.cammino.risuscito.services.broadcast.BROADCAST_DOWNLOAD_PROGRESS"
        const val DATA_DESTINATION_FILE = "it.cammino.risuscito.services.data.DATA_DESTINATION_FILE"
        const val DATA_PROGRESS = "it.cammino.risuscito.services.data.DATA_PROGRESS"
        const val DATA_ERROR = "it.cammino.risuscito.services.data.DATA_ERROR"
    }

}