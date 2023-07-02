package com.example.ytscrobblefilter

import android.media.session.MediaController
import com.google.api.services.youtube.YouTube

class Scrobbler(mService: YouTube) {

    val mService = mService
    fun shouldScrobble(controllers: List<MediaController>): Boolean{

        val ytUtils = YTUtils()

        val YTController = ytUtils.getYTController(controllers)
        val song = ytUtils.getSongData(YTController)

        if (song == null)
            return false


        ytUtils.getYTlink(song.title, mService)
        return false
    }
}