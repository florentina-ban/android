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
import flore.ubb.mob.recipeapp.core.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    val recipeRepository: RecipeRepository

    init {
        val recipeDao = RecipeDb.getDatabase(application, viewModelScope).recipeDao()
        recipeRepository = RecipeRepository(recipeDao)
    }

    fun loadItem(itemId: String) {
        viewModelScope.launch {
            Log.i(TAG, "loadItem...")
            mutableFetching.value = true
            mutableException.value = null

            try {
                val result = withContext(Dispatchers.IO) {
                   recipeRepository.getById(itemId)
                }
                mutableRecipe.value = result
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
                    when (val result = recipeRepository.update(recipe)){
                        is Result.Success -> {
                            Log.i(TAG,"saveOrUpdateItem succeeded");
                            mutableRecipe.value = result.data
                            mutableCompleted.value = true
                            mutableFetching.value = false
                        }
                        is Result.Error -> {
                            mutableFetching.value = false
                            // mutableException.value = result.data
                            Log.w(TAG, "saveOrUpdateItem failed");

                        }
                    }
                } else {
                    Log.i(TAG,"empty")
                    when (val result = recipeRepository.save(recipe)){
                        is Result.Success -> {
                            Log.i(TAG,"saveOrUpdateItem succeeded");
                            mutableRecipe.value = result.data
                            mutableCompleted.value = true
                            mutableFetching.value = false
                        }
                        is Result.Error -> {
                            mutableFetching.value = false
                           // mutableException.value = result.data
                            Log.w(TAG, "saveOrUpdateItem failed");

                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "saveOrUpdateItem failed"+e.message, e);
            }
        }
    }

    fun removeItem(id: String) {
        viewModelScope.launch {
            Log.i(TAG, "removeItem...")
            mutableFetching.value = true
            mutableException.value = null
            try {
                val result = withContext(Dispatchers.IO) {
                    recipeRepository.remove(id)
                }
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
