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
import java.lang.Math.min
import kotlin.system.measureTimeMillis

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

    suspend fun trackSearch(title: String): Track?{

        var response: Collection<Track>

        withContext(Dispatchers.IO) {
            response = Track.search(null, title, 1, apikey)
        }

        if (response.isEmpty()){
            return null
        }

        return response.elementAt(0)
    }

    suspend fun nowPlaying(trackData: ScrobbleData){

        withContext(Dispatchers.IO) {
            val result = Track.updateNowPlaying(trackData, session)
            Log.i("Track.updateNowPlaying", result.status.toString())
        }
    }

    suspend fun scrobble(trackData: ScrobbleData){

        withContext(Dispatchers.IO) {
            val result = Track.scrobble(trackData.artist, trackData.track, trackData.timestamp, session)
            Log.i("Track.Scrobble", result.status.toString())
        }
    }

    fun scrobbleData(track: Track, duration: Int): ScrobbleData{

        val data = ScrobbleData()

        data.track = track.name
        data.artist = track.artist
        data.duration = duration
        data.timestamp = (System.currentTimeMillis() / 1000).toInt();

        return data
    }
}