package com.example.ytscrobblefilter.data.room
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
@Dao
interface ArtistDao {
    @Insert
    fun insert(artist: Artist)

    @Query("SELECT * FROM Artist")
    fun getAllArtists(): List<Artist?>?
    @Insert
    fun insertAll(users: List<Artist>)
}