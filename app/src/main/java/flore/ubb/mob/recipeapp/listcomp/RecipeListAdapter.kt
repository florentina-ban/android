package flore.ubb.mob.recipeapp.listcomp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import flore.ubb.mob.recipeapp.R
import flore.ubb.mob.recipeapp.data.Recipe
import flore.ubb.mob.recipeapp.editcomp.EditRecipeFragment
import kotlinx.android.synthetic.main.view_recipe.view.*

class RecipeListAdapter(
    private val fragment: Fragment
) : RecyclerView.Adapter<RecipeListAdapter.ViewHolder>() {

    var items = emptyList<Recipe>()
        set(value) {
            field = value
            notifyDataSetChanged();
        }

    private var onItemClick: View.OnClickListener;

    init {
        onItemClick = View.OnClickListener { view ->
            val item = view.tag as Recipe
            fragment.findNavController().navigate(R.id.SecondFragment, Bundle().apply {
               putString(EditRecipeFragment.ITEM_ID, item._id)
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_recipe, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recipe = items[position]
        holder.itemView.tag = recipe
        holder.textView.text = recipe.name
        holder.itemView.setOnClickListener(onItemClick)
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.recipe
    }
}