package com.example.ytscrobblefilter.data.room.Artist
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

    @Query("SELECT EXISTS(SELECT 1 FROM Artist WHERE name = :name LIMIT 1)")
    fun contains(name: String): Boolean

    @Query("DELETE FROM Artist")
    fun nukeTable()

    @Query("SELECT * FROM Artist WHERE name = :name")
    fun getArtist(name: String): Artist?
}