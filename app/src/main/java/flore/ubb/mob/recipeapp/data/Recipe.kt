package flore.ubb.mob.recipeapp.data

import androidx.room.*
import java.sql.Timestamp
import java.util.*

class MyConverter(){
    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }

    @TypeConverter
    fun toTimestamp(date: Date?): Long? {
        return date?.time
    }
}


@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey @ColumnInfo(name = "_id")
    var _id: String,
    @ColumnInfo(name = "name")
    var name: String,

    @ColumnInfo(name = "likes")
    var likes: Int,

    @ColumnInfo(name = "description")
    var description: String,

    @ColumnInfo(name = "origin")
    var origin: String,
    @ColumnInfo(name = "triedIt")
    var triedIt: Boolean,


    @TypeConverters(MyConverter::class)
    @ColumnInfo(name = "date")
    var date: Date,

   @ColumnInfo(name = "version")
    var timestamp: Long?,

    // 0, null -> ok;
    // 1 -> doar local -> created
    // 2 -> doar local -> updated
    // 3 -> doar Local -> removed
    @ColumnInfo(name = "location")
    var database: Int?

) {
    override fun toString(): String {
        return _id+" "+name+" "+description+" "+likes;
    }
}