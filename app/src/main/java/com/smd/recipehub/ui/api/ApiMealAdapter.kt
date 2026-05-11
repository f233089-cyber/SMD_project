package com.smd.recipehub.ui.api

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.smd.recipehub.R
import com.smd.recipehub.data.model.MealDto

class ApiMealAdapter(
    private val onSave: (MealDto) -> Unit
) : RecyclerView.Adapter<ApiMealAdapter.ApiMealViewHolder>() {
    private val meals = mutableListOf<MealDto>()

    fun submitList(newMeals: List<MealDto>) {
        meals.clear()
        meals.addAll(newMeals)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApiMealViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_api_meal, parent, false)
        return ApiMealViewHolder(view)
    }

    override fun getItemCount(): Int = meals.size

    override fun onBindViewHolder(holder: ApiMealViewHolder, position: Int) {
        holder.bind(meals[position])
    }

    inner class ApiMealViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val image: ImageView = itemView.findViewById(R.id.mealImage)
        private val title: TextView = itemView.findViewById(R.id.mealTitle)
        private val subtitle: TextView = itemView.findViewById(R.id.mealSubtitle)
        private val saveButton: Button = itemView.findViewById(R.id.saveApiMealButton)

        fun bind(meal: MealDto) {
            title.text = meal.strMeal ?: "Unnamed meal"
            subtitle.text = listOfNotNull(meal.strCategory, meal.strArea).joinToString(" - ").ifBlank { "API meal" }
            Glide.with(image)
                .load(meal.strMealThumb)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(image)
            saveButton.setOnClickListener { onSave(meal) }
        }
    }
}
