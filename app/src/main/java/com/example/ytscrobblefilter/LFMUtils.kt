package com.example.ytscrobblefilter

import android.util.Log
import de.umass.lastfm.Authenticator
import de.umass.lastfm.Session
import de.umass.lastfm.Track
import de.umass.lastfm.scrobble.ScrobbleData
import de.umass.lastfm.scrobble.ScrobbleResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LFMUtils {

    private lateinit var session: Session
    private val username = BuildConfig.LFMusrname
    private val password = BuildConfig.LFMpasswd
    private val apikey = BuildConfig.LFMapikey
    private val secret = BuildConfig.LFMSecret

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

    suspend fun trackSearch(title: String): de.umass.lastfm.Track?{

        var response: Collection<de.umass.lastfm.Track>

        withContext(Dispatchers.IO) {
            response = de.umass.lastfm.Track.search(null, title, 1, apikey)
        }

        if (response.isEmpty()){
            return null
        }

        return response.elementAt(0)
    }

    suspend fun nowPlaying(trackData: ScrobbleData){

        withContext(Dispatchers.IO) {
            val result = de.umass.lastfm.Track.updateNowPlaying(trackData, session)
            Log.i("Track.updateNowPlaying", result.status.toString())
        }
    }

    suspend fun scrobble(trackData: ScrobbleData){
        val timeSec = (System.currentTimeMillis() / 1000).toInt() + trackData.timestamp

        withContext(Dispatchers.IO) {
            val result = de.umass.lastfm.Track.scrobble(trackData.artist, trackData.track, timeSec, session)
            Log.i("Track.Scrobble", result.status.toString())
        }
    }

    fun scrobbleData(track: Track, duration: Int): ScrobbleData{

        val data = ScrobbleData()

        data.track = track.name
        data.artist = track.artist
        data.duration = duration


        //if track is longer than 4 minutes, scrobble 4 min into the track, else scrobble when half of the track scrobbles.
        if (data.duration / 2 >= 240000){//4 minutes in me
            data.timestamp = (System.currentTimeMillis() / 1000).toInt() + 240000
        }
        else data.timestamp = (System.currentTimeMillis() / 1000).toInt() + data.duration / 2

        return data
    }
}