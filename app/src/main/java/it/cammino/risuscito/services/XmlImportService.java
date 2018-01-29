package it.cammino.risuscito.services;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.util.Xml;

import com.crashlytics.android.Crashlytics;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

import it.cammino.risuscito.LUtils;
import it.cammino.risuscito.ListaPersonalizzata;
import it.cammino.risuscito.R;
import it.cammino.risuscito.database.RisuscitoDatabase;
import it.cammino.risuscito.database.dao.ListePersDao;
import it.cammino.risuscito.database.entities.ListaPers;

public class XmlImportService extends IntentService {

  public static final String ACTION_URL = "it.cammino.risuscito.import.action.URL";
  public static final String ACTION_FINISH = "it.cammino.risuscito.import.action.URL";
  private static final String CHANNEL_ID = "itcr_import_channel";
  // We don't use namespaces
  private static final String ns = null;
  final int NOTIFICATION_ID = 2;
  final String TAG = getClass().getCanonicalName();

  public XmlImportService() {
    super("XmlImportService");
  }

  /**
   * This method is invoked on the worker thread with a request to process. Only one Intent is
   * processed at a time, but the processing happens on a worker thread that runs independently from
   * other application logic. So, if this code takes a long time, it will hold up other requests to
   * the same IntentService, but it will not hold up anything else. When all requests have been
   * handled, the IntentService stops itself, so you should not call {@link #stopSelf}.
   *
   * @param intent The value passed to {@link Context#startService(Intent)}.
   */
  @Override
  protected void onHandleIntent(Intent intent) {
    Log.d(TAG, "onHandleIntent: Starting");
    Uri data = intent.getData();
    if (data != null) {
      intent.setData(null);
      importData(data);
    }
  }

  private void importData(Uri data) {
    Log.d(TAG, "importData: data = " + data.toString());
    Log.d(TAG, "importData:  data.getScheme = " + data.getScheme());
    final String scheme = data.getScheme();

    NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(this);
    mNotificationManager.cancelAll();
    Notification mNotification;

    if (LUtils.hasO()) createChannel();

    mNotification =
        new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentTitle(getString(R.string.app_name))
            .setProgress(0, 0, true)
            .setContentText(getString(R.string.import_running))
            .build();

    mNotificationManager.notify(NOTIFICATION_ID, mNotification);

    if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
      try {
        InputStream fis = getContentResolver().openInputStream(data);
        ListaPersonalizzata celebrazione = parse(fis);

        if (celebrazione != null) {
          //                    DatabaseCanti listaCanti = new DatabaseCanti(this);
          //
          //                    SQLiteDatabase db = listaCanti.getReadableDatabase();
          //
          //                    ContentValues values = new ContentValues();
          //                    values.put("titolo_lista", celebrazione.getName());
          //                    values.put("lista",
          // ListaPersonalizzata.serializeObject(celebrazione));
          //
          //                    db.insert("LISTE_PERS", "", values);
          //
          //                    db.close();

          ListePersDao mDao = RisuscitoDatabase.getInstance(this).listePersDao();
          ListaPers listaPers = new ListaPers();
          listaPers.titolo = celebrazione.getName();
          listaPers.lista = celebrazione;
          mDao.insertLista(listaPers);

          mNotification =
              new NotificationCompat.Builder(this, CHANNEL_ID)
                  .setSmallIcon(R.drawable.ic_stat_action_done)
                  .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                  .setContentTitle(getString(R.string.app_name))
                  .setContentText(getString(R.string.import_done))
                  .build();

          mNotificationManager.notify(NOTIFICATION_ID, mNotification);

          Log.d(TAG, "Sending broadcast notification: ACTION_FINISH");
          Intent intentBroadcast = new Intent(ACTION_FINISH);
          sendBroadcast(intentBroadcast);

          Intent i =
              getBaseContext()
                  .getPackageManager()
                  .getLaunchIntentForPackage(getBaseContext().getPackageName());
          if (i != null) {
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
          }
          startActivity(i);
          stopSelf();
        } else {
          mNotification =
              new NotificationCompat.Builder(this, CHANNEL_ID)
                  .setSmallIcon(R.drawable.ic_stat_alert_error)
                  .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                  .setContentTitle(getString(R.string.app_name))
                  .setTicker(getString(R.string.import_error))
                  .setContentText(getString(R.string.import_error))
                  .build();
          mNotificationManager.notify(NOTIFICATION_ID, mNotification);

          Log.d(TAG, "Sending broadcast notification: ACTION_FINISH");
          Intent intentBroadcast = new Intent(ACTION_FINISH);
          sendBroadcast(intentBroadcast);

          stopSelf();
        }
      } catch (XmlPullParserException | SecurityException | IOException e) {
        Log.e(TAG, "importData: " + e.getLocalizedMessage(), e);
        //        FirebaseCrash.log("importData: " + e.getMessage());
        Crashlytics.logException(e);
        mNotification =
            new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_alert_error)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle(getString(R.string.app_name))
                .setTicker(getString(R.string.import_error))
                .setContentText(getString(R.string.import_error))
                .build();
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);

