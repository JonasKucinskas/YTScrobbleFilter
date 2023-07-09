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

    lateinit var mService: YouTube
    lateinit var mCredential: GoogleAccountCredential

    private fun isYoutubeController(controller: MediaController): Boolean {
        return (controller.packageName == "app.revanced.android.youtube" ||
            controller.packageName == "com.google.android.youtube" )
    }

    fun getYTController(controllers: List<MediaController>): MediaController? {

        for (controller in controllers){
            if (isYoutubeController(controller)){
                return controller
            }
        }

        Log.e("Media controller", "No Youtube media controller found.")

        return null
    }

    suspend fun getVideoID(title: String): String{

        var response: SearchListResponse?

        withContext(Dispatchers.IO) {

            Log.i("isSong() coroutine", "Starting.")

            val request = mService.search().list("snippet")
            response = request.setMaxResults(1L)
                .setQ(title)
                .setType("video")
                .execute()
        }

        return response!!.items[0].id.videoId
    }


    suspend fun isSong(videoID: String): Boolean{

        var response: VideoListResponse

        withContext(Dispatchers.IO) {

            Log.i("isSong() coroutine", "Starting.")

            val request: YouTube.Videos.List = mService.videos().list("snippet")
            response = request.setId(videoID).execute()
        }
        Log.i("isSong() coroutine", "Finished.")

        //"10" category id is "Song"
        return response.items[0].snippet.categoryId == "10"
    }


    fun mServiceInit(){

        val transport = AndroidHttp.newCompatibleTransport()
        val jsonFactory: JsonFactory = JacksonFactory.getDefaultInstance()
        mService = YouTube.Builder(
            transport, jsonFactory, mCredential
        )
            .setApplicationName("YTScrobbleFilter")
            .build()
    }

    fun getCredential() {

        mCredential = GoogleAccountCredential.usingOAuth2(context, listOf(YouTubeScopes.YOUTUBE_READONLY)).setBackOff(
            ExponentialBackOff()
        )

        val sharedPreferences = context.getSharedPreferences("MyPrefs", AppCompatActivity.MODE_PRIVATE)
        val userEmail = sharedPreferences.getString("email", null)

        if (userEmail != null){
            mCredential.selectedAccount = Account(userEmail, "com.example.ytscrobblefilter")
        }

    }
}