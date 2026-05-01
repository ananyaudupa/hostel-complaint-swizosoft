package com.swizosoft.hostelcomplaints.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        // Handle incoming notifications
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Send token to server
    }
}
