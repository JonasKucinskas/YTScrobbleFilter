import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.ytscrobblefilter.data.room.ArtistDao

@Database(entities = [Artist::class], version = 1)
abstract class ArtistDatabase : RoomDatabase() {

    abstract fun artistDao(): ArtistDao
    // Singleton instance
    companion object {
        private var instance: ArtistDatabase? = null

        @Synchronized
        fun getInstance(context: Context): ArtistDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    ArtistDatabase::class.java,
                    "artist_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return instance!!
        }
    }
}