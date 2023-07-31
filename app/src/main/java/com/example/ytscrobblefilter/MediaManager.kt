package com.example.ytscrobblefilter

import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaController.Callback
import android.media.session.MediaSessionManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import de.umass.lastfm.Authenticator
import de.umass.lastfm.Session
import de.umass.lastfm.scrobble.ScrobbleResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneOffset

class MediaManager(context: Context): MediaSessionManager.OnActiveSessionsChangedListener {

    val notificationHelper = NotificationHelper(context)
    private val ytUtils = YTUtils(context)
    var ytController: MediaController? = null
    var lastVideoTitle: String? = null
    var lfmUtils = LFMUtils()

    /*
    init {
        ytUtils.getCredential()
        ytUtils.mServiceInit()
    }
    */
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

            val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE)

            //this somewhat fixes multiple calls, however user cant scrobble same track 2 times in a row.
            if (title.isEmpty() || title == lastVideoTitle){
                return
            }
            else lastVideoTitle = title

            Log.i("MetaData", "changed to ${metadata.getString(MediaMetadata.METADATA_KEY_TITLE)}")

            CoroutineScope(Dispatchers.IO).launch{

                //val videoID = ytUtils.getVideoID(title)
                val track = lfmUtils.trackSearch(title)

                if (track != null/*ytUtils.isSong(videoID)*/) {
                    Log.i("Song", "is a song")

                    notificationHelper.sendNotification("LISTENING", title, 1)

                    val timeSec = (System.currentTimeMillis() / 1000).toInt()

                    val result: ScrobbleResult = de.umass.lastfm.Track.updateNowPlaying(track.artist, track.name, lfmUtils.session)
                    val scrobbleResult: ScrobbleResult = de.umass.lastfm.Track.scrobble(track.artist, track.name, timeSec, lfmUtils.session)

                    Log.i("Track.Scrobble", scrobbleResult.status.toString())
                }
                else Log.i("song", "not a song")
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