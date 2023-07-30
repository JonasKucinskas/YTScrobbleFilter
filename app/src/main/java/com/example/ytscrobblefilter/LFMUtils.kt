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
    val username = BuildConfig.LFMusrname
    val password = BuildConfig.LFMpasswd
    val apikey = BuildConfig.LFMapikey
    val secret = BuildConfig.LFMSecret

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

    suspend fun titleParse(title: String): Collection<de.umass.lastfm.Track>{

        var response: Collection<de.umass.lastfm.Track>

        withContext(Dispatchers.IO) {
            Log.i("titleParse() coroutine", "Starting.")
            response = de.umass.lastfm.Track.search(title, apikey)
        }

        return response
    }
}