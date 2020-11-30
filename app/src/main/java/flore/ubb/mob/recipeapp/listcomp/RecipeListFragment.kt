package flore.ubb.mob.recipeapp.listcomp

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.work.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import flore.ubb.mob.recipeapp.R
import flore.ubb.mob.recipeapp.auth.data.AuthRepository
import flore.ubb.mob.recipeapp.auth.data.remote.RemoteAuthDataSource
import flore.ubb.mob.recipeapp.core.BackgroundWorker
import flore.ubb.mob.recipeapp.core.ConnectivityLiveData
import flore.ubb.mob.recipeapp.core.TAG
import flore.ubb.mob.recipeapp.data.Recipe
import flore.ubb.mob.recipeapp.data.RecipeRepository
import flore.ubb.mob.recipeapp.data.remote.RecipeApi
import flore.ubb.mob.recipeapp.data.remote.WebsocketCreator
import flore.ubb.mob.recipeapp.editcomp.EditRecipeFragment
import kotlinx.android.synthetic.main.fragment_recipe_list.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class WebSocketEvent(val event: String, val mypay: Recipe){
    val type: String = event
    val payload : Recipe = mypay
}

class RecipeListFragment : Fragment() {
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var connectivityLiveData: ConnectivityLiveData
    private lateinit var myContext: Context
    private lateinit var recipeListAdapter: RecipeListAdapter
    private lateinit var recipesModel: RecipesViewModel
    private var isActive: Boolean = false

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onStop() {
        super.onStop()
        isActive = false
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onStart() {
        super.onStart()
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
        isActive = true

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun collectEvents() {
        while (true) {
            val event = WebsocketCreator.eventChannel.receive()
            Log.d(TAG, "received $event")
            var elem = JSONObject(event)
            var type = elem.getString("event")
            var payload = elem.getJSONObject("payload")
            var id = payload.getString("_id")
            var name = payload.getString("name")
            var description = payload.getString("description")
            var origin = payload.getString("origin")
            var likes = payload.getInt("likes")
            var triedIt = payload.getBoolean("triedIt")
            //var date: Long = Date.parse(payload.getString("date"))
            var realDate = Date()
            //var realDate = Date(date)
            var recipe: Recipe = Recipe(id, name, likes, description,origin,triedIt,realDate, null, null)
            Log.d("WebSocket: ", type)
            Log.d("WebSocket: ", id)

            if  (type.equals("removed")) {
                Log.d("WebSocket: ", "equal")
                recipesModel.recipeRepository.deleteOne(id)
            }
            if  (type.equals("created")) {
                Log.d("WebSocket: ", "equal")
                recipesModel.recipeRepository.addToList(recipe);
            }
            if  (type.equals("updated")) {
                Log.d("WebSocket: ", "equal")
                recipesModel.recipeRepository.updateDao(recipe);
            }
            recipeListAdapter.notifyDataSetChanged()
           // runBlocking { WebsocketCreator.eventChannel.send(event) }
        }
    }
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(TAG, "onCreate")
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        myContext = inflater.context
        return inflater.inflate(R.layout.fragment_recipe_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.v(TAG, "onActivityCreated")
        if (!AuthRepository.isLoggedIn) {
            findNavController().navigate(R.id.login_fragment);
        }
        CoroutineScope(Dispatchers.Main).launch { collectEvents() }

        setupItemList()

        connectivityManager =
            ContextCompat.getSystemService(myContext, android.net.ConnectivityManager::class.java)!!
        connectivityLiveData = ConnectivityLiveData(connectivityManager)
        connectivityLiveData.observe(viewLifecycleOwner, {
            view?.findViewById<CheckBox>(R.id.connected_check)?.isChecked = it
        })

        add_recipe_button.setOnClickListener {
            Log.v(TAG, "add new item")
            findNavController().navigate(R.id.SecondFragment)
        }
        logout_button.setOnClickListener{
            Log.v(TAG, "logout")
            AuthRepository.logout()
            //recipesModel.recipeRepository.clearCash()
            findNavController().navigate(R.id.login_fragment)
        }

    }

    private fun setupItemList() {
        recipeListAdapter = RecipeListAdapter(this)
        recipe_list.adapter = recipeListAdapter
        recipesModel = ViewModelProvider(this).get(RecipesViewModel::class.java)
        recipesModel.items.observe(viewLifecycleOwner, { items ->
            Log.v(TAG, "update items")
            recipeListAdapter.items = items
        })
        recipesModel.loading.observe(viewLifecycleOwner, { loading ->
            Log.i(TAG, "update loading")
            progress.visibility = if (loading) View.VISIBLE else View.GONE
        })
        recipesModel.loadingError.observe(viewLifecycleOwner, { exception ->
            if (exception != null) {
                Log.i(TAG, "update loading error")
                val message = "Loading exception ${exception.message}"
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
            }
        })
        recipesModel.refresh()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v(TAG, "onDestroy")
    }

    val networkCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            Log.d(TAG, "The default network is now: " + network)

        }

        override fun onLost(network: Network) {
            Log.d(
                TAG,
                "The application no longer has a default network. The last default network was " + network
            )
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            Log.d(TAG, "The default network changed capabilities: " + networkCapabilities)
        }

        override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
            Log.d(TAG, "The default network changed link properties: " + linkProperties)
        }
    }
    @SuppressLint("RestrictedApi")
    private fun startAndObserveJob() {
        // setup WorkRequest
        val work = recipesModel.recipeRepository.getRecipesForWorker()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val inputData = Data.Builder()
            .put("work",work)
            .build()
//        val myWork = PeriodicWorkRequestBuilder<ExampleWorker>(1, TimeUnit.MINUTES)
        val myWork = OneTimeWorkRequest.Builder(BackgroundWorker::class.java)
            .setConstraints(constraints)
            .setInputData(inputData)
            .build()
        val workId = myWork.id
        WorkManager.getInstance(myContext).apply {
            // enqueue Work
            enqueue(myWork)
            // observe work status
            getWorkInfoByIdLiveData(workId)
                .observe(viewLifecycleOwner, { status ->
                    val isFinished = status?.state?.isFinished
                    Log.d(TAG, "Job $workId; finished: $isFinished")
                })
        }
        //Toast.makeText(this, "Job $workId enqueued", Toast.LENGTH_SHORT).show()
    }

}