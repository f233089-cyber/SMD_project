package com.smd.recipehub.ui.api

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smd.recipehub.R
import com.smd.recipehub.data.api.RetrofitClient
import com.smd.recipehub.data.local.SavedMealDao
import com.smd.recipehub.data.model.MealDto
import com.smd.recipehub.data.model.SavedMeal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ApiMealsFragment : Fragment() {
    private lateinit var adapter: ApiMealAdapter
    private lateinit var dao: SavedMealDao
    private lateinit var progress: ProgressBar
    private lateinit var emptyText: TextView
    private lateinit var searchInput: EditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_api_meals, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dao = SavedMealDao(requireContext())
        progress = view.findViewById(R.id.apiProgress)
        emptyText = view.findViewById(R.id.apiEmptyText)
        searchInput = view.findViewById(R.id.apiSearchInput)

        adapter = ApiMealAdapter(onSave = ::saveApiMeal)
        view.findViewById<RecyclerView>(R.id.apiRecyclerView).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ApiMealsFragment.adapter
        }

        view.findViewById<Button>(R.id.apiSearchButton).setOnClickListener {
            loadMeals(searchInput.text.toString())
        }

        loadMeals("Chicken")
    }

    private fun loadMeals(query: String) {
        progress.isVisible = true
        emptyText.isVisible = false
        lifecycleScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    if (query.isBlank()) {
                        RetrofitClient.mealApi.filterByCategory("Chicken").meals.orEmpty()
                    } else {
                        RetrofitClient.mealApi.searchMeals(query).meals.orEmpty()
                    }
                }
            }.onSuccess { meals ->
                adapter.submitList(meals)
                emptyText.isVisible = meals.isEmpty()
                emptyText.text = if (meals.isEmpty()) "No meals found." else ""
            }.onFailure { error ->
                emptyText.isVisible = true
                emptyText.text = error.localizedMessage ?: "Unable to load meals."
            }
            progress.isVisible = false
        }
    }

    private fun saveApiMeal(meal: MealDto) {
        val id = meal.idMeal
        if (id.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Missing API id", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val detailedMeal = RetrofitClient.mealApi.lookupMeal(id).meals?.firstOrNull() ?: meal
                    val categoryId = dao.ensureCategory(detailedMeal.strCategory ?: "API Saved")
                    dao.insertSavedMeal(
                        SavedMeal(
                            categoryId = categoryId,
                            mealApiId = detailedMeal.idMeal,
                            title = detailedMeal.strMeal ?: "API Meal",
                            area = detailedMeal.strArea,
                            instructions = detailedMeal.strInstructions,
                            imageUrl = detailedMeal.strMealThumb,
                            notes = "Saved from TheMealDB API",
                            rating = 5
                        )
                    )
                }
            }.onSuccess {
                Toast.makeText(requireContext(), "Meal saved locally", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(requireContext(), it.localizedMessage ?: "Save failed", Toast.LENGTH_LONG).show()
            }
        }
    }
}
