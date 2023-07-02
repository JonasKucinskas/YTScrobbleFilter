package com.example.ytscrobblefilter

import android.content.Context
import android.media.session.MediaController
class Scrobbler(context: Context) {

    val context = context
    fun shouldScrobble(controllers: List<MediaController>): Boolean{

        val ytUtils = YTUtils(context)

        val YTController = ytUtils.getYTController(controllers)
        val video = ytUtils.getSongData(YTController) ?: return false

        ytUtils.getYTlink(video.title)
        return false
    }
}