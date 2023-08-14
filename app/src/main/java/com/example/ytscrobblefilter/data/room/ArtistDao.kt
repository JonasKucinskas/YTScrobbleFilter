package com.example.ytscrobblefilter.data.room

import Artist
import androidx.room.Insert
import androidx.room.Query

interface ArtistDao {
    @Insert
    fun insert(artist: Artist)

    @Query("SELECT * FROM Artist")
    fun getAllArtists(): List<Artist?>?
}