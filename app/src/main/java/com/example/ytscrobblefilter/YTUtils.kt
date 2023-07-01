package com.example.ytscrobblefilter

import android.media.MediaMetadata
import android.media.session.MediaController
import android.util.Log

class YTUtils {

    private fun isYoutubeController(controller: MediaController): Boolean{
        return (controller.packageName == "app.revanced.android.youtube" ||
            controller.packageName == "com.google.android.youtube" )
    }

    public fun getYTController(controllers: List<MediaController>): MediaController? {

        for (controller in controllers){
            if (isYoutubeController(controller)){
                return controller
            }
        }

        Log.e("Media controller", "No Youtube media controller found.")

        return null
    }

    public fun getSongData(controller: MediaController?): Song{



        val title = controller?.metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
        val artist = controller?.metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)
        val duration = controller?.metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION)

        return Song(title, artist, duration)
    }

}