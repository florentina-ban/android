package flore.ubb.mob.recipeapp.core

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import flore.ubb.mob.recipeapp.data.Recipe
import flore.ubb.mob.recipeapp.data.remote.RecipeApi
import java.util.concurrent.TimeUnit
import kotlin.Result

class BackgroundWorker (
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {
    override fun doWork(): Result {
        // perform long running operation
        val work = inputData.keyValueMap.get("work")
        for (i in 1..3) {
            TimeUnit.SECONDS.sleep(1)
            //maybe one work at a time??

            Log.d("ExampleWorker", "progress: $i")
        }
        return Result.success()
    }
}