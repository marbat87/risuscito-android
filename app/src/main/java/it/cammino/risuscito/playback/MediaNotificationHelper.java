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

package it.cammino.risuscito.playback;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.app.NotificationCompat.MediaStyle;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import it.cammino.risuscito.R;

/**
 * Helper class for building Media style Notifications from a
 * {@link android.support.v4.media.session.MediaSessionCompat}.
 */
class MediaNotificationHelper {

    private static final String CHANNEL_ID = "itcr_media_playback_channel";

    private MediaNotificationHelper() {
        // Helper utility class; do not instantiate.
    }

    static Notification createNotification(Context context,
                                           MediaSessionCompat mediaSession) {

        //Crezione notification channel per Android O
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createChannel(context);

        MediaControllerCompat controller = mediaSession.getController();
        MediaMetadataCompat mMetadata = controller.getMetadata();
        PlaybackStateCompat mPlaybackState = controller.getPlaybackState();

        if (mMetadata == null || mPlaybackState == null) {
            return null;
        }

        boolean isPlaying = mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING;
        NotificationCompat.Action actionPlayPause = isPlaying
                ? new NotificationCompat.Action(R.drawable.notification_pause,
                    context.getString(R.string.label_pause),
                    MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                            PlaybackStateCompat.ACTION_PAUSE))
                : new NotificationCompat.Action(R.drawable.notification_play,
                    context.getString(R.string.label_play),
                    MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                            PlaybackStateCompat.ACTION_PLAY));

        MediaDescriptionCompat description = mMetadata.getDescription();
        Bitmap art = description.getIconBitmap();
        if (art == null) {
            // use a placeholder art while the remote art is being downloaded.
            art = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.ic_launcher_144dp);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID);
        notificationBuilder
                .setStyle(new MediaStyle()
                        // show only play/pause in compact view.
                        .setShowActionsInCompactView(new int[]{0})
                        .setMediaSession(mediaSession.getSessionToken()))
                .addAction(actionPlayPause)
                .setSmallIcon(R.drawable.ic_notification_music)
                .setShowWhen(false)
                .setContentIntent(controller.getSessionActivity())
                .setContentTitle(description.getTitle())
                .setContentText(description.getSubtitle())
                .setLargeIcon(art)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        if (mPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING || mPlaybackState.getState() == PlaybackStateCompat.STATE_PAUSED) {
            NotificationCompat.Action actionRestart = new NotificationCompat.Action(R.drawable.notification_restart,
                    context.getString(R.string.label_restart),
                    MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS));
            notificationBuilder.addAction(actionRestart);
        }

        return notificationBuilder.build();
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private static void createChannel(Context context) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // The id of the channel.
//        String id = CHANNEL_ID;
        // The user-visible name of the channel.
        CharSequence name = "Media playback";
        // The user-visible description of the channel.
        String description = "Media playback controls";
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
        // Configure the notification channel.
        mChannel.setDescription(description);
        mChannel.setShowBadge(false);
        mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        if (mNotificationManager != null) {
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }
}
