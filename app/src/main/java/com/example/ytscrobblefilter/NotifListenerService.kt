package com.example.ytscrobblefilter

import android.content.ComponentName
import android.content.Context
import android.media.session.MediaSessionManager
import android.service.notification.NotificationListenerService
import android.util.Log
import androidx.core.content.ContextCompat

class NotifListenerService: NotificationListenerService(){

    override fun onCreate() {
        super.onCreate()

        Log.e("Session", "Notification listener service running.")

        val sessManager = ContextCompat.getSystemService(this, MediaSessionManager::class.java)!!
        val mediaManager = MediaManager(this)

        sessManager.addOnActiveSessionsChangedListener(mediaManager, ComponentName(this, this::class.java))
        /*
        val controllers: List<MediaController> = sessManager.getActiveSessions(
            ComponentName(this, NotifListenerService::class.java)
        )
        */
    }
}