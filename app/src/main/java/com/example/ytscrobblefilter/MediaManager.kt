package com.example.ytscrobblefilter

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaController.Callback
import android.media.session.MediaSessionManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MediaManager(private val context: Context): MediaSessionManager.OnActiveSessionsChangedListener {

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onActiveSessionsChanged(controllers: List<MediaController>?) {

        //doesn't work when user pauses/unpauses, need more testing.

        if (controllers.isNullOrEmpty())
            return

        val ytUtils = YTUtils(context)
        ytUtils.getCredential()
        ytUtils.mServiceInit()




        val YTController = ytUtils.getYTController(controllers)
        val metadata = YTController?.metadata ?: return

        val callback = ControllerCallback(ytUtils)
        YTController.registerCallback(callback)
        val song = Song(metadata)

        CoroutineScope(Dispatchers.IO).launch{

            val videoID = ytUtils.getVideoID(song.title)
            if (ytUtils.isSong(videoID)) {
                Log.i("Song", "is a song")

                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    val channel = NotificationChannel("my_channel_id", "My Channel", NotificationManager.IMPORTANCE_DEFAULT)
                    notificationManager.createNotificationChannel(channel)
                }

                val builder = NotificationCompat.Builder(context, "my_channel_id")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("listening")
                    .setContentText(song.title)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)

                notificationManager.notify(1, builder.build())
            }
            else Log.i("song", "not a song")
        }


    }

    inner class ControllerCallback(private val ytUtils: YTUtils): Callback(){
        @Synchronized
        override fun onMetadataChanged(metadata: MediaMetadata?){
            metadata ?: return

            val song = Song(metadata)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                val channel = NotificationChannel("my_channel_id", "My Channel", NotificationManager.IMPORTANCE_DEFAULT)
                notificationManager.createNotificationChannel(channel)
            }

            val builder = NotificationCompat.Builder(context, "my_channel_id")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("SONG CHANGED")
                .setContentText(song.title)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            notificationManager.notify(2, builder.build())
        }

    }
}