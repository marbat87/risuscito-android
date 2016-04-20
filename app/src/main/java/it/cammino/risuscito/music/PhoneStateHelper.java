package it.cammino.risuscito.music;

import android.Manifest;
import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class PhoneStateHelper extends PhoneStateListener {

    PhoneListener phoneListener;
    TelephonyManager mgr;

    public PhoneStateHelper(Context ctx, PhoneListener listener) {
        phoneListener = listener;
        mgr = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
    }

    public void listen() {
        mgr.listen(this, PhoneStateListener.LISTEN_CALL_STATE);
    }

    public void unregister() {
        mgr.listen(this, PhoneStateListener.LISTEN_NONE);
    }

    /**
     * Callback invoked when device call state changes.
     *
     * @param state          call state
     * @param incomingNumber incoming call phone number. If application does not have
     *                       {@link Manifest.permission#READ_PHONE_STATE READ_PHONE_STATE} permission, an empty
     *                       string will be passed as an argument.
     * @see TelephonyManager#CALL_STATE_IDLE
     * @see TelephonyManager#CALL_STATE_RINGING
     * @see TelephonyManager#CALL_STATE_OFFHOOK
     */
    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        if (state == TelephonyManager.CALL_STATE_RINGING) {
            //Incoming call: Pause music
            phoneListener.onPhoneRinging();
        } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
            //A call is dialing, active or on hold
            phoneListener.onDialing();
        }
        super.onCallStateChanged(state, incomingNumber);
    }

    public interface PhoneListener {
        void onPhoneRinging();

        void onDialing();
    }
}


