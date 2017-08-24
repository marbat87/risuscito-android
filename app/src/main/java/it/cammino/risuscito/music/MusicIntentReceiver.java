package it.cammino.risuscito.music;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import it.cammino.risuscito.PaginaRenderActivity;

/**
 * Receives broadcasted intents. In particular, we are interested in the
 * android.media.AUDIO_BECOMING_NOISY and android.intent.action.MEDIA_BUTTON intents, which is
 * broadcast, for example, when the user disconnects the headphones. This class works because we are
 * declaring it in a &lt;receiver&gt; tag in AndroidManifest.xml.
 */
public class MusicIntentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
//            Toast.makeText(context, "Headphones disconnected.", Toast.LENGTH_SHORT).show();
            // send an intent to our MusicService to telling it to pause the audio
            Intent i = new Intent(context, MusicService.class);
            i.setAction(MusicService.ACTION_PAUSE);
//            context.startService(i);
            ContextCompat.startForegroundService(context, i);
        } else if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
            KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
            if (keyEvent == null || keyEvent.getAction() != KeyEvent.ACTION_DOWN)
                return;
            Log.d(getClass().getName(), "onReceive: keycode " + keyEvent.getKeyCode());
            Intent i = new Intent(context, MusicService.class);
            switch (keyEvent.getKeyCode()) {
                case KeyEvent.KEYCODE_HEADSETHOOK:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    i.setAction(MusicService.ACTION_TOGGLE_PLAYBACK);
//                    context.startService(i);
                    ContextCompat.startForegroundService(context, i);
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    i.setAction(MusicService.ACTION_PLAY);
//                    context.startService(i);
                    ContextCompat.startForegroundService(context, i);
                    break;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    i.setAction(MusicService.ACTION_PAUSE);
//                    context.startService(i);
                    ContextCompat.startForegroundService(context, i);
                    break;
                case KeyEvent.KEYCODE_MEDIA_STOP:
                    context.stopService(i);
//                    i.setAction(MusicService.ACTION_STOP);
//                    context.startService(i);
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    Toast.makeText(context, "Not supported!", Toast.LENGTH_SHORT).show();
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    // restart from beginning
                    i.setAction(MusicService.ACTION_REWIND);
//                    context.startService(i);
                    ContextCompat.startForegroundService(context, i);
                    break;
            }
        }
    }
}