import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Artist")
class Artist {
    @PrimaryKey(autoGenerate = true)
    var id = 0

    @ColumnInfo(name = "username")
    var name: String? = null

    @ColumnInfo(name = "mbid")
    var mbid: String? = null

    @ColumnInfo(name = "playcount")
    var playcount: Int? = null

    @ColumnInfo(name = "url")
    var url: String? = null

    @ColumnInfo(name = "imageUrls")
    var imageUrls: ArrayList<String>? = null
}