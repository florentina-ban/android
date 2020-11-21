package flore.ubb.mob.recipeapp.editcomp

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import flore.ubb.mob.recipeapp.TAG
import flore.ubb.mob.recipeapp.data.Recipe
import flore.ubb.mob.recipeapp.data.RecipeRepository
import flore.ubb.mob.recipeapp.data.local.RecipeDb
import kotlinx.coroutines.launch
import java.util.*

class RecipeEditViewModel (application: Application) : AndroidViewModel(application) {
    private val mutableRecipe = MutableLiveData<Recipe>().apply { value = Recipe("", "",
        0,"","", false, Date() ) }
    private val mutableFetching = MutableLiveData<Boolean>().apply { value = false }
    private val mutableCompleted = MutableLiveData<Boolean>().apply { value = false }
    private val mutableException = MutableLiveData<Exception>().apply { value = null }

    val recipe: LiveData<Recipe> = mutableRecipe
    val fetching: LiveData<Boolean> = mutableFetching
    val fetchingError: LiveData<Exception> = mutableException
    val completed: LiveData<Boolean> = mutableCompleted

    val recipeRepository: RecipeRepository = RecipeRepository

    init{
        //val recipeDao = RecipeDb.getDatabase(application, viewModelScope).recipeDao()
        //recipeRepository = RecipeRepository(recipeDao)
       // recipeRepository = RecipeRepository()
    }


    fun loadItem(itemId: String) {
        viewModelScope.launch {
            Log.i(TAG, "loadItem...")
            mutableFetching.value = true
            mutableException.value = null
            try {
                mutableRecipe.value = recipeRepository.load(itemId)
                Log.i(TAG, "loadItem succeeded")
                mutableFetching.value = false
            } catch (e: Exception) {
                Log.w(TAG, "loadItem failed", e)
                mutableException.value = e
                mutableFetching.value = false
            }
        }
    }

    fun saveOrUpdateItem(id:String, name: String, description: String, origin: String, likes: Int, triedIt: Boolean, date: Date) {
        viewModelScope.launch {
            Log.i(TAG, "saveOrUpdateItem...");
            val recipe = mutableRecipe.value ?: return@launch
            recipe._id = id
            recipe.name = name
            recipe.description = description
            recipe.likes = likes
            recipe.triedIt = triedIt
            recipe.origin = origin
            recipe.date = date
            mutableFetching.value = true
            mutableException.value = null
            Log.v(TAG, "-------------------------")
            try {
                if (recipe._id?.isNotEmpty()!!) {
                    Log.i(TAG,"notempty")
                    mutableRecipe.value = recipeRepository.update(recipe)
                } else {
                    Log.i(TAG,"empty")
                    mutableRecipe.value = recipeRepository.save(recipe)
                }
                Log.i(TAG,"saveOrUpdateItem succeeded");
                mutableCompleted.value = true
                mutableFetching.value = false
            } catch (e: Exception) {
                Log.w(TAG, "saveOrUpdateItem failed"+e.message, e);
                mutableException.value = e
                mutableFetching.value = false
            }
        }
    }

    fun removeItem(id: String){
            viewModelScope.launch {
                Log.i(TAG, "removeItem...")
                mutableFetching.value = true
                mutableException.value = null
                try {
                    mutableRecipe.value = recipeRepository.remove(id)
                    Log.i(TAG, "removeItem succeeded")
                    mutableFetching.value = false
                    mutableCompleted.value = true
                } catch (e: Exception) {
                    Log.w(TAG, "removeItem failed", e)
                    mutableException.value = e
                    mutableFetching.value = false
                }
            }
        }
}
