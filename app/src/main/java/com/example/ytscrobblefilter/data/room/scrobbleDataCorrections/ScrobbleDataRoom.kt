package com.example.ytscrobblefilter.data.room.scrobbleDataCorrections
import androidx.room.Entity
import androidx.room.PrimaryKey
import de.umass.lastfm.scrobble.ScrobbleData

@Entity(tableName = "ScrobbleDataRoom")
class ScrobbleDataRoom() {

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    var oldArtist: String? = null
    var oldTrack: String? = null

    var newArtist: String? = null
    var newTrack: String? = null

    constructor(
        oldScrobbleData: ScrobbleData,
        newScrobbleData: ScrobbleData
    ) : this() {
        oldArtist = oldScrobbleData.artist
        oldTrack = oldScrobbleData.track
        newArtist = newScrobbleData.artist
        newTrack = newScrobbleData.track
    }
}
