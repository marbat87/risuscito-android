package it.cammino.risuscito.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

import it.cammino.risuscito.DatabaseCanti;

public class ConsegnatiSaverService extends IntentService {
    // The tag we put on debug messages
    final String TAG = getClass().getName();
    public static final String BROADCAST_SAVING_COMPLETED = "it.cammino.risuscito.services.broadcast.SAVING_COMPLETED";
    public static final String BROADCAST_SINGLE_COMPLETED = "it.cammino.risuscito.services.broadcast.SINGLE_COMPLETED";
    public static final String DATA_DONE = "it.cammino.risuscito.services.data.DATA_DONE";
    public static final String IDS_CONSEGNATI = "it.cammino.risuscito.services.data.IDS_CONSEGNATI";

    public ConsegnatiSaverService() {
        super("ConsegnatiSaver");
    }

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
        startSaving(intent);
    }

    void startSaving(Intent intent) {
        ArrayList<Integer> ids = intent.getIntegerArrayListExtra(IDS_CONSEGNATI);
        DatabaseCanti privateListaCanti = new DatabaseCanti(getApplicationContext());
        SQLiteDatabase db = privateListaCanti.getReadableDatabase();
        db.delete("CANTI_CONSEGNATI", "", null);
        int i = 0;

        for (Integer id: ids) {
            String sql = "INSERT INTO CANTI_CONSEGNATI" +
                    "       (_id, id_canto)" +
                    "   SELECT COALESCE(MAX(_id) + 1,1), " + id +
                    "             FROM CANTI_CONSEGNATI";

            try {
                db.execSQL(sql);
                i++;
                Log.d(TAG, "Sending broadcast notification: " + BROADCAST_SINGLE_COMPLETED);
                Log.d(TAG, "Sending broadcast notification: " + BROADCAST_SINGLE_COMPLETED + " - DONE = " + i);
                Intent intentBroadcast = new Intent(BROADCAST_SINGLE_COMPLETED);
                intentBroadcast.putExtra(DATA_DONE, i);
                sendBroadcast(intentBroadcast);
            } catch (SQLException e) {
                Log.e(getClass().toString(), "ERRORE INSERT:");
                e.printStackTrace();
            }
        }
        db.close();
        if (privateListaCanti != null)
            privateListaCanti.close();
        Log.d(TAG, "Sending broadcast notification: " + BROADCAST_SAVING_COMPLETED);
        sendBroadcast(new Intent(BROADCAST_SAVING_COMPLETED));
    }

}