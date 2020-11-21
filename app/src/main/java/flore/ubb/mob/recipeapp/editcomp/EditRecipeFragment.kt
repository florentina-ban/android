package flore.ubb.mob.recipeapp.editcomp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import flore.ubb.mob.recipeapp.R
import flore.ubb.mob.recipeapp.TAG
import kotlinx.android.synthetic.main.fragment_edit_recipe.*
import kotlinx.android.synthetic.main.fragment_edit_recipe.progress
import kotlinx.android.synthetic.main.fragment_recipe_list.*
import java.util.*
import java.util.Calendar.*

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class EditRecipeFragment : Fragment() {
    companion object {
        const val ITEM_ID = "ITEM_ID"
    }

    private lateinit var viewModel: RecipeEditViewModel
    private var itemId: String=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(TAG, "onCreate")
        arguments?.let {
            if (it.containsKey(ITEM_ID)) {
                itemId = it.getString(ITEM_ID).toString()
            }
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_recipe, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.v(TAG, "onViewCreated")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.v(TAG, "onActivityCreated")
        setupViewModel()

        recipe_likes.maxValue = 1000
        recipe_likes.minValue = 0

        button_save.setOnClickListener {
            Log.v(TAG, "save item")
                val month = recipe_date.month
                val day = recipe_date.dayOfMonth
                val year = recipe_date.year
                var mydate = Date(year, month,day)
                viewModel.saveOrUpdateItem(
                    itemId,
                    recipe_name.text.toString(),
                    recipe_description.text.toString(),
                    recipe_origin.text.toString(),
                    recipe_likes.value,
                    recipe_triedIt.isChecked,
                    mydate
                    )
        }
        button_cancel.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }
        removeButton.setOnClickListener{
            var removed = viewModel.removeItem(itemId)
            //findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this).get(RecipeEditViewModel::class.java)
        viewModel.recipe.observe(viewLifecycleOwner, { recipe->
            Log.v(TAG, "update items")
            //Log.v(TAG,recipe.toString())
            recipe_name.setText(recipe.name)
            recipe_description.setText(recipe.description)
            recipe_likes.value = recipe.likes
            recipe_origin.setText(recipe.origin)
            recipe_triedIt.isChecked = recipe.triedIt
            recipe_date.updateDate(recipe.date.year, recipe.date.month, recipe.date.day)

        })
        viewModel.fetching.observe(viewLifecycleOwner, { fetching ->
            Log.v(TAG, "update fetching")
            progress.visibility = if (fetching) View.VISIBLE else View.GONE
        })
        viewModel.fetchingError.observe(viewLifecycleOwner, { exception ->
            if (exception != null) {
                Log.v(TAG, "update fetching error")
                val message = "Fetching exception ${exception.message}"
                val parentActivity = activity?.parent
                if (parentActivity != null) {
                    Toast.makeText(parentActivity, message, Toast.LENGTH_SHORT).show()
                }
            }
        })
        viewModel.completed.observe(viewLifecycleOwner, Observer { completed ->
            if (completed) {
                Log.v(TAG, "completed, navigate back")
                findNavController().navigateUp()
            }
        })
        val id = itemId
        if (id!=null) {
            viewModel.loadItem(id)
        }
    }
}