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

package it.cammino.risuscito.playback

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.preference.PreferenceManager
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import it.cammino.risuscito.LUtils
import it.cammino.risuscito.R
import it.cammino.risuscito.Utility
import it.cammino.risuscito.database.RisuscitoDatabase
import it.cammino.risuscito.database.dao.CantoDao
import it.cammino.risuscito.ui.ThemeableActivity
import pub.devrel.easypermissions.EasyPermissions
import java.util.*

@Suppress("unused")
/**
 * Utility class to get a list of MusicTrack's based on a server-side JSON configuration.
 *
 *
 * In a real application this class may pull data from a remote server, as we do here, or
 * potentially use [android.provider.MediaStore] to locate media files located on the device.
 */
class MusicProvider internal constructor(private val mContext: Context) {

    // Categorized caches for music track data:
    private val mMusicListById: LinkedHashMap<String, MediaMetadataCompat> = LinkedHashMap()
    @Volatile private var mCurrentState = State.NON_INITIALIZED
    private val mDao: CantoDao = RisuscitoDatabase.getInstance(mContext).cantoDao()

    val allMusics: Iterable<MediaMetadataCompat>
        get() = if (mCurrentState != State.INITIALIZED || mMusicListById.isEmpty()) {
            emptyList()
        } else mMusicListById.values

    val isInitialized: Boolean
        get() = mCurrentState == State.INITIALIZED

    /**
     * Return the MediaMetadata for the given musicID.
     *
     * @param musicId The unique music ID.
     */
    fun getMusic(musicId: String): MediaMetadataCompat? {
        return if (mMusicListById.containsKey(musicId)) mMusicListById[musicId] else null
    }

    /**
     * Update the metadata associated with a musicId. If the musicId doesn't exist, the update is
     * dropped. (That is, it does not create a new mediaId.)
     *
     * @param musicId The ID
     * @param metadata New Metadata to associate with it
     */
    @Synchronized fun updateMusic(musicId: String, metadata: MediaMetadataCompat) {
        val track = mMusicListById[musicId]
        if (track != null) {
            mMusicListById[musicId] = metadata
        }
    }

    /**
     * Get the list of music tracks from a server and caches the track information for future
     * reference, keying tracks by musicId and grouping by genre.
     */
    @SuppressLint("StaticFieldLeak")
    fun retrieveMediaAsync(callback: Callback?) {
        Log.d(TAG, "retrieveMediaAsync called")
        if (mCurrentState == State.INITIALIZED) {
            // Already initialized, so call back immediately.
            callback!!.onMusicCatalogReady(true)
            return
        }

        // Asynchronously load the music catalog in a separate thread
        object : AsyncTask<Void, Void, State>() {
            override fun doInBackground(vararg params: Void): State {
                retrieveMedia()
                return mCurrentState
            }

            override fun onPostExecute(current: State) {
                callback?.onMusicCatalogReady(current == State.INITIALIZED)
            }
        }.execute()
    }

