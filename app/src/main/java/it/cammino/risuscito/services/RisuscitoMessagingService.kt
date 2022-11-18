package it.cammino.risuscito.services

import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class RisuscitoMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Title: ${it.title}")
            Log.d(TAG, "Message Notification Body: ${it.body}")
            val intentBroadcast = Intent(MESSAGE_RECEIVED_TAG)
            intentBroadcast.putExtra(MESSAGE_TITLE, it.title)
            intentBroadcast.putExtra(MESSAGE_BODY, it.body)
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intentBroadcast)
        }
    }

    companion object {
        private val TAG = RisuscitoMessagingService::class.java.canonicalName
        const val MESSAGE_TITLE = "MESSAGE_TITLE"
        const val MESSAGE_BODY = "MESSAGE_BODY"
        const val MESSAGE_RECEIVED_TAG = "MESSAGE_RECEIVED_TAG"
    }
}