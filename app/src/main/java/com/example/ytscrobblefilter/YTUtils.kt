package com.example.ytscrobblefilter

import android.accounts.Account
import android.content.Context
import android.media.MediaMetadata
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class YTUtils(Context: Context) {

    public lateinit var mService: YouTube
    public lateinit var mCredential: GoogleAccountCredential
    private var context: Context


    init {
        context = Context
    }
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

    fun getVideoData(controller: MediaController?): Video?{

        if (controller == null){
            Log.e("MediaController", "Media controller doesn't contain meta-data or is null")
            return null
        }

        val title = controller.metadata!!.getString(MediaMetadata.METADATA_KEY_TITLE)
        val creator = controller.metadata!!.getString(MediaMetadata.METADATA_KEY_ARTIST)
        val duration = controller.metadata!!.getLong(MediaMetadata.METADATA_KEY_DURATION)

        return Video(title, creator, duration)
    }


    suspend fun isSong(title: String): Boolean{

        var response: SearchListResponse? = null

        withContext(Dispatchers.IO) {

            Log.i("isSong() coroutine", "Starting.")

            val request = mService.search().list("snippet")
            response = request.setMaxResults(1L)
                .setQ(title)
                .setType("video")
                //category "10" is for songs.
                .setVideoCategoryId("10").execute()

        }

        if (response == null){
            return false
        }

        return response!!.items.size > 0
    }

    /*
    private fun isSong(url: String): Boolean{

        //Gets video id from youtube url, start at 32, because it's the length of url before video id.
        val videoID = url.substring(32)
        //Not tested yet llul

        val request: YouTube.Videos.List = mService.videos().list("snippet")
        val response = request.setId(videoID).execute()

        //"10" category id is "Song"
        return response.items[0].snippet.categoryId == "10"
    }
    */

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

        mCredential.selectedAccount = Account(userEmail, "com.example.ytscrobblefilter")
    }
}