package com.example.ytscrobblefilter

import android.util.Log
import com.google.api.services.youtube.YouTube
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import okhttp3.Request.Builder
import okhttp3.Response

class LastfmUtils {

    private val API_KEY = "434c754a715a7ea0b03a0d5025768f3a"
    private val BASE_URL = "https://ws.audioscrobbler.com/2.0/"

    private var client: OkHttpClient? = OkHttpClient()
    private var gson: Gson? = Gson()

    suspend fun getArtistInfo(artistName: String) {
        val url = "$BASE_URL?method=artist.getinfo&artist=$artistName&api_key=$API_KEY&format=json"

        withContext(Dispatchers.IO) {
            val request: Request = Builder()
                .url(url)
                .build()
            val response: Response = client!!.newCall(request).execute()
            val responseBody: ResponseBody? = response.body
            if (responseBody != null) {
                val json = responseBody.string()
            }
        }
    }
}

