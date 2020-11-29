package flore.ubb.mob.recipeapp.listcomp

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import flore.ubb.mob.recipeapp.TAG
import flore.ubb.mob.recipeapp.data.Recipe
import flore.ubb.mob.recipeapp.data.RecipeRepository
import flore.ubb.mob.recipeapp.data.local.RecipeDb
import flore.ubb.mob.recipeapp.core.Result
import kotlinx.coroutines.launch

class RecipesViewModel (application: Application) : AndroidViewModel(application) {

    //private val mutableItems = MutableLiveData<List<Recipe>>().apply { value = emptyList() }
    private val mutableLoading = MutableLiveData<Boolean>().apply { value = false }
    private val mutableException = MutableLiveData<Exception>().apply { value = null }

    val items: LiveData<List<Recipe>>
    val loading: LiveData<Boolean> = mutableLoading
    val loadingError: LiveData<Exception> = mutableException

    val recipeRepository: RecipeRepository

    init {
        val itemDao = RecipeDb.getDatabase(application, viewModelScope).recipeDao()
        recipeRepository = RecipeRepository(itemDao)
        items = recipeRepository.recipes
    }

    fun refresh() {
        viewModelScope.launch {
            Log.v(TAG, "refresh...");
            mutableLoading.value = true
            mutableException.value = null
            when (val result = recipeRepository.refresh()) {
                is Result.Success -> {
                    Log.d(TAG, "refresh succeeded");
                }
                is Result.Error -> {
                    Log.w(TAG, "refresh failed", result.exception);
                    mutableException.value = result.exception
                }
            }
            mutableLoading.value = false
        }
    }
}

/*
class RecipesViewModel (application: Application) : AndroidViewModel(application) {

   private val mutableItems = MutableLiveData<List<Recipe>>().apply { value = emptyList() }
    private val mutableLoading = MutableLiveData<Boolean>().apply { value = false }
    private val mutableException = MutableLiveData<Exception>().apply { value = null }

    var items: LiveData<List<Recipe>> = mutableItems
    val loading: LiveData<Boolean> = mutableLoading
    val loadingError: LiveData<Exception> = mutableException

    val recipeRepository: RecipeRepository

    init {
        val recipeDao = RecipeDb.getDatabase(application, viewModelScope).recipeDao()
        recipeRepository = RecipeRepository(recipeDao)
    }

    fun createItem(position: Int): Unit {
        val list = mutableListOf<Recipe>()
        list.addAll(mutableItems.value!!)
        list.add(Recipe(position.toString(), "Recipe " + position, 0 , "description", "", false, Date()))
        mutableItems.value = list
    }

    fun refresh(){
        viewModelScope.launch {
            Log.v(TAG, "refresh...");
            mutableLoading.value = true
            mutableException.value = null
            var recipes = recipeRepository.getAll()
            mutableItems.value= recipes.value
            items = mutableItems
            Log.d(TAG, mutableItems.value.toString())
            Log.d(TAG, "loadRecipes succeeded")

        }
    }

    fun loadItems() {
        viewModelScope.launch {
            Log.v( TAG, "loadRecipes...");
            mutableLoading.value = true
            mutableException.value = null
            try {
                when (val result = recipeRepository.refresh() ) {
                    is Result.Success -> {
                        Log.d(TAG, "refresh succeeded");
                        mutableItems.value = recipeRepository.cachedItems as MutableList<Recipe>
                        items = mutableItems
                        Log.d(TAG, mutableItems.value.toString())
                        Log.d(TAG, "loadRecipes succeeded");
                    }
                    is Result.Error -> {
                        Log.w(TAG, "refresh failed", result.exception);
                        mutableException.value = result.exception
                    }
                }
                mutableLoading.value = false
            } catch (e: Exception) {
                Log.w(TAG, "loadRecipes failed", e);
                mutableException.value = e
                mutableLoading.value = false
            }
        }
    }
}

 */