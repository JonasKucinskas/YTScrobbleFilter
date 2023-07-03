package com.example.ytscrobblefilter

import android.accounts.Account
import android.content.Context
import android.media.session.MediaController
import androidx.appcompat.app.AppCompatActivity

class Scrobbler(context: Context) {

    val context = context
    fun shouldScrobble(controllers: List<MediaController>): Boolean{

        val ytUtils = YTUtils(context)
        ytUtils.getCredential()//name empty every time brah

        val sharedPreferences = context.getSharedPreferences("MyPrefs", AppCompatActivity.MODE_PRIVATE)
        val userEmail = sharedPreferences.getString("email", null)

        ytUtils.mCredential.selectedAccount = Account(userEmail, "com.example.ytscrobblefilter")
        ytUtils.mServiceInit()

        val YTController = ytUtils.getYTController(controllers)
        val video = ytUtils.getSongData(YTController) ?: return false

        return ytUtils.isSong(video.title)
    }
}