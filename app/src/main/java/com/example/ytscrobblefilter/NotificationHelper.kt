package com.example.ytscrobblefilter

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.getBroadcast
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.ytscrobblefilter.NotificationHelper.IntentActionNames.blacklistNewArtist
import com.example.ytscrobblefilter.NotificationHelper.IntentActionNames.editNewArtist
import com.example.ytscrobblefilter.NotificationHelper.IntentActionNames.scrobbleNewArtist
import com.example.ytscrobblefilter.NotificationHelper.NotificationIds.shouldScrobble


class NotificationHelper(private val context: Context) {


    val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        if (id == shouldScrobble){

            val scrobbleEditIntent = Intent(context, MainActivity::class.java)
                .apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            val scrobbleEditPendingIntent: PendingIntent = PendingIntent.getActivity(
                context,
                69,
                scrobbleEditIntent,
                PendingIntent.FLAG_IMMUTABLE
            )

            builder.setContentIntent(scrobbleEditPendingIntent)


            val scrobbleIntent = Intent(context, MediaManager.NotificationBroadcastReceiver::class.java)
                .setAction(scrobbleNewArtist) // Set the action
            val scrobblePendingIntent = getBroadcast(
                context,
                shouldScrobble,
                scrobbleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val blacklistIntent = Intent(context, MediaManager.NotificationBroadcastReceiver::class.java)
                .setAction(blacklistNewArtist) // Set the action
            val blacklistPendingIntent = getBroadcast(
                context,
                shouldScrobble,
                blacklistIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )


            // Add the scrobble action button
            builder.addAction(
                R.mipmap.ic_launcher,
                "Scrobble",
                scrobblePendingIntent
            )

            // Add the blacklist action button
            builder.addAction(
                R.mipmap.ic_launcher,
                "Blacklist",
                blacklistPendingIntent
            )

            builder.setSmallIcon(android.R.drawable.ic_media_play)
        }
        else builder.setSmallIcon(R.mipmap.ic_launcher)

        notificationManager.notify(id, builder.build())
    }
}