        Log.d(TAG, "Sending broadcast notification: ACTION_FINISH");
        Intent intentBroadcast = new Intent(ACTION_FINISH);
        sendBroadcast(intentBroadcast);

        stopSelf();
      }
    }
  }

  public ListaPersonalizzata parse(InputStream in) throws XmlPullParserException, IOException {
    try {
      XmlPullParser parser = Xml.newPullParser();
      parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
      parser.setInput(in, null);
      parser.nextTag();
      return readLista(parser);
    } finally {
      in.close();
    }
  }

  private ListaPersonalizzata readLista(XmlPullParser parser)
      throws XmlPullParserException, IOException {
    ListaPersonalizzata list = new ListaPersonalizzata();
    Position tempPos;

    parser.require(XmlPullParser.START_TAG, ns, "list");
    String title = parser.getAttributeValue(null, "title");
    if (title != null) list.setName(parser.getAttributeValue(null, "title"));
    else {
      Log.e(TAG, "readLista: title is null");
      //      FirebaseCrash.log("importData: title is null");
      Crashlytics.log("importData: title is null");
      return null;
    }
    while (parser.next() != XmlPullParser.END_TAG) {
      if (parser.getEventType() != XmlPullParser.START_TAG) {
        continue;
      }
      String name = parser.getName();
      // Starts by looking for the entry tag
      if (name.equals("position")) {
        tempPos = readPosition(parser);
        list.addPosizione(tempPos.getName());
        if (!tempPos.getCanto().equalsIgnoreCase("0"))
          list.addCanto(tempPos.getCanto(), list.getNumPosizioni() - 1);
      } else {
        skip(parser);
      }
    }
    return list;
  }

  // Processes positions tags in the list.
  private Position readPosition(XmlPullParser parser) throws IOException, XmlPullParserException {
    Position result = new Position();
    parser.require(XmlPullParser.START_TAG, ns, "position");
    String name = parser.getAttributeValue(null, "name");
    String canto = readCanto(parser);
    parser.require(XmlPullParser.END_TAG, ns, "position");
    result.setName(name.trim());
    result.setCanto(canto.trim());
    return result;
  }

  // For the tags title and summary, extracts their text values.
  private String readCanto(XmlPullParser parser) throws IOException, XmlPullParserException {
    String result = "";
    if (parser.next() == XmlPullParser.TEXT) {
      result = parser.getText();
      parser.nextTag();
    }
    return result;
  }

  private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
    if (parser.getEventType() != XmlPullParser.START_TAG) {
      throw new IllegalStateException();
    }
    int depth = 1;
    while (depth != 0) {
      switch (parser.next()) {
        case XmlPullParser.END_TAG:
          depth--;
          break;
        case XmlPullParser.START_TAG:
          depth++;
          break;
      }
    }
  }

  @TargetApi(Build.VERSION_CODES.O)
  private void createChannel() {
    NotificationManager mNotificationManager =
        (NotificationManager)
            getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
    // The id of the channel.
    //        String id = CHANNEL_ID;
    // The user-visible name of the channel.
    CharSequence name = "XML Import";
    // The user-visible description of the channel.
    String description = "Importing selected XML";
    NotificationChannel mChannel =
        new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
    // Configure the notification channel.
    mChannel.setDescription(description);
    mChannel.setShowBadge(false);
    mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
    if (mNotificationManager != null) {
      mNotificationManager.createNotificationChannel(mChannel);
    }
  }

  private static class Position {
    String name;
    String canto;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getCanto() {
      return canto;
    }

    public void setCanto(String canto) {
      this.canto = canto;
    }
  }
}
