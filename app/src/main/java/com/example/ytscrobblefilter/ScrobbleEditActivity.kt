package com.example.ytscrobblefilter

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.umass.lastfm.scrobble.ScrobbleData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScrobbleEditActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrobble_edit)
        val lfmUtils = LFMUtils(this)


        val saveButton = findViewById<FloatingActionButton>(R.id.saveButton)

        val artistTextView = findViewById<TextView>(R.id.artist)
        val titleTextView = findViewById<TextView>(R.id.title)

        val scrobbleData: LiveData<ScrobbleData> = MediaManager.ScrobbleDataSingleton.getScrobbleData()

        scrobbleData.observe(this) { data: ScrobbleData ->
            artistTextView.text = data.artist
            titleTextView.text = data.track
        }

        saveButton.setOnClickListener{

            scrobbleData.value?.artist = artistTextView.text.toString()
            scrobbleData.value?.track = titleTextView.text.toString()

            MediaManager.ScrobbleDataSingleton.setScrobbleData(scrobbleData.value!!)

            CoroutineScope(Dispatchers.IO).launch{
                lfmUtils.scrobble(scrobbleData.value!!)
            }

            Toast.makeText(this, "Song data updated.", Toast.LENGTH_SHORT).show()
        }
    }
}