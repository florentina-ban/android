package flore.ubb.mob.recipeapp.core

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import flore.ubb.mob.recipeapp.data.Recipe
import flore.ubb.mob.recipeapp.data.RecipeRepository
import flore.ubb.mob.recipeapp.data.remote.RecipeApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.Result

class BackgroundWorker (
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        // perform long running operation
        Log.d("ExampleWorker: ", "beginning")
        var id = inputData.getString("id")
        var name = inputData.getString("name")
        var origin = inputData.getString("origin")
        val likes = inputData.getInt("likes", 0)
        val triedIt = inputData.getBoolean("triedIt", false)
        var description = inputData.getString("description")
        val year = inputData.getInt("dateYear", 2020)
        val month = inputData.getInt("dateMonth", 11)
        val day = inputData.getInt("dateDay", 5)
        val database = inputData.getInt("database", 1)
        val timeStamp = inputData.getLong("timestamp", 0)
        if (id == null)
            id = ""
        if (description == null)
            description = ""
        if (origin == null)
            origin = ""
        if (name == null)
            name = ""
        val work =
            Recipe(id, name, likes, description, origin, triedIt, Date(), timeStamp, database)
        //val repo = inputData.keyValueMap.get("repo") as RecipeRepository
        val repo = RecipeRepository
        // 0, null -> ok;
        // 1 -> doar local -> created
        // 2 -> doar local -> updated
        // 3 -> doar Local -> removed
        withContext(Dispatchers.IO) {
            if (work.database == 1) {
                repo.save(work)
                Log.d("ExampleWorker: "+work.database, "recipe created")
            }

            if (work.database == 2) {
                repo.update(work)
                Log.d("ExampleWorker: "+work.database, "recipe updated")
            }
            if (work.database == 3) {
                repo.remove(work)
                Log.d("ExampleWorker: "+work.database, "recipe removed")
            }
        }
        return Result.success()
    }
}