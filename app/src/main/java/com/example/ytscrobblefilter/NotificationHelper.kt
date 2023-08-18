package com.example.ytscrobblefilter

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.getBroadcast
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.ytscrobblefilter.NotificationHelper.IntentActionNames.blacklistNewArtist
import com.example.ytscrobblefilter.NotificationHelper.IntentActionNames.editNewArtist
import com.example.ytscrobblefilter.NotificationHelper.IntentActionNames.scrobbleNewArtist
import com.example.ytscrobblefilter.NotificationHelper.NotificationIds.shouldScrobble


class NotificationHelper(private val context: Context) {


    private val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    object NotificationIds {
        const val listening = 1
        const val scrobbled = 2
        const val trackSearchError = 3
        const val nowPlayingError = 4
        const val scrobbleError = 5
        const val getArtistError = 6
        const val shouldScrobble = 7
        const val artistEdited = 8

    }

    object IntentActionNames {
        const val scrobbleNewArtist = "SCROBBLE_NEW_ARTIST"
        const val blacklistNewArtist = "BLACKLIST_NEW_ARTIST"
        const val editNewArtist = "EDIT_NEW_ARTIST"
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
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)


        if (id == shouldScrobble){
            val intent = Intent(context, MediaManager.NotificationBroadcastReceiver::class.java)

            val pendingIntent = PendingIntent.getActivity(
                context,
                id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            intent.action = scrobbleNewArtist

            builder.setContentIntent(pendingIntent)
                .addAction(R.mipmap.ic_launcher, "Scrobble", getBroadcast(//Add clickable button
                context,
                id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))

            intent.action = blacklistNewArtist

            builder.setContentIntent(pendingIntent).addAction(R.mipmap.ic_launcher, "Blacklist", getBroadcast(//Add clickable button
                context,
                id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))

            intent.action = editNewArtist

            builder.setContentIntent(pendingIntent).addAction(R.mipmap.ic_launcher, "Edit", getBroadcast(//Add clickable button
                context,
                id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
        }

        notificationManager.notify(id, builder.build())
    }
}