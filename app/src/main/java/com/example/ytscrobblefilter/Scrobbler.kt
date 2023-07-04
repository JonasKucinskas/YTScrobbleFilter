package com.example.ytscrobblefilter

import android.accounts.Account
import android.content.Context
import android.media.session.MediaController
import androidx.appcompat.app.AppCompatActivity

class Scrobbler(context: Context) {

    val context = context
    fun shouldScrobble(controllers: List<MediaController>): Boolean{

        val ytUtils = YTUtils(context)
        ytUtils.getCredential()
        ytUtils.mServiceInit()

        val YTController = ytUtils.getYTController(controllers)
        val video = ytUtils.getVideoData(YTController) ?: return false

        return false//.isSong(video.title)
    }
}