package com.example.ytscrobblefilter.data.room.scrobbleDataCorrections

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ScrobbleDataRoom::class], version = 1)
abstract class ScrobbleDataDatabase : RoomDatabase() {

    //this db contains blacklisted artists.

    abstract fun artistDao(): ScrobbleDataDao
    // Singleton instance
    companion object {
        private var instance: ScrobbleDataDatabase? = null

        fun getInstance(context: Context): ScrobbleDataDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): ScrobbleDataDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                ScrobbleDataDatabase::class.java,
                "ScrobbleDataCorrections"
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}