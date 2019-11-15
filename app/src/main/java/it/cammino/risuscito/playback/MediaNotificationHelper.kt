package it.cammino.risuscito.playback


import android.app.Notification
import android.content.Context
import android.graphics.BitmapFactory
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media.session.MediaButtonReceiver
import com.mikepenz.iconics.dsl.iconicsDrawable
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import com.mikepenz.iconics.utils.toAndroidIconCompat
import it.cammino.risuscito.R
import it.cammino.risuscito.Utility

/**
 * Helper class for building Media style Notifications from a
 * [android.support.v4.media.session.MediaSessionCompat].
 */
internal object MediaNotificationHelper {

    private const val CHANNEL_ID = "itcr_media_playback_channel"

    fun createNotification(context: Context,
                           mediaSession: MediaSessionCompat?): Notification? {

        //Crezione notification channel per Android O
        Utility.createNotificationChannelWrapper(context, CHANNEL_ID, "Media playback", "Media playback controls")

        val controller = mediaSession?.controller
        val mMetadata = controller?.metadata
        val mPlaybackState = controller?.playbackState

        if (mMetadata == null || mPlaybackState == null) {
            return null
        }

        val isPlaying = mPlaybackState.state == PlaybackStateCompat.STATE_PLAYING
        val iconPause = context.iconicsDrawable(CommunityMaterial.Icon2.cmd_pause) {
            color = colorRes(R.color.ic_notification_color)
            size = sizeDp(24)
            padding = sizeDp(2)
        }
        val iconPlay = context.iconicsDrawable(CommunityMaterial.Icon2.cmd_play) {
            color = colorRes(R.color.ic_notification_color)
            size = sizeDp(24)
            padding = sizeDp(2)
        }
        val actionPlayPause = if (isPlaying)
            NotificationCompat.Action(iconPause.toAndroidIconCompat(),
                    context.getString(R.string.label_pause),
                    MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                            PlaybackStateCompat.ACTION_PAUSE))
        else
            NotificationCompat.Action(iconPlay.toAndroidIconCompat(),
                    context.getString(R.string.label_play),
                    MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                            PlaybackStateCompat.ACTION_PLAY))

        val description = mMetadata.description
        var art = description.iconBitmap
        if (art == null) {
            // use a placeholder art while the remote art is being downloaded.
            art = BitmapFactory.decodeResource(context.resources,
                    R.drawable.ic_launcher_144dp)
        }

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
        notificationBuilder
                .setStyle(MediaStyle()
                        // show only play/pause in compact view.
                        .setShowActionsInCompactView(0)
                        .setMediaSession(mediaSession.sessionToken))
                .addAction(actionPlayPause)
                .setSmallIcon(R.drawable.ic_notification_music)
                .setShowWhen(false)
                .setContentIntent(controller.sessionActivity)
                .setContentTitle(description.title)
                .setContentText(description.subtitle)
                .setLargeIcon(art)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        if (mPlaybackState.state == PlaybackStateCompat.STATE_PLAYING || mPlaybackState.state == PlaybackStateCompat.STATE_PAUSED) {
            val iconRestart = context.iconicsDrawable(CommunityMaterial.Icon2.cmd_restart) {
                color = colorRes(R.color.ic_notification_color)
                size = sizeDp(24)
                padding = sizeDp(2)
            }
            val actionRestart = NotificationCompat.Action(iconRestart.toAndroidIconCompat(),
                    context.getString(R.string.label_restart),
                    MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS))
            notificationBuilder.addAction(actionRestart)
        }

        return notificationBuilder.build()
    }

}// Helper utility class; do not instantiate.
