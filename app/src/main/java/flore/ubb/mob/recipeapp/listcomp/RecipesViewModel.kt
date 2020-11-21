package flore.ubb.mob.recipeapp.listcomp

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import flore.ubb.mob.recipeapp.TAG
import flore.ubb.mob.recipeapp.data.Recipe
import flore.ubb.mob.recipeapp.data.RecipeRepository
import flore.ubb.mob.recipeapp.data.local.RecipeDb
import flore.ubb.mob.recipeapp.data.remote.WebsocketCreator
import kotlinx.coroutines.launch
import java.util.*

class RecipesViewModel (application: Application) : AndroidViewModel(application) {

    private val mutableItems = MutableLiveData<List<Recipe>>().apply { value = emptyList() }
    private val mutableLoading = MutableLiveData<Boolean>().apply { value = false }
    private val mutableException = MutableLiveData<Exception>().apply { value = null }

    val items: LiveData<List<Recipe>> = mutableItems
    val loading: LiveData<Boolean> = mutableLoading
    val loadingError: LiveData<Exception> = mutableException

    val recipeRepository: RecipeRepository = RecipeRepository

    init {
        //val recipeDao = RecipeDb.getDatabase(application, viewModelScope).recipeDao()
        //recipeRepository = RecipeRepository(recipeDao)
        //recipeRepository = RecipeRepository
    }

    fun createItem(position: Int): Unit {
        val list = mutableListOf<Recipe>()
        list.addAll(mutableItems.value!!)
        list.add(Recipe(position.toString(), "Recipe " + position, 0 , "description", "", false, Date()))
        mutableItems.value = list
    }

    fun loadItems() {
        viewModelScope.launch {
            Log.v( TAG, "loadRecipes...");
            mutableLoading.value = true
            mutableException.value = null
            try {
                mutableItems.value = recipeRepository.loadAll()
                Log.d(TAG, mutableItems.value.toString())
                Log.d(TAG, "loadRecipes succeeded");

                mutableLoading.value = false
            } catch (e: Exception) {
                Log.w(TAG, "loadRecipes failed", e);
                mutableException.value = e
                mutableLoading.value = false
            }
        }
    }
}