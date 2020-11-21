package flore.ubb.mob.recipeapp.listcomp

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import flore.ubb.mob.recipeapp.R
import flore.ubb.mob.recipeapp.TAG
import flore.ubb.mob.recipeapp.auth.data.AuthRepository
import flore.ubb.mob.recipeapp.auth.data.remote.RemoteAuthDataSource
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

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class RecipeListFragment : Fragment() {
    private lateinit var recipeListAdapter: RecipeListAdapter
    private lateinit var recipesModel: RecipesViewModel
    private var isActive: Boolean = false

    override fun onStop() {
        super.onStop()
        isActive = false
    }

    override fun onStart() {
        super.onStart()
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
            var recipe: Recipe = Recipe(id, name, likes, description,origin,triedIt,realDate)
            Log.d("WebSocket: ", type)
            Log.d("WebSocket: ", id)

            if  (type.equals("removed")) {
                Log.d("WebSocket: ", "equal")
                recipesModel.recipeRepository.removeFromList(id)
            }
            if  (type.equals("created")) {
                Log.d("WebSocket: ", "equal")
                recipesModel.recipeRepository.addToList(recipe);
            }
            if  (type.equals("updated")) {
                Log.d("WebSocket: ", "equal")
                recipesModel.recipeRepository.updateList(recipe);
            }
            recipeListAdapter.notifyDataSetChanged()
           // runBlocking { WebsocketCreator.eventChannel.send(event) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(TAG, "onCreate")
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recipe_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.v(TAG, "onActivityCreated")
        if (!AuthRepository.isLoggedIn){
            findNavController().navigate(R.id.login_fragment);
        }
        CoroutineScope(Dispatchers.Main).launch { collectEvents() }

        setupItemList()
        add_recipe_button.setOnClickListener {
            Log.v(TAG, "add new item")
            findNavController().navigate(R.id.SecondFragment)
        }
        logout_button.setOnClickListener{
            Log.v(TAG, "logout")
            AuthRepository.logout()
            recipesModel.recipeRepository.clearCash()
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
        recipesModel.loadItems()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}