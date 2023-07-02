package com.example.ytscrobblefilter

import android.media.session.MediaController
import android.media.session.MediaSessionManager

class MediaManager: MediaSessionManager.OnActiveSessionsChangedListener {
    override fun onActiveSessionsChanged(controllers: List<MediaController>?) {

        //doesn't work when user pauses.

        if (controllers == null || controllers.size == 0)
            return

        Scrobbler().shouldScrobble(controllers)
    }
}