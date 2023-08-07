package com.example.ytscrobblefilter

import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaController.Callback
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Math.min

class MediaManager(context: Context): MediaSessionManager.OnActiveSessionsChangedListener {

    val notificationHelper = NotificationHelper(context)
    var ytController: MediaController? = null
    var lastVideoTitle: String? = null
    var lfmUtils = LFMUtils(context)
    val coroutineScope = CoroutineScope(Dispatchers.IO)


    @RequiresApi(Build.VERSION_CODES.R)
    override fun onActiveSessionsChanged(controllers: List<MediaController>?) {
        controllers ?: return
        ytController = getYTController(controllers) ?: return

        val callback = ControllerCallback()
        ytController!!.registerCallback(callback)
    }


    inner class ControllerCallback: Callback(){

        //this is getting called multiple times for one metadata change, no idea why.
        @Synchronized
        override fun onMetadataChanged(metadata: MediaMetadata?){
            metadata ?: return

            val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE)
            val duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION).toInt()

            //this somewhat fixes multiple calls, however user cant scrobble same track 2 times in a row.
            if (title.isEmpty() || title == lastVideoTitle){
                return
            }
            else lastVideoTitle = title

            Log.i("MetaData", "changed to $title")


            coroutineScope.launch{

                //val videoID = ytUtils.getVideoID(title)
                val track = lfmUtils.trackSearch(title)

                if (track == null){
                    Log.i("Is a Song?", "Not a song")
                    return@launch
                }
                Log.i("Is a Song?", "is a song")

                val trackData = lfmUtils.scrobbleData(track, duration)

                notificationHelper.sendNotification("LISTENING", "${track.artist} - ${track.name}",
                    NotificationHelper.NotificationIds.listening
                )

                lfmUtils.nowPlaying(trackData)
                val offset = min(trackData.duration / 2, 240000)//4 minutes of half of track's duration.

                //there's probably a better way to do this.
                delay(offset.toLong())

                lfmUtils.scrobble(trackData)

                notificationHelper.sendNotification("SCROBBLED", "${track.artist} - ${track.name}",
                    NotificationHelper.NotificationIds.scrobbled
                )
            }
        }

        override fun onPlaybackStateChanged(state: PlaybackState?) {
            super.onPlaybackStateChanged(state)
            state ?: return

            val stateName = when (state.state) {
                PlaybackState.STATE_NONE -> "None"
                PlaybackState.STATE_STOPPED -> "Stopped"
                PlaybackState.STATE_PAUSED -> "Paused"
                PlaybackState.STATE_PLAYING -> "Playing"
                PlaybackState.STATE_FAST_FORWARDING -> "Fast Forwarding"
                PlaybackState.STATE_REWINDING -> "Rewinding"
                PlaybackState.STATE_BUFFERING -> "Buffering"
                PlaybackState.STATE_ERROR -> "Error"
                else -> "Unknown"
            }

            Log.i("Playback state change", stateName)

            if (stateName == "Stopped"){
                coroutineScope.cancel()//cancer current scrobble and other calls.
            }
        }

        override fun onSessionDestroyed() {
            super.onSessionDestroyed()
            synchronized(this) {
                ytController!!.unregisterCallback(this)
            }
        }
    }

    private fun isYoutubeController(controller: MediaController): Boolean {
        return (controller.packageName == "app.revanced.android.youtube" ||
                controller.packageName == "com.google.android.youtube" )
    }

    private fun getYTController(controllers: List<MediaController>): MediaController? {

        for (controller in controllers){
            if (isYoutubeController(controller)){
                return controller
            }
        }

        Log.e("Media controller", "No Youtube media controller found.")

        return null
    }
}