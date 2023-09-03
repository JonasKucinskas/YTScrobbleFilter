package com.example.ytscrobblefilter

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import com.example.ytscrobblefilter.data.room.scrobbleDataCorrections.ScrobbleDataDatabase
import com.example.ytscrobblefilter.data.room.scrobbleDataCorrections.ScrobbleDataRoom
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

        scrobbleData.value
        scrobbleData.observe(this) { data: ScrobbleData ->
            artistTextView.text = data.artist
            titleTextView.text = data.track

        }

        saveButton.setOnClickListener{

            val newData = ScrobbleData(scrobbleData.value!!)
            newData.artist = artistTextView.text.toString()
            newData.track = titleTextView.text.toString()

            val data = ScrobbleDataRoom(scrobbleData.value!!, newData)

            MediaManager.ScrobbleDataSingleton.setScrobbleData(scrobbleData.value!!)
            val db = ScrobbleDataDatabase.getInstance(this)

            CoroutineScope(Dispatchers.IO).launch{
                db.artistDao().insert(data)
                lfmUtils.scrobble(scrobbleData.value!!)
            }

            Toast.makeText(this, "Song data updated.", Toast.LENGTH_SHORT).show()
        }
    }
}