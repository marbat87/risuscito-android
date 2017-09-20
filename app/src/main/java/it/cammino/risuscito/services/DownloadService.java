package it.cammino.risuscito.services;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadService extends IntentService {
    // The tag we put on debug messages
    final String TAG = getClass().getName();
    public static final String ACTION_DOWNLOAD = "it.cammino.risuscito.services.action.ACTION_DOWNLOAD";
    public static final String ACTION_CANCEL = "it.cammino.risuscito.services.action.ACTION_CANCEL";
    public static final String BROADCAST_DOWNLOAD_ERROR = "it.cammino.risuscito.services.broadcast.BROADCAST_DOWNLOAD_ERROR";
    public static final String BROADCAST_DOWNLOAD_COMPLETED = "it.cammino.risuscito.services.broadcast.BROADCAST_DOWNLOAD_COMPLETED";
    public static final String BROADCAST_DOWNLOAD_CANCELLED = "it.cammino.risuscito.services.broadcast.BROADCAST_DOWNLOAD_CANCELLED";
    public static final String BROADCAST_DOWNLOAD_PROGRESS = "it.cammino.risuscito.services.broadcast.BROADCAST_DOWNLOAD_PROGRESS";
    public static final String DATA_DESTINATION_FILE = "it.cammino.risuscito.services.data.DATA_DESTINATION_FILE";
    public static final String DATA_PROGRESS = "it.cammino.risuscito.services.data.DATA_PROGRESS";
    public static final String DATA_ERROR = "it.cammino.risuscito.services.data.DATA_ERROR";

    public DownloadService() {
        super("DownloadService");
    }

    private BroadcastReceiver cancelBRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            isCancelled = true;
        }
    };

    /**
     * This method is invoked on the worker thread with a request to process.
     * Only one Intent is processed at a time, but the processing happens on a
     * worker thread that runs independently from other application logic.
     * So, if this code takes a long time, it will hold up other requests to
     * the same IntentService, but it will not hold up anything else.
     * When all requests have been handled, the IntentService stops itself,
     * so you should not call {@link #stopSelf}.
     *
     * @param intent The value passed to {@link
     *               Context#startService(Intent)}.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        isCancelled = false;
        registerReceiver(cancelBRec, new IntentFilter(ACTION_CANCEL));
        startSaving(intent);
    }

    boolean isCancelled;

    void startSaving(Intent intent) {
        String uri = intent.getData().toString();
        String mPath = intent.getStringExtra(DATA_DESTINATION_FILE);
        Log.d(TAG, "startSaving DATA " + uri);
        Log.d(TAG, "startSaving: DATA_DESTINATION_FILE " + mPath);

        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                getClass().getName());
        wakelock.acquire(30000);

        try {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(uri);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
//                    return "Server returned HTTP " + connection.getResponseCode()
//                            + " " + connection.getResponseMessage();
                    unregisterReceiver(cancelBRec);
                    String erroreMessage = "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                    Log.e(TAG, "Sending broadcast notification: " + BROADCAST_DOWNLOAD_ERROR);
                    Log.e(TAG, "Sending broadcast notification: " + DATA_ERROR + ": " + erroreMessage);
                    Intent intentBroadcast = new Intent(BROADCAST_DOWNLOAD_ERROR);
                    intentBroadcast.putExtra(DATA_ERROR, erroreMessage);
                    sendBroadcast(intentBroadcast);
                    return;
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
//                output = new FileOutputStream(sUrl[1]);
                output = new FileOutputStream(intent.getStringExtra(DATA_DESTINATION_FILE));
//                    Log.i(PaginaRenderActivity.this.getClass().toString(), "URL[1]:" + sUrl[1]);

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled) {
                        unregisterReceiver(cancelBRec);
                        try {
                            if (output != null)
                                output.close();
                            if (input != null)
                                input.close();
                            File fileToDelete = new File(mPath);
                            fileToDelete.delete();
                        } catch (IOException ignored) {
                            Log.e(getClass().toString(), ignored.getLocalizedMessage(), ignored);
                        }
                        if (connection != null)
                            connection.disconnect();

                        Log.d(TAG, "Sending broadcast notification: " + BROADCAST_DOWNLOAD_CANCELLED);
                        sendBroadcast(new Intent(BROADCAST_DOWNLOAD_CANCELLED));
                        return;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) {// only if total length is known
//                        publishProgress((int) (total * 100 / fileLength));
                        int progress = (int) total * 100 / fileLength;
                        Log.d(TAG, "Sending broadcast notification: " + BROADCAST_DOWNLOAD_PROGRESS);
                        Log.d(TAG, "Sending broadcast notification: " + DATA_PROGRESS + ": " + progress);
                        Intent intentBroadcast = new Intent(BROADCAST_DOWNLOAD_PROGRESS);
                        intentBroadcast.putExtra(DATA_PROGRESS, progress);
                        sendBroadcast(intentBroadcast);
                    }
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                unregisterReceiver(cancelBRec);
                Log.e(getClass().toString(), e.getLocalizedMessage(), e);
                Log.e(TAG, "Sending broadcast notification: " + BROADCAST_DOWNLOAD_ERROR);
                Log.e(TAG, "Sending broadcast notification: " + DATA_ERROR + ": " + e.toString());
                Intent intentBroadcast = new Intent(BROADCAST_DOWNLOAD_ERROR);
                intentBroadcast.putExtra(DATA_ERROR, e.toString());
                sendBroadcast(intentBroadcast);
                return;
//                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                }
                catch (IOException ignored) {
//                        ignored.printStackTrace();
                    Log.e(getClass().toString(), ignored.getLocalizedMessage(), ignored);
                }

                if (connection != null)
                    connection.disconnect();
            }
        } finally {
            wakelock.release();
        }
        unregisterReceiver(cancelBRec);
        Log.d(TAG, "Sending broadcast notification: " + BROADCAST_DOWNLOAD_COMPLETED);
        sendBroadcast(new Intent(BROADCAST_DOWNLOAD_COMPLETED));
    }

}