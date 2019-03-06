package it.cammino.risuscito.services

import android.app.IntentService
import android.app.Notification
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.util.Xml
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.crashlytics.android.Crashlytics
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

class XmlImportService : IntentService("XmlImportService") {

    /**
     * This method is invoked on the worker thread with a request to process. Only one Intent is
     * processed at a time, but the processing happens on a worker thread that runs independently from
     * other application logic. So, if this code takes a long time, it will hold up other requests to
     * the same IntentService, but it will not hold up anything else. When all requests have been
     * handled, the IntentService stops itself, so you should not call [.stopSelf].
     *
     * @param intent The value passed to [Context.startService].
     */
    override fun onHandleIntent(intent: Intent?) {
        Log.d(TAG, "onHandleIntent: Starting")
        val data = intent!!.data
        if (data != null) {
            intent.data = null
            importData(data)
        }
    }

    private fun importData(data: Uri) {
        Log.d(TAG, "importData: data = $data")
        Log.d(TAG, "importData:  data.getScheme = " + data.scheme)
        val scheme = data.scheme

        val mNotificationManager = NotificationManagerCompat.from(this)
        mNotificationManager.cancelAll()
        var mNotification: Notification

//        if (LUtils.hasO()) createChannel()
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
                            .setSmallIcon(R.drawable.ic_stat_action_done)
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                            .setContentTitle(getString(R.string.app_name))
                            .setContentText(getString(R.string.import_done))
                            .build()

                    mNotificationManager.notify(NOTIFICATION_ID, mNotification)

                    Log.d(TAG, "Sending broadcast notification: ACTION_FINISH")
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
                            .setSmallIcon(R.drawable.ic_stat_alert_error)
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                            .setContentTitle(getString(R.string.app_name))
                            .setTicker(getString(R.string.import_error))
                            .setContentText(getString(R.string.import_error))
                            .build()
                    mNotificationManager.notify(NOTIFICATION_ID, mNotification)

                    Log.d(TAG, "Sending broadcast notification: ACTION_FINISH")
                    val intentBroadcast = Intent(ACTION_FINISH)
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intentBroadcast)

                    stopSelf()
                }
            } catch (e: XmlPullParserException) {
                Log.e(TAG, "importData: " + e.localizedMessage, e)
                Crashlytics.logException(e)
                mNotification = NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_stat_alert_error)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setContentTitle(getString(R.string.app_name))
                        .setTicker(getString(R.string.import_error))
                        .setContentText(getString(R.string.import_error))
                        .build()
                mNotificationManager.notify(NOTIFICATION_ID, mNotification)

                Log.d(TAG, "Sending broadcast notification: ACTION_FINISH")
                val intentBroadcast = Intent(ACTION_FINISH)
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intentBroadcast)

                stopSelf()
            } catch (e: SecurityException) {
                Log.e(TAG, "importData: " + e.localizedMessage, e)
                Crashlytics.logException(e)
                mNotification = NotificationCompat.Builder(this, CHANNEL_ID).setSmallIcon(R.drawable.ic_stat_alert_error).setVisibility(NotificationCompat.VISIBILITY_PUBLIC).setContentTitle(getString(R.string.app_name)).setTicker(getString(R.string.import_error)).setContentText(getString(R.string.import_error)).build()
                mNotificationManager.notify(NOTIFICATION_ID, mNotification)
                Log.d(TAG, "Sending broadcast notification: ACTION_FINISH")
                val intentBroadcast = Intent(ACTION_FINISH)
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intentBroadcast)
                stopSelf()
            } catch (e: IOException) {
                Log.e(TAG, "importData: " + e.localizedMessage, e)
                Crashlytics.logException(e)
                mNotification = NotificationCompat.Builder(this, CHANNEL_ID).setSmallIcon(R.drawable.ic_stat_alert_error).setVisibility(NotificationCompat.VISIBILITY_PUBLIC).setContentTitle(getString(R.string.app_name)).setTicker(getString(R.string.import_error)).setContentText(getString(R.string.import_error)).build()
                mNotificationManager.notify(NOTIFICATION_ID, mNotification)
                Log.d(TAG, "Sending broadcast notification: ACTION_FINISH")
                val intentBroadcast = Intent(ACTION_FINISH)
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intentBroadcast)
                stopSelf()
            }

        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun parse(`in`: InputStream?): ListaPersonalizzata? {
        try {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(`in`, null)
            parser.nextTag()
            return readLista(parser)
        } finally {
            `in`!!.close()
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
            Crashlytics.log("importData: title is null")
            return null
        }
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            val name = parser.name
            // Starts by looking for the entry tag
            if (name == "position") {
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
        parser.require(XmlPullParser.START_TAG, ns, "position")
        val name = parser.getAttributeValue(null, "name")
        val canto = readCanto(parser)
        parser.require(XmlPullParser.END_TAG, ns, "position")
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
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }

//    @TargetApi(Build.VERSION_CODES.O)
//    private fun createChannel() {
//        val mNotificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        // The id of the channel.
//        //        String id = CHANNEL_ID;
//        // The user-visible name of the channel.
//        val name = "XML Import"
//        // The user-visible description of the channel.
//        val description = "Importing selected XML"
//        val mChannel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW)
//        // Configure the notification channel.
//        mChannel.description = description
//        mChannel.setShowBadge(false)
//        mChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
//        mNotificationManager.createNotificationChannel(mChannel)
//    }

    private class Position {
        lateinit var name: String
        lateinit var canto: String
    }

    companion object {
        internal const val NOTIFICATION_ID = 2
        internal val TAG = XMLFilterImpl::class.java.canonicalName
        const val ACTION_URL = "it.cammino.risuscito.import.action.URL"
        const val ACTION_FINISH = "it.cammino.risuscito.import.action.URL"
        private const val CHANNEL_ID = "itcr_import_channel"
        // We don't use namespaces
        private val ns: String? = null
    }
}
