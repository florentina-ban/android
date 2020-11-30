package flore.ubb.mob.recipeapp.data

import android.util.Log
import androidx.lifecycle.LiveData
import flore.ubb.mob.recipeapp.data.local.RecipeDao
import flore.ubb.mob.recipeapp.data.remote.RecipeApi
import java.lang.Exception
import flore.ubb.mob.recipeapp.core.Result
import flore.ubb.mob.recipeapp.core.TAG

class RecipeRepository (private val recipeDao: RecipeDao) {
    val recipes = recipeDao.getAllFromLocal()

    fun getCurrentRecipes(): LiveData<List<Recipe>>{
        return recipes
    }

    fun getRecipesForWorker(): LiveData<List<Recipe>>{
        return recipeDao.getAllForWorker()
    }

    suspend fun refresh(): Result<Boolean> {
        try {
            val items = RecipeApi.service.find()
            recipeDao.deleteAll()
            for (item in items) {
                recipeDao.insert(item)
            }
            return Result.Success(true)
        } catch(e: Exception) {
            return Result.Error(e)
        }
    }

    fun getById(itemId: String): Recipe {
        var a = recipeDao.getById(itemId)
        return a
    }

    suspend fun save(item: Recipe): Result<Recipe> {
        try {
            val createdItem = RecipeApi.service.create(item)
            //recipeDao.insert(createdItem)
            return Result.Success(createdItem)
        } catch(e: Exception) {
            item.database = 1
            item.timestamp = System.currentTimeMillis()
            recipeDao.insert(item)
            Log.v(TAG, "in exception: ")
            return Result.Error(e)
        }
    }

    suspend fun remove(recipe: Recipe): Result<Recipe>{
        Log.i(TAG, "remove")
        try {
            val removedItem = RecipeApi.service.remove(recipe._id)
            //removeFromList(id)
            return Result.Success(removedItem)
        }catch (e: Exception){
            recipe.database = 2
            recipe.timestamp = System.currentTimeMillis()
            recipeDao.update(recipe)
            Log.v(TAG, "in exception: ")
            return Result.Error(e)
        }
    }

    suspend fun update(item: Recipe): Result<Recipe> {
        try {
            val updatedItem = RecipeApi.service.update(item._id, item)
            //recipeDao.update(updatedItem)
            return Result.Success(updatedItem)
        } catch(e: Exception) {
            item.database = 1
            item.timestamp = System.currentTimeMillis()
            recipeDao.update(item)
            Log.d(TAG, "in update exception: "+e.toString())
            return Result.Error(e)
        }
    }

    suspend fun updateDao(recipe: Recipe){
        recipeDao.update(recipe)
    }

    suspend fun addToList(recipe: Recipe){
        recipeDao.insert(recipe)
    }

    suspend fun deleteOne(id: String){
        recipeDao.deleteOne(id)
    }


    /*
    object RecipeRepository  {

    private var cachedItems: MutableList<Recipe>? = null;

    suspend fun loadAll(): List<Recipe> {
       // var connected = true
        //Log.i(TAG, "loadAll "+cachedItems?.toString()+" "+ cachedItems?.size.toString())
        if (cachedItems != null) {
           return cachedItems as List<Recipe>;
        }

        cachedItems = mutableListOf()
       // if (connected) {
            val items = RecipeApi.service.find()
            cachedItems?.addAll(items)
            //recipeDao.deleteAll()
//            for (rec in items)
//                recipeDao.insert(rec)
//        }
//        else {
//            val recipes =  recipeDao.getAll()
//            cachedItems?.addAll(recipes as Collection<Recipe>)
//        }
        return cachedItems as List<Recipe>
    }

    suspend fun load(itemId: String): Recipe {
        Log.i(TAG, "load")
        val item = cachedItems?.find { it._id == itemId }
        if (item != null) {
            return item
        }
        return RecipeApi.service.read(itemId)
    }

    suspend fun save(item: Recipe): Recipe {
        Log.i(TAG, "save")
        val createdItem = RecipeApi.service.create(item)
        Log.v(TAG, createdItem._id)
        Log.v(TAG, createdItem.name)
        return createdItem
    }

    suspend fun update(item: Recipe): Recipe {
        Log.i(TAG, "update")
        val updatedItem = RecipeApi.service.update(item._id,item)
        return updatedItem
    }
    fun clearCash(){
        cachedItems=null
        Log.v(TAG,"after clear: "+cachedItems?.toString())
    }
    suspend fun remove(id: String): Recipe{
        Log.i(TAG, "remove")
        val removedItem = RecipeApi.service.remove(id)
        //removeFromList(id)
        return removedItem
    }
    fun removeFromList(id: String){
        val index = cachedItems?.indexOfFirst { it._id == id }
        if (index!=null) {
            cachedItems?.get(index)?._id?.let { Log.i(TAG, it) }
            Log.i(TAG, "removed item - length: "+cachedItems?.size)
            var a = cachedItems?.removeAt(index)
            Log.i(TAG, "removed item - length: "+cachedItems?.size)
        }
    }
    fun addToList(recipe: Recipe){
        cachedItems?.add(recipe);
    }
    fun updateList(recipe: Recipe){
        val index = cachedItems?.indexOfFirst { it._id == recipe._id }
        if (index != null) {
            cachedItems?.set(index, recipe)
        }
    }

     */
}