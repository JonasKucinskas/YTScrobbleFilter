package com.example.ytscrobblefilter

import android.content.ComponentName
import android.media.session.MediaSessionManager
import android.service.notification.NotificationListenerService
import androidx.core.content.ContextCompat

class NotifListener: NotificationListenerService(){

    override fun onCreate() {
        super.onCreate()
        init()
    }

    private fun init(){

        val sessManager = ContextCompat.getSystemService(this, MediaSessionManager::class.java)!!
        val mediaManager = MediaManager()

        //https://github.com/kawaiiDango/pScrobbler/blob/main/app/src/main/java/com/arn/scrobble/NLService.kt#L178
        //https://github.com/kawaiiDango/pScrobbler/blob/aec9cf3ece299a2cde1c6b12ac438a364c813ae1/app/src/main/java/com/arn/scrobble/SessListener.kt
        sessManager.addOnActiveSessionsChangedListener(mediaManager, ComponentName(this, this::class.java))


        mediaManager.onActiveSessionsChanged{
            sessManager.getActiveSessions(ComponentName(this, this::class.java))
        }
    }
}