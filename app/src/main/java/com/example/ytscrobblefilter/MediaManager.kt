package com.example.ytscrobblefilter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaController.Callback
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Build
import com.example.ytscrobblefilter.NotificationHelper.NotificationIds
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.ytscrobblefilter.NotificationHelper.IntentActionNames.blacklistNewArtist
import com.example.ytscrobblefilter.NotificationHelper.IntentActionNames.editNewArtist
import com.example.ytscrobblefilter.NotificationHelper.IntentActionNames.scrobbleNewArtist
import com.example.ytscrobblefilter.data.room.Artist
import com.example.ytscrobblefilter.data.room.ArtistDatabase
import de.umass.lastfm.scrobble.ScrobbleData
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
    val lfmUtils = LFMUtils(context)
    val coroutineScope = CoroutineScope(Dispatchers.IO)
    val artistDatabase = ArtistDatabase.getInstance(context)

    object ScrobbleDataSingleton {
        private val scrobbleData = MutableLiveData<ScrobbleData>()

        fun setScrobbleData(data: ScrobbleData) {
            scrobbleData.postValue(data)
        }

        fun getScrobbleData(): LiveData<ScrobbleData> {
            return scrobbleData
        }
    }

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
            //coroutineScope.cancel()//if video changed, cancel previous operations.

            val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE)
            val duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION).toInt()

            //this fixes multiple calls, however user cant scrobble same track 2 times in a row.
            if (title.isEmpty() || title == lastVideoTitle){
                return
            }
            lastVideoTitle = title

            Log.i("MetaData", "changed to $title")

            coroutineScope.launch {

                val track = lfmUtils.trackSearch(title)

                if (track != null){
                    val scrobbleData = lfmUtils.getScrobbleData(track, duration)
                    ScrobbleDataSingleton.setScrobbleData(scrobbleData)
                    val artistInDatabase = artistDatabase.artistDao().getArtist(scrobbleData.artist)


                    if(artistInDatabase != null && !artistInDatabase.blacklisted){//scrobble

                        //lfmUtils.nowPlaying(trackData)

                        //there's probably a better way to do this.
                        //delay(min(scrobbleData.duration / 2, 240000).toLong())//4 minutes of half of track's duration.

                        //lfmUtils.scrobble(trackData)

                        notificationHelper.sendNotification("Track scrobbled", "${track.artist} - ${track.name}",
                            NotificationIds.scrobbled
                        )
                    }
                    else if (artistInDatabase == null){
                        notificationHelper.sendNotification("Scrobble this artist?", "Scrobble ${scrobbleData.artist} from now on?",
                            NotificationIds.shouldScrobble
                        )
                        //response is handled in NotificationBroadcastReceiver class.
                    }
                }
                else{
                    Log.i("trackSearch", "Not in last.fm database.")
                    //todo let user create custom metadata and scrobble.
                }
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
                //coroutineScope.cancel()//cancel current scrobble and other calls.
                //Log.i("Coroutine scope", "Canceled")
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

    //Notification action button OnClick receiver.
    class NotificationBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            //have to do this terribleness on every call :|
            val lfmUtils = LFMUtils(context)
            val db = ArtistDatabase.getInstance(context)
            val notificationHelper = NotificationHelper(context)
            //

            val scrobbleData = ScrobbleDataSingleton.getScrobbleData().value ?: return

            if (intent.action == scrobbleNewArtist) {

                val sleepTime = min(scrobbleData.duration / 2, 240000).toLong()

                CoroutineScope(Dispatchers.IO).launch {
                    //need to do this in order to fetch all Artist metadata in correct object.
                    val artist = lfmUtils.artistGetInfo(scrobbleData.artist)
                    //lfmUtils.nowPlaying(scrobbleData)
                    //delay(sleepTime)
                    //lfmUtils.scrobble(scrobbleData)

                    //artist cant be null here:
                    val roomArtist = Artist(artist!!)

                    db.artistDao().insert(roomArtist)
                    Log.i("Room db", "Inserted artist: ${roomArtist.name}")
                }

                notificationHelper.sendNotification("Track scrobbled.", "${scrobbleData.artist} - ${scrobbleData.track}",
                    NotificationIds.scrobbled
                )
            }
            else if (intent.action == blacklistNewArtist){
                //add artist to blacklist

                CoroutineScope(Dispatchers.IO).launch {

                    val artist = lfmUtils.artistGetInfo(scrobbleData.artist)

                    //artist cant be null here.
                    val roomArtist = Artist(artist!!)
                    roomArtist.blacklisted = true

                    //db.artistDao().insert(roomArtist)
                    Log.i("Room Database", "Added and blacklisted artist: ${roomArtist.name}")
                }
            }

            //cancel notification after action button is clicked.
            notificationHelper.notificationManager.cancel(NotificationIds.shouldScrobble)
        }
    }
}