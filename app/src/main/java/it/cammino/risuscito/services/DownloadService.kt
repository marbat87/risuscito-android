package it.cammino.risuscito.services

import android.annotation.TargetApi
import android.app.IntentService
import android.content.*
import android.os.Build
import android.os.PowerManager
import android.provider.MediaStore
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import it.cammino.risuscito.LUtils.Companion.hasQ
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class DownloadService : IntentService("DownloadService") {

    private val cancelBRec = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            isCancelled = true
        }
    }

    internal var isCancelled: Boolean = false

    override fun onHandleIntent(intent: Intent?) {
        isCancelled = false
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(cancelBRec, IntentFilter(ACTION_CANCEL))
        startSaving(intent)
    }

    private fun startSaving(intent: Intent?) {
        val uri = intent?.data?.toString()
        val mPath = intent?.getStringExtra(DATA_DESTINATION_FILE) ?: ""
        val mExternal = intent?.getBooleanExtra(DATA_EXTERNAL_DOWNLOAD, false) ?: false
        Log.d(TAG, "startSaving DATA $uri")
        Log.d(TAG, "startSaving: DATA_DESTINATION_FILE $mPath")

        val pm = applicationContext.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val wakelock = pm?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                javaClass.name)
        wakelock?.acquire(30000)

        var input: InputStream? = null
        var connection: HttpURLConnection? = null

        try {

            val url = URL(uri)
            connection = url.openConnection() as? HttpURLConnection
            connection?.connect()

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection?.responseCode != HttpURLConnection.HTTP_OK) {
                LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(cancelBRec)
                val erroreMessage = ("Server returned HTTP ${connection?.responseCode} ${connection?.responseMessage}")
                Log.e(TAG, "Sending broadcast notification: $BROADCAST_DOWNLOAD_ERROR")
                Log.e(TAG, "Sending broadcast notification: $DATA_ERROR: $erroreMessage")
                val intentBroadcast = Intent(BROADCAST_DOWNLOAD_ERROR)
                intentBroadcast.putExtra(DATA_ERROR, erroreMessage)
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intentBroadcast)
                return
            }

            val fileLength = connection.contentLength

            // download the file
            input = connection.inputStream

            if (mExternal && hasQ())
                startSavingO(input, mPath, fileLength)
            else
                startSavingLegacy(input, mPath, fileLength)

        } finally {
            try {
                input?.close()
            } catch (ignored: IOException) {
                Log.e(javaClass.toString(), ignored.localizedMessage, ignored)
            }
            connection?.disconnect()
            if (wakelock?.isHeld == true)
                wakelock.release()
        }
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(cancelBRec)
        Log.d(TAG, "Sending broadcast notification: $BROADCAST_DOWNLOAD_COMPLETED")
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(Intent(BROADCAST_DOWNLOAD_COMPLETED))
    }

    @TargetApi(Build.VERSION_CODES.Q)
    private fun startSavingO(input: InputStream?, mPath: String, fileLength: Int) {
        val resolver = contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, mPath)
            put(MediaStore.Audio.Media.TITLE, "Risuscitò$mPath")
//                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/Risuscitò")
            put(MediaStore.Audio.Media.IS_PENDING, 1)
        }

        val collection = MediaStore.Audio.Media
                .getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val mUri = resolver.insert(collection, contentValues)

        mUri?.let {
            Log.d(TAG, "mUri ${it.path}")
            Log.d(TAG, "mId ${ContentUris.parseId(it)}")
            resolver.openOutputStream(it).use { outputStream ->
                try {
                    val data = ByteArray(4096)
                    var total: Long = 0
                    var count = input?.read(data) ?: 0
                    while (count != -1) {
                        // allow canceling with back button
                        if (isCancelled) {
                            LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(cancelBRec)
                            try {
                                outputStream?.close()
                            } catch (ignored: IOException) {
                                Log.e(javaClass.toString(), ignored.localizedMessage, ignored)
                            }

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
                        outputStream?.write(data, 0, count)
                        count = input?.read(data) ?: 0
                    }
                } catch (e: Exception) {
                    LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(cancelBRec)
                    Log.e(javaClass.toString(), e.localizedMessage, e)
                    Log.e(TAG, "Sending broadcast notification: $BROADCAST_DOWNLOAD_ERROR")
                    Log.e(TAG, "Sending broadcast notification: $DATA_ERROR: $e")
                    val intentBroadcast = Intent(BROADCAST_DOWNLOAD_ERROR)
                    intentBroadcast.putExtra(DATA_ERROR, e.toString())
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intentBroadcast)
//                    return
                } finally {
                    try {
                        outputStream?.close()
                    } catch (ignored: IOException) {
                        Log.e(javaClass.toString(), ignored.localizedMessage, ignored)
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.Audio.Media.IS_PENDING, 0)
                    resolver.update(it, contentValues, null, null)
                }
            }
        }
    }

    private fun startSavingLegacy(input: InputStream?, mPath: String, fileLength: Int) {
        val output = FileOutputStream(mPath)
        try {
            val data = ByteArray(4096)
            var total: Long = 0
            var count = input?.read(data) ?: 0
            while (count != -1) {
                // allow canceling with back button
                if (isCancelled) {
                    LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(cancelBRec)
                    try {
                        output.close()
                        mPath.let { File(it).delete() }
                    } catch (ignored: IOException) {
                        Log.e(javaClass.toString(), ignored.localizedMessage, ignored)
                    }

                    Log.d(TAG, "Sending broadcast notification: $BROADCAST_DOWNLOAD_CANCELLED")
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(Intent(BROADCAST_DOWNLOAD_CANCELLED))
                    break
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
                count = input?.read(data) ?: 0
            }
        } catch (e: Exception) {
            LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(cancelBRec)
            Log.e(javaClass.toString(), e.localizedMessage, e)
            Log.e(TAG, "Sending broadcast notification: $BROADCAST_DOWNLOAD_ERROR")
            Log.e(TAG, "Sending broadcast notification: $DATA_ERROR: $e")
            val intentBroadcast = Intent(BROADCAST_DOWNLOAD_ERROR)
            intentBroadcast.putExtra(DATA_ERROR, e.toString())
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intentBroadcast)
        } finally {
            try {
                output.close()
            } catch (ignored: IOException) {
                Log.e(javaClass.toString(), ignored.localizedMessage, ignored)
            }
        }
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
        const val DATA_EXTERNAL_DOWNLOAD = "it.cammino.risuscito.services.data.DATA_EXTERNAL_DOWNLOAD"
    }

}