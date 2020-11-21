package flore.ubb.mob.recipeapp.data

import androidx.room.*
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

//    @ColumnInfo(name = "date")
//    var timestamp: Timestamp =Timestamp(date.getTime())

) {
    override fun toString(): String {
        return _id+" "+name+" "+description+" "+likes;
    }
}