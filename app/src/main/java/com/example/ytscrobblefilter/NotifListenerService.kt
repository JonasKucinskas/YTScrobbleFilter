package com.example.ytscrobblefilter

import android.content.ComponentName
import android.content.Intent
import android.media.session.MediaSessionManager
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.util.Log
import androidx.core.content.ContextCompat

class NotifListenerService: NotificationListenerService(){

    override fun onCreate() {
        super.onCreate()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return super.onBind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.e("Session", "Notification listener service running")

        val sessManager = ContextCompat.getSystemService(this, MediaSessionManager::class.java)!!
        //val mediaManager = MediaManager()

        //https://github.com/kawaiiDango/pScrobbler/blob/main/app/src/main/java/com/arn/scrobble/NLService.kt#L178
        //https://github.com/kawaiiDango/pScrobbler/blob/aec9cf3ece299a2cde1c6b12ac438a364c813ae1/app/src/main/java/com/arn/scrobble/SessListener.kt
       // sessManager.addOnActiveSessionsChangedListener(mediaManager, ComponentName(this, this::class.java))

        //mediaManager.onActiveSessionsChanged(
       //     sessManager.getActiveSessions(ComponentName(this, this::class.java))
        //)

        return super.onStartCommand(intent, flags, startId)
    }
}