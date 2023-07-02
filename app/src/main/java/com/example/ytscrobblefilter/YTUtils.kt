package com.example.ytscrobblefilter

import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.util.Log
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.YouTubeScopes


class YTUtils(Context: Context) {

    private var mService: YouTube
    private var mCredential: GoogleAccountCredential
    private var context: Context
    init{

        context = Context
        mCredential = getCredential()
        mService = mServiceInit()

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

    fun getSongData(controller: MediaController?): Video?{

        if (controller == null){
            Log.e("MediaController", "Media controller doesn't contain meta-data or is null")
            return null
        }

        val title = controller.metadata!!.getString(MediaMetadata.METADATA_KEY_TITLE)
        val creator = controller.metadata!!.getString(MediaMetadata.METADATA_KEY_ARTIST)
        val duration = controller.metadata!!.getLong(MediaMetadata.METADATA_KEY_DURATION)

        return Video(title, creator, duration)
    }

    //NAME is null.
    fun getYTlink(title: String){
        val request: YouTube.Search.List = mService.search()
            .list(title)
        val response = request.execute()

        response.items
    }

    fun mServiceInit(): YouTube{

        val transport = AndroidHttp.newCompatibleTransport()
        val jsonFactory: JsonFactory = JacksonFactory.getDefaultInstance()
        return YouTube.Builder(
            transport, jsonFactory, mCredential
        )
            .setApplicationName("YTScrobbleFilter")
            .build()
    }


    fun getCredential(): GoogleAccountCredential {

        mCredential = GoogleAccountCredential.usingOAuth2(context, listOf(YouTubeScopes.YOUTUBE_READONLY)).setBackOff(
            ExponentialBackOff()
        )
        /*
        if (mCredential.selectedAccountName == null) {
            signIn()
        }
        */
        return mCredential
    }
}