package com.example.ytscrobblefilter

import android.content.Context
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MediaManager(context: Context): MediaSessionManager.OnActiveSessionsChangedListener {

    val context = context
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
            if (!ytUtils.isSong(video.title)) {
                Log.i("song", "Not a song")
            }
            else Log.i("song", "is song")
        }
        //val notifManger = NotificationManagerCompat.from(context)
        //notifManger.notify(1,builder)
    }
}