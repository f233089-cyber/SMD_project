package com.smd.recipehub.ui.saved

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smd.recipehub.R
import com.smd.recipehub.data.model.SavedMeal

class SavedMealAdapter(
    private val onEdit: (SavedMeal) -> Unit,
    private val onDelete: (SavedMeal) -> Unit
) : RecyclerView.Adapter<SavedMealAdapter.SavedMealViewHolder>() {
    private val meals = mutableListOf<SavedMeal>()

    fun submitList(newMeals: List<SavedMeal>) {
        meals.clear()
        meals.addAll(newMeals)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedMealViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_saved_meal, parent, false)
        return SavedMealViewHolder(view)
    }

    override fun getItemCount(): Int = meals.size

    override fun onBindViewHolder(holder: SavedMealViewHolder, position: Int) {
        holder.bind(meals[position])
    }

    inner class SavedMealViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.savedTitle)
        private val meta: TextView = itemView.findViewById(R.id.savedMeta)
        private val notes: TextView = itemView.findViewById(R.id.savedNotes)
        private val editButton: Button = itemView.findViewById(R.id.editSavedButton)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteSavedButton)

        fun bind(meal: SavedMeal) {
            title.text = meal.title
            meta.text = "${meal.categoryName} | ${meal.area.orEmpty()} | Rating: ${meal.rating}/5"
            notes.text = meal.notes.orEmpty()
            editButton.setOnClickListener { onEdit(meal) }
            deleteButton.setOnClickListener { onDelete(meal) }
        }
    }
}
