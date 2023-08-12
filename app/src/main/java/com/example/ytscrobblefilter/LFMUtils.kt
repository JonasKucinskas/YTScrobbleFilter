package com.example.ytscrobblefilter

import android.content.Context
import android.util.Log
import com.example.ytscrobblefilter.NotificationHelper.NotificationIds
import de.umass.lastfm.Authenticator
import de.umass.lastfm.Session
import de.umass.lastfm.Track
import de.umass.lastfm.User
import de.umass.lastfm.scrobble.ScrobbleData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LFMUtils(context: Context) {

    private lateinit var session: Session

    private val username = BuildConfig.LFMusrname
    private val password = BuildConfig.LFMpasswd
    private val apikey = BuildConfig.LFMapikey
    private val secret = BuildConfig.LFMSecret
    private val notificationHelper = NotificationHelper(context)

    init {
        CoroutineScope(Dispatchers.IO).launch {
            session = Authenticator.getMobileSession(
                username,
                password,
                apikey,
                secret
            )
        }

    }

    suspend fun trackSearch(title: String): Track?{

        var response: Collection<Track>? = null

        withContext(Dispatchers.IO) {
            try{
                response = Track.search(null, title, 1, apikey)
            }
            catch(e: Exception){
                Log.e("Track.search()", e.toString())
                notificationHelper.sendNotification("Track search error", "error while looking for this track", NotificationIds.trackSearchError)
            }
        }

        if (response.isNullOrEmpty()){
            return null
        }

        return response!!.elementAt(0)
    }

    suspend fun getArtistLib(): Collection<de.umass.lastfm.Artist>? {
        return withContext(Dispatchers.IO) {
            try{
                User.getTopArtists("Baradac", apikey)
            }
            catch (e: Exception){
                Log.e("User.getTopArtists()", e.toString())
                notificationHelper.sendNotification("Get artists error", "Failed to fetch artists", NotificationIds.getArtistError)
                null
            }
        }
    }

    suspend fun nowPlaying(trackData: ScrobbleData){

        withContext(Dispatchers.IO) {
            try{
                Track.updateNowPlaying(trackData, session)
            }
            catch (e: Exception){
                Log.e("Track.updateNowPlaying()", e.toString())
                notificationHelper.sendNotification("Now playing error", "Failed to update now playing status on last fm", NotificationIds.nowPlayingError)
            }
        }
    }

    suspend fun scrobble(trackData: ScrobbleData){

        withContext(Dispatchers.IO) {

            try{
                Track.scrobble(trackData.artist, trackData.track, trackData.timestamp, session)
            }
            catch (e: Exception){
                Log.e("Track.Scrobble()", e.toString())
                notificationHelper.sendNotification("Scrobble error", "Failed to scrobble current track", NotificationIds.scrobbleError)
            }
        }
    }

    fun scrobbleData(track: Track, duration: Int): ScrobbleData{

        val data = ScrobbleData()

        data.track = track.name
        data.artist = track.artist
        data.duration = duration
        data.timestamp = (System.currentTimeMillis() / 1000).toInt()

        return data
    }
}