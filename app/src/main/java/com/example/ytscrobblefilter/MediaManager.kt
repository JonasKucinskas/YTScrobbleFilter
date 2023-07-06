package com.example.ytscrobblefilter

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MediaManager(private val context: Context): MediaSessionManager.OnActiveSessionsChangedListener {

    override fun onActiveSessionsChanged(controllers: List<MediaController>?) {

        //doesn't work when user pauses/unpauses, need more testing.

        if (controllers.isNullOrEmpty())
            return

        val ytUtils = YTUtils(context)
        ytUtils.getCredential()
        ytUtils.mServiceInit()

        val YTController = ytUtils.getYTController(controllers)
        val video = ytUtils.getVideoData(YTController) ?: return


        CoroutineScope(Dispatchers.IO).launch{

            val videoID = ytUtils.getVideoID(video.title)
            if (ytUtils.isSong(videoID)) {
                Log.i("song", "is a song")

                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    val channel = NotificationChannel("my_channel_id", "My Channel", NotificationManager.IMPORTANCE_DEFAULT)
                    notificationManager.createNotificationChannel(channel)
                }

                val builder = NotificationCompat.Builder(context, "my_channel_id")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("listening")
                    .setContentText(video.title)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)

                notificationManager.notify(1, builder.build())
            }
            else Log.i("song", "not a song")
        }
    }
}