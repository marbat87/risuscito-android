package it.cammino.risuscito.services

import android.app.Notification
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.util.Xml
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import it.cammino.risuscito.ListaPersonalizzata
import it.cammino.risuscito.R
import it.cammino.risuscito.Utility
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.entities.ListaPers
import org.xml.sax.helpers.XMLFilterImpl
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream

class XmlImportService : JobIntentService() {

    override fun onHandleWork(intent: Intent) {
        Log.d(TAG, "onHandleWork: Starting")
        val data = intent.data
        data?.let {
            intent.data = null
            importData(it)
        }
    }

    private fun importData(data: Uri) {
        Log.d(TAG, "$TAG_IMPORT_DATA: data = $data")
        Log.d(TAG, "$TAG_IMPORT_DATA:  data.getScheme = ${data.scheme}")
        val scheme = data.scheme

        val mNotificationManager = NotificationManagerCompat.from(this)
        mNotificationManager.cancelAll()
        var mNotification: Notification

        Utility.createNotificationChannelWrapper(applicationContext, CHANNEL_ID, "XML Import", "Importing selected XML")

        mNotification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle(getString(R.string.app_name))
                .setProgress(0, 0, true)
                .setContentText(getString(R.string.import_running))
                .build()

        mNotificationManager.notify(NOTIFICATION_ID, mNotification)

        if (ContentResolver.SCHEME_CONTENT == scheme) {
            try {
                val fis = contentResolver.openInputStream(data)
                val celebrazione = parse(fis)

                if (celebrazione != null) {
                    val mDao = RisuscitoDatabase.getInstance(this).listePersDao()
                    val listaPers = ListaPers()
                    listaPers.titolo = celebrazione.name
                    listaPers.lista = celebrazione
                    mDao.insertLista(listaPers)

                    mNotification = NotificationCompat.Builder(this, CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_notification_done)
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                            .setContentTitle(getString(R.string.app_name))
                            .setContentText(getString(R.string.import_done))
                            .build()

                    mNotificationManager.notify(NOTIFICATION_ID, mNotification)

                    Log.d(TAG, ACTION_FINISH)
                    val intentBroadcast = Intent(ACTION_FINISH)
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intentBroadcast)

                    val i = baseContext
                            .packageManager
                            .getLaunchIntentForPackage(baseContext.packageName)
                    i?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(i)
                    stopSelf()
                } else {
                    mNotification = NotificationCompat.Builder(this, CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_notification_error)
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                            .setContentTitle(getString(R.string.app_name))
                            .setTicker(getString(R.string.import_error))
                            .setContentText(getString(R.string.import_error))
                            .build()
                    mNotificationManager.notify(NOTIFICATION_ID, mNotification)

                    Log.d(TAG, ACTION_FINISH)
                    val intentBroadcast = Intent(ACTION_FINISH)
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intentBroadcast)

                    stopSelf()
                }
            } catch (e: XmlPullParserException) {
                Log.e(TAG, TAG_IMPORT_DATA, e)
                FirebaseCrashlytics.getInstance().recordException(e)
                mNotification = NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification_error)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setContentTitle(getString(R.string.app_name))
                        .setTicker(getString(R.string.import_error))
                        .setContentText(getString(R.string.import_error))
                        .build()
                mNotificationManager.notify(NOTIFICATION_ID, mNotification)

                Log.d(TAG, ACTION_FINISH)
                val intentBroadcast = Intent(ACTION_FINISH)
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intentBroadcast)

                stopSelf()
            } catch (e: SecurityException) {
                Log.e(TAG, TAG_IMPORT_DATA, e)
                FirebaseCrashlytics.getInstance().recordException(e)
                mNotification = NotificationCompat.Builder(this, CHANNEL_ID).setSmallIcon(R.drawable.ic_notification_error).setVisibility(NotificationCompat.VISIBILITY_PUBLIC).setContentTitle(getString(R.string.app_name)).setTicker(getString(R.string.import_error)).setContentText(getString(R.string.import_error)).build()
                mNotificationManager.notify(NOTIFICATION_ID, mNotification)
                Log.d(TAG, ACTION_FINISH)
                val intentBroadcast = Intent(ACTION_FINISH)
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intentBroadcast)
                stopSelf()
            } catch (e: IOException) {
                Log.e(TAG, TAG_IMPORT_DATA, e)
                FirebaseCrashlytics.getInstance().recordException(e)
                mNotification = NotificationCompat.Builder(this, CHANNEL_ID).setSmallIcon(R.drawable.ic_notification_error).setVisibility(NotificationCompat.VISIBILITY_PUBLIC).setContentTitle(getString(R.string.app_name)).setTicker(getString(R.string.import_error)).setContentText(getString(R.string.import_error)).build()
                mNotificationManager.notify(NOTIFICATION_ID, mNotification)
                Log.d(TAG, ACTION_FINISH)
                val intentBroadcast = Intent(ACTION_FINISH)
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intentBroadcast)
                stopSelf()
            }

        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun parse(inputStream: InputStream?): ListaPersonalizzata? {
        inputStream.use {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(it, null)
            parser.nextTag()
            return readLista(parser)
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readLista(parser: XmlPullParser): ListaPersonalizzata? {
        val list = ListaPersonalizzata()
        var tempPos: Position

        parser.require(XmlPullParser.START_TAG, ns, "list")
        val title = parser.getAttributeValue(null, "title")
        if (title != null)
            list.name = parser.getAttributeValue(null, "title")
        else {
            Log.e(TAG, "readLista: title is null")
            //      FirebaseCrash.log("importData: title is null");
            FirebaseCrashlytics.getInstance().log("$TAG_IMPORT_DATA: title is null")
        }
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            val name = parser.name
            // Starts by looking for the entry tag
            if (name == POSITION_TAG) {
                tempPos = readPosition(parser)
                list.addPosizione(tempPos.name)
                if (!tempPos.canto.equals("0", ignoreCase = true))
                    list.addCanto(tempPos.canto, list.numPosizioni - 1)
            } else {
                skip(parser)
            }
        }
        return list
    }

    // Processes positions tags in the list.
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readPosition(parser: XmlPullParser): Position {
        val result = Position()
        parser.require(XmlPullParser.START_TAG, ns, POSITION_TAG)
        val name = parser.getAttributeValue(null, "name")
        val canto = readCanto(parser)
        parser.require(XmlPullParser.END_TAG, ns, POSITION_TAG)
        result.name = name.trim { it <= ' ' }
        result.canto = canto.trim { it <= ' ' }
        return result
    }

    // For the tags title and summary, extracts their text values.
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readCanto(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        check(parser.eventType == XmlPullParser.START_TAG)
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }

    private class Position {
        lateinit var name: String
        lateinit var canto: String
    }

    companion object {
        internal const val NOTIFICATION_ID = 2
        internal val TAG = XmlImportService::class.java.canonicalName
        internal const val TAG_IMPORT_DATA = "importData"
        private const val JOB_ID = 5000
        const val ACTION_URL = "it.cammino.risuscito.import.action.URL"
        const val ACTION_FINISH = "it.cammino.risuscito.import.action.URL"
        private const val CHANNEL_ID = "itcr_import_channel"
        private const val POSITION_TAG = "position"

        // We don't use namespaces
        private val ns: String? = null

        /**
         * Convenience method for enqueuing work in to this service.
         */
        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, XMLFilterImpl::class.java, JOB_ID, work)
        }
    }
}
