package flore.ubb.mob.recipeapp.data

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import flore.ubb.mob.recipeapp.MainActivity
import flore.ubb.mob.recipeapp.TAG
import flore.ubb.mob.recipeapp.core.Api
import flore.ubb.mob.recipeapp.core.ConnectionChecker
import flore.ubb.mob.recipeapp.data.local.RecipeDao
import flore.ubb.mob.recipeapp.data.local.RecipeDb
import flore.ubb.mob.recipeapp.data.remote.RecipeApi
import flore.ubb.mob.recipeapp.listcomp.RecipeListAdapter
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.WebSocket
import okhttp3.WebSocketListener

//class RecipeRepository (private val recipeDao: RecipeDao) {
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
}