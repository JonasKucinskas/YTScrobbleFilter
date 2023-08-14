package com.example.ytscrobblefilter.data.room
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Artist")
class Artist {
    @PrimaryKey(autoGenerate = true)
    var id = 0

    var name: String? = null
    var mbid: String? = null
    var playcount: Int? = null
    var url: String? = null
    var imageUrl: String? = null
}