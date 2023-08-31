package com.example.ytscrobblefilter

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LiveData
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.umass.lastfm.scrobble.ScrobbleData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Dispatcher

class ScrobbleEditActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrobble_edit)

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

            val lfmUtils = LFMUtils(this)
            CoroutineScope(Dispatchers.IO).launch{
                lfmUtils.scrobble(scrobbleData.value!!, false)
            }

            Toast.makeText(this, "Data updated.", Toast.LENGTH_SHORT).show()
        }
    }
}