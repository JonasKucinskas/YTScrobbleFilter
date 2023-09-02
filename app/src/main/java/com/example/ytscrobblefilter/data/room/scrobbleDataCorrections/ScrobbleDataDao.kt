package com.example.ytscrobblefilter.data.room.scrobbleDataCorrections
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
@Dao
interface ScrobbleDataDao {
    @Insert
    fun insert(scrobbleData: ScrobbleDataRoom)

    @Query("SELECT * FROM ScrobbleDataRoom WHERE oldArtist = :oldArtist AND oldTrack = :oldTrack")
    fun getNewScrobbleData(oldArtist: String, oldTrack: String): ScrobbleDataRoom?

    @Query("DELETE FROM ScrobbleDataRoom")
    fun nukeTable()
}