package com.example.ytscrobblefilter

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import android.content.pm.PackageManager
import com.google.gson.Gson
import com.example.ytscrobblefilter.BuildConfig

class LastfmUtils {

    private val key = BuildConfig.LFMapikey
    private val apiSecret = BuildConfig.LFMSecret
    private val BASE_URL = "https://ws.audioscrobbler.com/2.0/"

    private var client: OkHttpClient? = OkHttpClient()
    private var gson: Gson? = Gson()

    suspend fun getArtistInfo(artistName: String) {
        /*
        val url = BASE_URL
        val requestBody = "method=track.scrobble&artist=$artist&track=$track&sk=$sessionKey"

        val request = Request.Builder()
            .url(url)
            .post(requestBody.toRequestBody("application/x-www-form-urlencoded".toMediaType()))
            .build()

        withContext(Dispatchers.IO) {
            val request = Builder()
                .url(url)
                .post(create(MediaType.parse("application/x-www-form-urlencoded"), requestBody))
                .build()
        }

        consumer.sign(request)
        val response: Response = client.newCall(request).execute()
        // Handle the response as needed
        */

    }
}

