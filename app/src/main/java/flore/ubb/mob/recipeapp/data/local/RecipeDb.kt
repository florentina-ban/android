package flore.ubb.mob.recipeapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import flore.ubb.mob.recipeapp.data.MyConverter
import flore.ubb.mob.recipeapp.data.Recipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Recipe::class], version = 2)
@TypeConverters(MyConverter::class)
abstract class RecipeDb : RoomDatabase() {

    abstract fun recipeDao(): RecipeDao

    companion object {
        @Volatile
        private var INSTANCE: RecipeDb? = null

        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE recipes "
                            + " ADD COLUMN version INTEGER"

                )
                database.execSQL(
                    "ALTER TABLE recipes "
                            + " ADD COLUMN location INTEGER"

                )
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): RecipeDb {
            val inst = INSTANCE
            if (inst != null) {
                return inst
            }
            val instance =
                Room.databaseBuilder(
                    context.applicationContext,
                    RecipeDb::class.java,
                    "recipe_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .addCallback(WordDatabaseCallback(scope))
                    .build()
            INSTANCE = instance
            return instance
        }

        private class WordDatabaseCallback(private val scope: CoroutineScope) :
            RoomDatabase.Callback() {

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database.recipeDao())
                    }
                }
            }
        }

        suspend fun populateDatabase(recipeDao: RecipeDao) {
            recipeDao.deleteAll()
            //val item = Recipe("1", "Hello")
            //recipeDao.insert(item)
        }
    }

}