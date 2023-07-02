package com.example.ytscrobblefilter

import android.content.Context
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import androidx.core.content.ContentProviderCompat.requireContext

class MediaManager(context: Context): MediaSessionManager.OnActiveSessionsChangedListener {

    val context = context
    override fun onActiveSessionsChanged(controllers: List<MediaController>?) {

        //doesn't work when user pauses/unpauses.

        if (controllers == null || controllers.size == 0)
            return

        Scrobbler(context).shouldScrobble(controllers)
    }
}