package com.example.ytscrobblefilter

import android.accounts.Account
import android.content.Context
import android.media.session.MediaController
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.YouTubeScopes
import com.google.api.services.youtube.model.SearchListResponse
import com.google.api.services.youtube.model.VideoListResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class YTUtils(private val context: Context) {

    var mService: YouTube? = null
    var mCredential: GoogleAccountCredential? = null

    fun YTservicesInit(){
        mService = mServiceInit()
        mCredential = getCredential()
    }

    suspend fun getVideoID(title: String): String {

        var response: SearchListResponse?

        withContext(Dispatchers.IO) {
            Log.i("getVideoID() coroutine", "Starting.")

            val request = mService!!.search().list("snippet")
            response = request.setMaxResults(1L)
                .setQ(title)
                .setType("video")
                .execute()
        }

        Log.i("getVideoID() coroutine", "Finished.")

        return response!!.items[0].id.videoId
    }

    suspend fun isSong(videoID: String): Boolean{

        var response: VideoListResponse

        withContext(Dispatchers.IO) {

            Log.i("isSong() coroutine", "Starting.")

            val request: YouTube.Videos.List = mService!!.videos().list("snippet")
            response = request.setId(videoID).execute()
        }
        Log.i("isSong() coroutine", "Finished.")

        //"10" is a category id for a "Song"
        return response.items[0].snippet.categoryId == "10"
    }


    fun mServiceInit(): YouTube? {

        val transport = AndroidHttp.newCompatibleTransport()
        val jsonFactory: JsonFactory = JacksonFactory.getDefaultInstance()

        return YouTube.Builder(
            transport, jsonFactory, mCredential
        )
            .setApplicationName("YTScrobbleFilter")
            .build()
    }

    fun getCredential(): GoogleAccountCredential? {

        val mCredential = GoogleAccountCredential.usingOAuth2(context, listOf(YouTubeScopes.YOUTUBE_READONLY)).setBackOff(
            ExponentialBackOff()
        )

        val sharedPreferences = context.getSharedPreferences("MyPrefs", AppCompatActivity.MODE_PRIVATE)
        val userEmail = sharedPreferences.getString("email", null)

        if (userEmail != null){
            mCredential!!.selectedAccount = Account(userEmail, "com.example.ytscrobblefilter")
        }

        return mCredential
    }
}