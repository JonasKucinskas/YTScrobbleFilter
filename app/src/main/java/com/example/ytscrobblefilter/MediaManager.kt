package com.example.ytscrobblefilter

import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaController.Callback
import android.media.session.MediaSessionManager
import android.os.Build
import android.os.SystemClock
import android.util.Log
import androidx.annotation.RequiresApi
import de.umass.lastfm.Authenticator
import de.umass.lastfm.Session
import de.umass.lastfm.scrobble.ScrobbleData
import de.umass.lastfm.scrobble.ScrobbleResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Math.min
import java.time.Instant
import java.time.ZoneOffset

class MediaManager(context: Context): MediaSessionManager.OnActiveSessionsChangedListener {

    val notificationHelper = NotificationHelper(context)
    private val ytUtils = YTUtils(context)
    var ytController: MediaController? = null
    var lastVideoTitle: String? = null
    var lfmUtils = LFMUtils()
    var playing: Boolean = false

    /*
    init {
        ytUtils.getCredential()
        ytUtils.mServiceInit()
    }
    */
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onActiveSessionsChanged(controllers: List<MediaController>?) {
        controllers ?: return
        ytController = ytUtils.getYTController(controllers) ?: return

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


            CoroutineScope(Dispatchers.IO).launch{

                //val videoID = ytUtils.getVideoID(title)
                val track = lfmUtils.trackSearch(title)

                if (track == null){
                    Log.i("song", "not a song")
                    return@launch
                }
                Log.i("Song", "is a song")

                val trackData = lfmUtils.scrobbleData(track, duration)

                notificationHelper.sendNotification("LISTENING", "${track.artist} - ${track.name}", 1)

                //lfmUtils.nowPlaying(trackData)
                val offset = min(trackData.duration / 2, 240000)//4 minutes of half of track's duration.

                delay(trackData.timestamp + offset - System.currentTimeMillis() / 1000)

                lfmUtils.scrobble(trackData)
                notificationHelper.sendNotification("SCROBBLED", "${track.artist} - ${track.name}", 3)
            }

            notificationHelper.sendNotification("CHANGED VIDEO", title, 2)
        }
        override fun onSessionDestroyed() {
            super.onSessionDestroyed()
            synchronized(this) {
                ytController!!.unregisterCallback(this)
            }
        }

    }
}