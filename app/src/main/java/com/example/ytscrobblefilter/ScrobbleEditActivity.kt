package com.example.ytscrobblefilter


import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import de.umass.lastfm.scrobble.ScrobbleData

class ScrobbleEditActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrobble_edit)

        val textView = findViewById<TextView>(R.id.textView)
        MediaManager.ScrobbleDataSingleton.getScrobbleData().observe(this) { scrobbleData: ScrobbleData ->
            textView.text = "${scrobbleData.artist} - ${scrobbleData.track}"
        }
    }
}