/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.cammino.risuscito.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;

import it.cammino.risuscito.DatabaseCanti;
import it.cammino.risuscito.ListaPersonalizzata;
import it.cammino.risuscito.R;
import it.cammino.risuscito.utils.LogHelper;

/**
 * Utility class to get a list of MusicTrack's based on a server-side JSON
 * configuration.
 */
public class DatabaseMusicSource implements MusicProviderSource {

    //    private static final String TAG = LogHelper.makeLogTag(DatabaseMusicSource.class);
    private final String TAG = getClass().getCanonicalName();


    Context mContext;
    private DatabaseCanti listaCanti;


    public DatabaseMusicSource(Context context) {
        this.mContext = context;
        listaCanti = new DatabaseCanti(mContext);
    }

    @Override
    public Iterator<MediaMetadataCompat> iterator() {
        SQLiteDatabase db = listaCanti.getReadableDatabase();

        String query = "SELECT a._id, a.titolo, coalesce(b.local_path, a.link) " +
                "  FROM ELENCO A" +
                "  LEFT JOIN LOCAL_LINKS B" +
                "  ON (A._id = b._id)";
        Cursor cursor = db.rawQuery(query, null);
        Log.d(TAG, "iterator: " + cursor.getCount());

        ArrayList<MediaMetadataCompat> tracks = new ArrayList<>();
        MediaMetadataCompat temp;

        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            Log.d(TAG, "iterator: " + cursor.getInt(0) +  " / " + cursor.getString(1) + " / " + cursor.getString(2));
            temp =  new MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, cursor.getString(0))
                    .putString(MusicProviderSource.CUSTOM_METADATA_TRACK_SOURCE, cursor.getString(2))
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, mContext.getString(R.string.app_name))
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Kiko Arguello")
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 0)
                    .putString(MediaMetadataCompat.METADATA_KEY_GENRE, "Sacred")
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, "")
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, cursor.getString(1))
                    .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, Integer.parseInt(cursor.getString(0)))
                    .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, cursor.getCount())
                    .build();
            tracks.add(temp);
            cursor.moveToNext();
        }

        cursor.close();
        db.close();

        return tracks.iterator();
    }

}
