package it.cammino.risuscito.utils

import android.annotation.TargetApi
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import it.cammino.risuscito.LUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class Downloader(val activity: FragmentActivity) {

    private val viewModel: DownloaderViewModel = ViewModelProvider(activity).get(DownloaderViewModel::class.java)

    private var isCancelled = false

    fun cancel() {
        Log.d(TAG, "cancel")
        isCancelled = true
    }

    fun startSaving(sourceUrl: String?, destinationPath: String, isExternal: Boolean) {
        isCancelled = false
        Log.d(TAG, "startSaving sourceUrl $sourceUrl")
        Log.d(TAG, "startSaving destinationPath $destinationPath")
        Log.d(TAG, "startSaving isExternal $isExternal")

        val pm = activity.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val wakelock = pm?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                javaClass.name)
        wakelock?.acquire(30000)

        var input: InputStream? = null
        var connection: HttpURLConnection? = null

        try {

            val url = URL(sourceUrl)
            connection = url.openConnection() as? HttpURLConnection
            connection?.connect()

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection?.responseCode != HttpURLConnection.HTTP_OK) {
                val errorMessage = ("Server returned HTTP ${connection?.responseCode} ${connection?.responseMessage}")
                viewModel.handled = false
                viewModel.state.postValue(DownloadState.Error(errorMessage))
                return
            }

            val fileLength = connection.contentLength

            // download the file
            input = connection.inputStream

            if (isExternal && LUtils.hasQ())
                startSavingO(input, destinationPath, fileLength)
            else
                startSavingLegacy(input, destinationPath, fileLength)

        } finally {
            try {
                input?.close()
            } catch (ignored: IOException) {
                Log.e(TAG, "startSaving", ignored)
            }
            connection?.disconnect()
            if (wakelock?.isHeld == true)
                wakelock.release()
        }

        if (!isCancelled) {
            viewModel.handled = false
            viewModel.state.postValue(DownloadState.Completed())
        }
    }

    @TargetApi(Build.VERSION_CODES.Q)
    private fun startSavingO(input: InputStream?, mPath: String, fileLength: Int) {
        val resolver = activity.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, mPath)
            put(MediaStore.Audio.Media.TITLE, "Risuscitò$mPath")
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
                            Log.d(TAG, "$TAG_O isCancelled")
                            try {
                                outputStream?.close()
                            } catch (ignored: IOException) {
                                Log.e(TAG, TAG_O, ignored)
                            }
                            return
                        }
                        total += count.toLong()
                        // publishing the progress....
                        if (fileLength > 0) {// only if total length is known
                            val progress = total.toInt() * 100 / fileLength
                            Log.d(TAG, "$TAG_O progress: $progress")
                            viewModel.handled = false
                            viewModel.state.postValue(DownloadState.Progress(progress))
                        }
                        outputStream?.write(data, 0, count)
                        count = input?.read(data) ?: 0
                    }
                } catch (e: Exception) {
                    Log.e(TAG, TAG_O, e)
                    viewModel.handled = false
                    viewModel.state.postValue(DownloadState.Error(e.message ?: GENERIC_ERROR))
                } finally {
                    try {
                        outputStream?.close()
                    } catch (ignored: IOException) {
                        Log.e(TAG, TAG_O, ignored)
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
                    Log.d(TAG, "$TAG_LEGACY isCancelled")
                    try {
                        output.close()
                        mPath.let { File(it).delete() }
                    } catch (ignored: IOException) {
                        Log.e(TAG, TAG_LEGACY, ignored)
                    }
                    return
                }
                total += count.toLong()
                // publishing the progress....
                if (fileLength > 0) {// only if total length is known
                    val progress = total.toInt() * 100 / fileLength
                    Log.d(TAG, "$TAG_LEGACY progress: $progress")
                    viewModel.handled = false
                    viewModel.state.postValue(DownloadState.Progress(progress))
                }
                output.write(data, 0, count)
                count = input?.read(data) ?: 0
            }
        } catch (e: Exception) {
            Log.e(TAG, TAG_LEGACY, e)
            viewModel.handled = false
            viewModel.state.postValue(DownloadState.Error(e.message ?: GENERIC_ERROR))
        } finally {
            try {
                output.close()
            } catch (ignored: IOException) {
                Log.e(TAG, TAG_LEGACY, ignored)
            }
        }
    }

    class DownloaderViewModel : ViewModel() {
        var handled = true
        val state = MutableLiveData<DownloadState>()
    }

    companion object {
        internal val TAG = Downloader::class.java.canonicalName
        internal const val TAG_LEGACY = "startSavingLegacy"
        internal const val TAG_O = "startSavingO"
        private const val GENERIC_ERROR = "Generic download Error!"
    }
}