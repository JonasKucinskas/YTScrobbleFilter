package com.example.ytscrobblefilter

import android.content.Context
import android.util.Log
import com.example.ytscrobblefilter.NotificationHelper.NotificationIds
import de.umass.lastfm.Artist
import de.umass.lastfm.Authenticator
import de.umass.lastfm.PaginatedResult
import de.umass.lastfm.Period
import de.umass.lastfm.Session
import de.umass.lastfm.Track
import de.umass.lastfm.User
import de.umass.lastfm.scrobble.ScrobbleData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Math.min

class LFMUtils(context: Context) {

    private var session: Session? = null

    private val username = BuildConfig.LFMusrname
    private val password = BuildConfig.LFMpasswd
    private val apikey = BuildConfig.LFMapikey
    private val secret = BuildConfig.LFMSecret
    private val notificationHelper = NotificationHelper(context)

    init {
        CoroutineScope(Dispatchers.IO).launch {

            session = Authenticator.getMobileSession(
                "https://ws.audioscrobbler.com/2.0/",
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

    suspend fun getArtists(artistCount: Int): PaginatedResult<Artist>? {

        return withContext(Dispatchers.IO) {
            try{
                User.getTopArtists("Baradac", Period.OVERALL, artistCount, session)
            }
            catch (e: Exception){
                Log.e("User.getTopArtists()", e.toString())
                notificationHelper.sendNotification("Get artists error", "Failed to fetch artists", NotificationIds.getArtistError)
                null
            }
        }
    }

    suspend fun nowPlaying(scrobbleData: ScrobbleData){

        withContext(Dispatchers.IO) {
            try{
                Track.updateNowPlaying(scrobbleData, session)
            }
            catch (e: Exception){
                Log.e("Track.updateNowPlaying()", e.toString())
                notificationHelper.sendNotification("Now playing error", "Failed to update now playing status on last fm", NotificationIds.nowPlayingError)
            }
        }
    }

    suspend fun scrobble(scrobbleData: ScrobbleData){

        withContext(Dispatchers.IO) {

            try{
                //time passed from data creation to scrobbling, relevant when user is editing metadata.
                val timePassed = System.currentTimeMillis() / 1000 - scrobbleData.timestamp
                delay(min(scrobbleData.duration / 2, 240000).toLong() - timePassed)//4 minutes of half of track's duration.

                Track.scrobble(scrobbleData.artist, scrobbleData.track, scrobbleData.timestamp, session)

                notificationHelper.sendNotification("Track scrobbled", "${scrobbleData.artist} - ${scrobbleData.track}",
                    NotificationIds.scrobbled
                )
            }
            catch (e: Exception){
                Log.e("Track.Scrobble()", e.toString())
                notificationHelper.sendNotification("Scrobble error", "Failed to scrobble current track", NotificationIds.scrobbleError)
            }
        }
    }

    suspend fun artistGetInfo(name: String) : Artist?{

        return withContext(Dispatchers.IO) {

            try{
                Artist.getInfo(name, "Baradac", apikey)
            }
            catch (e: Exception){
                Log.e("artistSearch()", e.toString())
                notificationHelper.sendNotification("Artist search error.", "Failed search for artist", NotificationIds.getArtistError)
                null
            }
        }
    }

    fun getScrobbleData(track: Track, duration: Int): ScrobbleData{

        val data = ScrobbleData()

        data.track = track.name
        data.artist = track.artist
        data.duration = duration
        data.timestamp = (System.currentTimeMillis() / 1000).toInt()

        return data
    }


}