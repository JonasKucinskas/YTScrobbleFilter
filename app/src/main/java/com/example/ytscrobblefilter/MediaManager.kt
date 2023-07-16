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

        //doesn't work when user pauses/unpauses.

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
            //doesn't detect if user want to listen to the same song twice.

            val song = Song(metadata)

            if (song.title.isNullOrEmpty() || song.title == lastVideoTitle){
                return
            }
            else lastVideoTitle = song.title

            Log.i("MetaData", "changed to ${metadata.getString(MediaMetadata.METADATA_KEY_TITLE)}")

            CoroutineScope(Dispatchers.IO).launch{

                val videoID = ytUtils.getVideoID(song.title)
                if (ytUtils.isSong(videoID)) {
                    Log.i("Song", "is a song")

                    notificationHelper.sendNotification("LISTENING", song.title, 1)
                }
                else Log.i("song", "not a song")

            }

            notificationHelper.sendNotification("CHANGED VIDEO", song.title, 2)
        }
        override fun onSessionDestroyed() {
            super.onSessionDestroyed()
            synchronized(this) {
                ytController!!.unregisterCallback(this)
            }
        }
    }
}