package com.example.ytscrobblefilter

import android.media.MediaMetadata
import android.media.session.MediaController
import android.util.Log

class YTUtils {

    public fun isYoutubeController(controller: MediaController): Boolean{
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

    public fun getSong(controller: MediaController): Song{
        //return Song(controller.metadata.getString())
        val title = controller.metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
        val artist = controller.metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)

        return Song(title, artist)
    }

}