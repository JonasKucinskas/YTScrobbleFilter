package com.example.ytscrobblefilter

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.getSystemService

class NotificationHelper(private val context: Context) {


    private val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    object NotificationIds {
        const val listening = 1
        const val scrobbled = 2
        const val trackSearchError = 3
        const val nowPlayingError = 4
        const val scrobbleError = 5
    }


    init{
        val channel = NotificationChannel("my_channel_id", "My Channel", NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)
    }

    fun sendNotification(title: String, text: String, id: Int){

        val builder = NotificationCompat.Builder(context, "my_channel_id")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(id, builder.build())
    }
}