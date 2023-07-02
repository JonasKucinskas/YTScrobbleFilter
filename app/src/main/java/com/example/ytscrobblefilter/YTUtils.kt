package com.example.ytscrobblefilter

import android.media.MediaMetadata
import android.media.session.MediaController
import android.util.Log
import com.google.api.services.youtube.YouTube


class YTUtils {

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

    fun getSongData(controller: MediaController?): Song?{

        if (controller == null){
            Log.e("MediaController", "Media controller doesn't contain meta-data")
            return null
        }

        val title = controller.metadata!!.getString(MediaMetadata.METADATA_KEY_TITLE)
        val artist = controller.metadata!!.getString(MediaMetadata.METADATA_KEY_ARTIST)
        val duration = controller.metadata!!.getLong(MediaMetadata.METADATA_KEY_DURATION)

        return Song(title, artist, duration)
    }

    fun getYTlink(title: String, mService: YouTube){
        val request: YouTube.Search.List = mService.search()
            .list(title)
        val response = request.execute()
    }



}