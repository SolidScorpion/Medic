package com.solidscorpion.medic

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.pushwoosh.PushwooshFcmHelper


class FirebaseService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (PushwooshFcmHelper.isPushwooshMessage(remoteMessage)) {
            PushwooshFcmHelper.onMessageReceived(this, remoteMessage)
        }
    }
}
