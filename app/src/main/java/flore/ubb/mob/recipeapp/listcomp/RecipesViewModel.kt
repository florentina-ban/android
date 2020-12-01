package flore.ubb.mob.recipeapp.listcomp

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import androidx.work.*
import flore.ubb.mob.recipeapp.core.BackgroundWorker
import flore.ubb.mob.recipeapp.data.Recipe
import flore.ubb.mob.recipeapp.data.RecipeRepository
import flore.ubb.mob.recipeapp.data.local.RecipeDb
import flore.ubb.mob.recipeapp.core.Result
import flore.ubb.mob.recipeapp.core.TAG
import kotlinx.coroutines.launch

class RecipesViewModel (application: Application) : AndroidViewModel(application) {

    //private val mutableItems = MutableLiveData<List<Recipe>>().apply { value = emptyList() }
    private val mutableLoading = MutableLiveData<Boolean>().apply { value = false }
    private val mutableException = MutableLiveData<Exception>().apply { value = null }

    val items: LiveData<List<Recipe>>
    var itemsForWorkerLive: LiveData<List<Recipe>>
    val loading: LiveData<Boolean> = mutableLoading
    val loadingError: LiveData<Exception> = mutableException
    var itemsForWorker: List<Recipe> = emptyList()

    val recipeRepository = RecipeRepository

    init {
        val itemDao = RecipeDb.getDatabase(application, viewModelScope).recipeDao()
        recipeRepository.setRecipeDao(itemDao)
        items = recipeRepository.recipes
        itemsForWorkerLive = recipeRepository.recipesLocal
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

    @SuppressLint("RestrictedApi")
    fun startAndObserveJob(context: Context) {
        // setup WorkRequest
        val work: MutableList<Data> = mutableListOf()
        for (item in itemsForWorker) {
            viewModelScope.launch {
                // val work = itemsForWorker
                Log.d("indide worker - work:", item.toString())
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
                val inputData = Data.Builder()
                    .put("id", item._id)
                    .put("name", item.name)
                    .put("origin",item.origin)
                    .put("likes",item.likes)
                    .put("triedIt", item.triedIt)
                    .put("description", item.description)
                    .put("dateYear",item.date.year)
                    .put("dateMonth", item.date.month)
                    .put("dateDay",item.date.day)
                    .put("database", item.database)
                    .put("timestamp", item.timestamp)
                    .build()
//        val myWork = PeriodicWorkRequestBuilder<ExampleWorker>(1, TimeUnit.MINUTES)
                val myWork = OneTimeWorkRequest.Builder(BackgroundWorker::class.java)
                    .setConstraints(constraints)
                    .setInputData(inputData)
                    .build()
                val workId = myWork.id
                WorkManager.getInstance(context).apply {
                    // enqueue Work
                    enqueue(myWork)
                    // observe work status
//                getWorkInfoByIdLiveData(workId)
//                    .observe(viewLifecycleOwner, { status ->
//                        val isFinished = status?.state?.isFinished
//                        Log.d(TAG, "Job $workId; finished: $isFinished")
//                    })
                }
            }
            //Toast.makeText(this, "Job $workId enqueued", Toast.LENGTH_SHORT).show()
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