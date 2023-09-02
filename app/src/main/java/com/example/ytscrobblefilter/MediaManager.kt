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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.ytscrobblefilter.NotificationHelper.IntentActionNames.blacklistNewArtist
import com.example.ytscrobblefilter.NotificationHelper.IntentActionNames.scrobbleNewArtist
import com.example.ytscrobblefilter.data.room.Artist.Artist
import com.example.ytscrobblefilter.data.room.Artist.ArtistDatabase
import com.example.ytscrobblefilter.data.room.scrobbleDataCorrections.ScrobbleDataDatabase
import de.umass.lastfm.scrobble.ScrobbleData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MediaManager(context: Context): MediaSessionManager.OnActiveSessionsChangedListener {

    val notificationHelper = NotificationHelper(context)
    var ytController: MediaController? = null
    var lastVideoTitle: String? = null
    val lfmUtils = LFMUtils(context)
    var coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    val artistDatabase = ArtistDatabase.getInstance(context)
    val scrobbleDataDataBase = ScrobbleDataDatabase.getInstance(context)

    object ScrobbleDataSingleton {
        private var scrobbleData = MutableLiveData<ScrobbleData>()

        fun setScrobbleData(data: ScrobbleData) {
            scrobbleData.postValue(data)
        }

        fun getScrobbleData(): LiveData<ScrobbleData> {
            return scrobbleData
        }

        fun clearScrobbleData() {
            scrobbleData = MutableLiveData<ScrobbleData>()
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

            val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE)
            val duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION).toInt()

            //this fixes multiple calls, however user cant scrobble same track 2 times in a row.
            if (title.isEmpty() || title == lastVideoTitle){
                return
            }
            lastVideoTitle = title

            //reinstantiate this object in case it has been canceled.
            coroutineScope = CoroutineScope(Dispatchers.IO)

            Log.i("MetaData", "changed to $title")

            coroutineScope.launch {

                val track = lfmUtils.trackSearch(title)

                if (track != null){
                    val scrobbleData = lfmUtils.getScrobbleData(track, duration)
                    ScrobbleDataSingleton.setScrobbleData(scrobbleData)
                    val artistInDatabase = artistDatabase.artistDao().contains(scrobbleData.artist)

                    val artist = lfmUtils.artistGetInfo(scrobbleData.artist) ?: return@launch


                    //artist not blacklisted
                    if(!artistInDatabase){

                        val correctedData = scrobbleDataDataBase.artistDao().getNewScrobbleData(scrobbleData.artist, scrobbleData.track)

                        if (artist.userPlaycount > 0 || correctedData != null){//artist has been scrobbled before, scrobble.

                            if (correctedData != null){
                                scrobbleData.artist = correctedData.newArtist
                                scrobbleData.track = correctedData.newTrack
                            }

                            lfmUtils.nowPlaying(scrobbleData)
                            lfmUtils.scrobble(scrobbleData)
                        }
                        else{//artist not scrobbled before, ask if should scrobble.
                            //response is handled in NotificationBroadcastReceiver class.
                            notificationHelper.sendNotification("Scrobble this artist?", "Scrobble ${scrobbleData.artist} from now on?",
                                NotificationIds.shouldScrobble
                            )
                        }
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

            if (stateName == "Stopped" && coroutineScope.isActive){
                coroutineScope.cancel()//cancel current scrobble and other calls.
                ScrobbleDataSingleton.clearScrobbleData()
                notificationHelper.notificationManager.cancel(NotificationIds.shouldScrobble)
                Log.i("Coroutine scope", "Canceled")
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
                Log.i("Media controller", "Youtube media controller found.")
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
            val notificationHelper = NotificationHelper(context)
            //

            val scrobbleData = ScrobbleDataSingleton.getScrobbleData().value ?: return

            if (intent.action == scrobbleNewArtist) {

                CoroutineScope(Dispatchers.IO).launch {
                    lfmUtils.nowPlaying(scrobbleData)
                    lfmUtils.scrobble(scrobbleData)
                }
            }
            else if (intent.action == blacklistNewArtist){
                //add artist to blacklist

                CoroutineScope(Dispatchers.IO).launch {

                    val artist = lfmUtils.artistGetInfo(scrobbleData.artist)

                    //artist won't be null here.
                    val roomArtist = Artist(artist!!)

                    val db = ArtistDatabase.getInstance(context)
                    db.artistDao().insert(roomArtist)
                    Log.i("Room Database", "Added blacklisted artist: ${roomArtist.name}")
                }
            }

            //cancel notification after action button is clicked.
            notificationHelper.notificationManager.cancel(NotificationIds.shouldScrobble)
        }
    }
}