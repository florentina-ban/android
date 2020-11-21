package flore.ubb.mob.recipeapp.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import flore.ubb.mob.recipeapp.data.Recipe

@Dao
interface RecipeDao {

    @Query("SELECT * from recipes ORDER BY name ASC")
    fun getAll(): LiveData<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE _id=:id ")
    fun getById(id: String): LiveData<Recipe>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Recipe)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(item: Recipe)

    @Query("DELETE FROM recipes")
    suspend fun deleteAll()

}