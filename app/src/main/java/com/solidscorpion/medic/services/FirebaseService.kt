package com.solidscorpion.medic.services

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.pushwoosh.PushwooshFcmHelper
import org.json.JSONObject
import android.app.NotificationManager
import android.app.NotificationChannel
import android.annotation.SuppressLint
import android.app.Notification
import android.os.Build
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.solidscorpion.medic.MainActivity
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.solidscorpion.medic.R




class FirebaseService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (PushwooshFcmHelper.isPushwooshMessage(remoteMessage)) {
            Log.e("DATA",remoteMessage.data.toString())
            val params = remoteMessage.data
            val obj = JSONObject(params)

            val customObj = JSONObject(obj.getString("u"))
            val url = customObj["url"]
            val message = obj["title"].toString()
            Log.e("JSON OBJECT", url.toString())
            sendNotification(url.toString(), message)
        }
    }

    private fun sendNotification(url: String, message: String) {

        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.putExtra("URL", url)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val NOTIFICATION_CHANNEL_ID = "101"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant") val notificationChannel =
                NotificationChannel(NOTIFICATION_CHANNEL_ID, "Notification", NotificationManager.IMPORTANCE_MAX)

            //Configure Notification Channel
            notificationChannel.description = "Game Notifications"
            notificationChannel.enableLights(true)
            notificationChannel.vibrationPattern = longArrayOf(0, 100, 100, 100)
            notificationChannel.enableVibration(true)

            notificationManager.createNotificationChannel(notificationChannel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Medic")
            .setAutoCancel(true)
            .setSound(defaultSound)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setWhen(System.currentTimeMillis())
            .setPriority(Notification.PRIORITY_MAX)


        notificationManager.notify(1, notificationBuilder.build())


    }

}
