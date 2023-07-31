package com.example.ytscrobblefilter

import android.util.Log
import de.umass.lastfm.Authenticator
import de.umass.lastfm.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LFMUtils {

    lateinit var session: Session
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
            Log.i("trackSearch() coroutine", "Starting.")
            response = de.umass.lastfm.Track.search(title, apikey)
        }

        if (response.isEmpty()){
            return null
        }

        return response.elementAt(0)
    }
}