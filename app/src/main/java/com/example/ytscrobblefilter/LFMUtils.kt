package com.example.ytscrobblefilter

import android.util.Log
import de.umass.lastfm.Authenticator
import de.umass.lastfm.Session
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
            response = de.umass.lastfm.Track.search(title, apikey)
        }

        if (response.isEmpty()){
            return null
        }

        return response.elementAt(0)
    }

    suspend fun nowPlaying(data: ScrobbleData){

        withContext(Dispatchers.IO) {
            val result = de.umass.lastfm.Track.updateNowPlaying(data, session)
            Log.i("Track.updateNowPlaying", result.status.toString())
        }
    }

    suspend fun scrobble(artist: String, title: String){
        val timeSec = (System.currentTimeMillis() / 1000).toInt()

        withContext(Dispatchers.IO) {
            val result = de.umass.lastfm.Track.scrobble(artist, title, timeSec, session)
            Log.i("Track.Scrobble", result.status.toString())
        }
    }
}