    @Synchronized private fun retrieveMedia() {
        if (mCurrentState == State.NON_INITIALIZED) {
            mCurrentState = State.INITIALIZING

            Log.d(TAG, "LINGUA CONTEXT: " + ThemeableActivity.getSystemLocalWrapper(mContext.resources.configuration).language)
            Log.d(TAG, "LINGUA PREFERENCE: " + PreferenceManager.getDefaultSharedPreferences(mContext).getString(Utility.SYSTEM_LANGUAGE, ""))

            var mNewBase = mContext
            if (ThemeableActivity.getSystemLocalWrapper(mContext.resources.configuration)
                            .language != PreferenceManager.getDefaultSharedPreferences(mContext).getString(Utility.SYSTEM_LANGUAGE, "")) {
                val config = Configuration()
                val locale = Locale(PreferenceManager.getDefaultSharedPreferences(mContext).getString(Utility.SYSTEM_LANGUAGE, "it"))
                Locale.setDefault(locale)
                ThemeableActivity.setSystemLocalWrapper(config, locale)
                if (LUtils.hasJB()) {
                    mNewBase = mNewBase.createConfigurationContext(config)
                } else {
                    @Suppress("DEPRECATION")
                    mNewBase.resources.updateConfiguration(config, mNewBase.resources.displayMetrics)
                }
            }

//            val art = BitmapFactory.decodeResource(mNewBase.resources, R.drawable.ic_launcher_144dp)
            val art = decodeSampledBitmapFromResource(mNewBase.resources, R.drawable.ic_launcher_144dp, 320, 320)
            val artSmall = BitmapFactory.decodeResource(mNewBase.resources, R.mipmap.ic_launcher)

            val canti = mDao.allByWithLink
            Log.d(TAG, "retrieveMedia: " + canti.size)

            var temp: MediaMetadataCompat

            for (canto in canti) {
                Log.d(
                        TAG,
                        "retrieveMedia: "
                                + canto.id
                                + " / "
                                + mNewBase.resources.getString(LUtils.getResId(canto.titolo, R.string::class.java))
                                + " / "
                                + if (LUtils.getResId(canto.link, R.string::class.java) != - 1) mNewBase.resources.getString(LUtils.getResId(canto.link, R.string::class.java)) else canto.link)

                var url = if (LUtils.getResId(canto.link, R.string::class.java) != - 1) mNewBase.resources.getString(LUtils.getResId(canto.link, R.string::class.java)) else canto.link
                if (EasyPermissions.hasPermissions(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // ho il permesso di scrivere la memoria esterna, quindi cerco il file anche lì
                    if (!Utility.retrieveMediaFileLink(mContext, url!!, true).isEmpty())
                        url = Utility.retrieveMediaFileLink(mContext, url, true)
                } else {
                    if (!Utility.retrieveMediaFileLink(mContext, url!!, false).isEmpty())
                        url = Utility.retrieveMediaFileLink(mContext, url, false)
                }

                Log.v(TAG, "retrieveMedia: " + canto.id + " / " + mNewBase.resources.getString(LUtils.getResId(canto.titolo, R.string::class.java)) + " / " + url)

                if (!url.isEmpty()) {
                    temp = MediaMetadataCompat.Builder()
                            .putString(
                                    MediaMetadataCompat.METADATA_KEY_MEDIA_ID, canto.id.toString())
                            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, url)
                            .putString(
                                    MediaMetadataCompat.METADATA_KEY_ALBUM, mNewBase.getString(R.string.app_name))
                            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Kiko Arguello")
                            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 0)
                            .putString(MediaMetadataCompat.METADATA_KEY_GENRE, "Sacred")
                            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, "")
                            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, mNewBase.resources.getString(LUtils.getResId(canto.titolo, R.string::class.java)))
                            .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, canto.id.toLong())
                            .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, canti.size.toLong())
                            // Set high resolution bitmap in METADATA_KEY_ALBUM_ART. This is
                            //                                // used, for example, on the lockscreen
                            // background when the media
                            //                                // session is active.
                            .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, art)
                            //
                            //                                // Set small version of the album art in the
                            // DISPLAY_ICON. This is
                            //                                // used on the MediaDescription and thus it
                            // should be small to be
                            //                                // serialized if necessary.
                            .putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, artSmall)
                            .build()

                    mMusicListById[canto.id.toString()] = temp
                }
            }

            mCurrentState = State.INITIALIZED
            if (mCurrentState != State.INITIALIZED) {
                // Something bad happened, so we reset state to NON_INITIALIZED to allow
                // retries (eg if the network connection is temporary unavailable)
                mCurrentState = State.NON_INITIALIZED
            }
        }
    }

    private enum class State {
        NON_INITIALIZED,
        INITIALIZING,
        INITIALIZED
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    private fun decodeSampledBitmapFromResource(
            res: Resources,
            resId: Int,
            reqWidth: Int,
            reqHeight: Int
    ): Bitmap {
        // First decode with inJustDecodeBounds=true to check dimensions
        return BitmapFactory.Options().run {
            inJustDecodeBounds = true
            BitmapFactory.decodeResource(res, resId, this)

            // Calculate inSampleSize
            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)

            // Decode bitmap with inSampleSize set
            inJustDecodeBounds = false

            BitmapFactory.decodeResource(res, resId, this)
        }
    }

    /** Callback used by MusicService.  */
    interface Callback {
        fun onMusicCatalogReady(success: Boolean)
    }

    companion object {

        const val MEDIA_ID_ROOT = "__ROOT__"
        const val MEDIA_ID_EMPTY_ROOT = "__EMPTY__"
        private val TAG = MusicProvider::class.java.simpleName
    }
}
