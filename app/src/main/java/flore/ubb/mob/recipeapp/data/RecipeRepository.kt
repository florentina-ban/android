package flore.ubb.mob.recipeapp.data

import android.util.Log
import androidx.lifecycle.LiveData
import flore.ubb.mob.recipeapp.data.local.RecipeDao
import flore.ubb.mob.recipeapp.data.remote.RecipeApi
import java.lang.Exception
import flore.ubb.mob.recipeapp.core.Result
import flore.ubb.mob.recipeapp.core.TAG

object RecipeRepository{

    private lateinit var recipeDao: RecipeDao
    lateinit var recipes: LiveData<List<Recipe>>
    lateinit var recipesLocal: LiveData<List<Recipe>>
    init {
        try{
            recipes = recipeDao.getAll()
            recipesLocal = recipeDao.getAllForWorker()
            Log.d(TAG, "recipes_local_size"+recipesLocal.value?.size.toString())
        }catch (e: Exception){
            //recipes = recipeDao.getAllFromLocal()
        }
    }

    fun setRecipeDao(recipeD: RecipeDao){
        recipeDao = recipeD
        try{
            recipes = recipeDao.getAll()
            recipesLocal = recipeDao.getAllForWorker()
            Log.d(TAG, "recipes_local_size"+recipesLocal.value?.size.toString())
        }catch (e: Exception){
            //recipes = recipeDao.getAllFromLocal()
        }
    }

    fun getCurrentRecipes(): LiveData<List<Recipe>>{
        return recipes
    }

    fun getRecipesForWorker(): LiveData<List<Recipe>>{
        val a= recipeDao.getAllForWorker()
        Log.d(TAG, a.value?.size.toString())
        return a
    }

    suspend fun refresh(): Result<Boolean> {
        try {
            recipesLocal = recipeDao.getAllForWorker()
            Log.d(TAG, "recipes_local_size"+recipesLocal.value?.size.toString())
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
    // 1 -> doar local -> created
    suspend fun save(item: Recipe): Result<Recipe> {
        try {
            item.database = 0
            val createdItem = RecipeApi.service.create(item)
            recipeDao.insert(createdItem)
            recipeDao.deleteOne("")
            return Result.Success(createdItem)
        } catch(e: Exception) {
            item.database = 1
            item.timestamp = System.currentTimeMillis()
            recipeDao.insert(item)
            Log.v(TAG, "in exception: database: "+ item.database)
            return Result.Error(e)
        }
    }

    // 3 -> doar Local -> removed
    suspend fun remove(recipe: Recipe): Result<Recipe>{
        Log.i(TAG, "remove")
        try {
            recipe.database = 0
            val removedItem = RecipeApi.service.remove(recipe._id)
            recipeDao.deleteOne(recipe._id)
            //removeFromList(id)
            return Result.Success(removedItem)
        }catch (e: Exception){
            recipe.database = 3
            recipe.timestamp = System.currentTimeMillis()
            recipeDao.update(recipe)
            Log.v(TAG, "in exception: ")
            return Result.Error(e)
        }
    }
    // 2 -> doar local -> updated
    suspend fun update(item: Recipe): Result<Recipe> {
        try {
            item.database = 0
            val updatedItem = RecipeApi.service.update(item._id, item)
            recipeDao.update(item)
            return Result.Success(item)
        } catch(e: Exception) {
            item.database = 2
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