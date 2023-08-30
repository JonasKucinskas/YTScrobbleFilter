package com.example.ytscrobblefilter.data.room
import androidx.room.Entity
import androidx.room.PrimaryKey

import de.umass.lastfm.Artist
import de.umass.lastfm.ImageSize

@Entity(tableName = "Artist")
class Artist {

    @PrimaryKey(autoGenerate = true)
    var id = 0

    var name: String? = null
    var mbid: String? = null
    var playcount: Int? = null
    var url: String? = null
    var imageUrl: String? = null

    constructor() {
        // Default constructor for creating an empty Artist
    }

    constructor(lastFMArtist: Artist) {
        this.name = lastFMArtist.name
        this.mbid = lastFMArtist.mbid
        this.playcount = lastFMArtist.playcount
        this.url = lastFMArtist.url
        this.imageUrl = lastFMArtist.getImageURL(ImageSize.MEDIUM)
    }
}