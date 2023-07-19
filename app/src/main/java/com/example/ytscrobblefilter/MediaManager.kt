package com.example.ytscrobblefilter

import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaController.Callback
import android.media.session.MediaSessionManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MediaManager(context: Context): MediaSessionManager.OnActiveSessionsChangedListener {

    val notificationHelper = NotificationHelper(context)
    private val ytUtils = YTUtils(context)
    var ytController: MediaController? = null
    var lastVideoTitle: String? = null

    init {
        ytUtils.getCredential()
        ytUtils.mServiceInit()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onActiveSessionsChanged(controllers: List<MediaController>?) {

        if (controllers.isNullOrEmpty())
            return

        ytController = ytUtils.getYTController(controllers) ?: return

        val callback = ControllerCallback()
        ytController!!.registerCallback(callback)
    }


    inner class ControllerCallback: Callback(){

        //this is getting called multiple times for one metadata change, no idea why.
        @Synchronized
        override fun onMetadataChanged(metadata: MediaMetadata?){
            metadata ?: return

            val track = Track(metadata)

            //this somewhat fixes multiple calls, however user cant scrobble same track 2 times in a row.
            if (track.title.isEmpty() || track.title == lastVideoTitle){
                return
            }
            else lastVideoTitle = track.title

            Log.i("MetaData", "changed to ${metadata.getString(MediaMetadata.METADATA_KEY_TITLE)}")

            CoroutineScope(Dispatchers.IO).launch{

                val videoID = ytUtils.getVideoID(track.title)
                if (ytUtils.isSong(videoID)) {
                    Log.i("Song", "is a song")

                    notificationHelper.sendNotification("LISTENING", track.title, 1)
                }
                else Log.i("song", "not a song")

            }

            notificationHelper.sendNotification("CHANGED VIDEO", track.title, 2)
        }
        override fun onSessionDestroyed() {
            super.onSessionDestroyed()
            synchronized(this) {
                ytController!!.unregisterCallback(this)
            }
        }
    }
}