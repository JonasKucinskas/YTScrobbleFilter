package com.example.ytscrobblefilter

import android.media.session.MediaController
import android.media.session.MediaSessionManager

class MediaManager: MediaSessionManager.OnActiveSessionsChangedListener {
    override fun onActiveSessionsChanged(controllers: List<MediaController>?) {

        //doesn't work when user pauses.

        if (controllers == null)
            return

        val YTUtils = YTUtils()

        val YTController = YTUtils.getYTController(controllers)
        val song = YTUtils.getSongData(YTController)

    }
}