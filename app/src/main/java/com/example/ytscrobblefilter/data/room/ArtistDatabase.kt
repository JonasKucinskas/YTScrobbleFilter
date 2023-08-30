package com.example.ytscrobblefilter.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Artist::class], version = 1)
abstract class ArtistDatabase : RoomDatabase() {

    //this db contains blacklisted artists.

    abstract fun artistDao(): ArtistDao
    // Singleton instance
    companion object {
        private var instance: ArtistDatabase? = null

        fun getInstance(context: Context): ArtistDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): ArtistDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                ArtistDatabase::class.java,
                "app_database"
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}