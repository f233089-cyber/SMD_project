package com.smd.recipehub.ui.saved

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smd.recipehub.R
import com.smd.recipehub.data.local.SavedMealDao
import com.smd.recipehub.data.model.MealCategory
import com.smd.recipehub.data.model.SavedMeal
import com.smd.recipehub.data.model.SavedMealSort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SavedMealsFragment : Fragment() {
    private lateinit var dao: SavedMealDao
    private lateinit var adapter: SavedMealAdapter
    private lateinit var emptyText: TextView
    private lateinit var searchInput: EditText
    private lateinit var sortSpinner: Spinner

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_saved_meals, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dao = SavedMealDao(requireContext())
        emptyText = view.findViewById(R.id.localEmptyText)
        searchInput = view.findViewById(R.id.localSearchInput)
        sortSpinner = view.findViewById(R.id.sortSpinner)

        adapter = SavedMealAdapter(onEdit = ::showMealDialog, onDelete = ::confirmDelete)
        view.findViewById<RecyclerView>(R.id.localRecyclerView).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SavedMealsFragment.adapter
        }

        sortSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            SavedMealSort.values().map { it.label }
        )

        searchInput.addTextChangedListener { loadMeals() }
        sortSpinner.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, selectedView: View?, position: Int, id: Long) = loadMeals()
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) = Unit
        })

        view.findViewById<Button>(R.id.addLocalMealButton).setOnClickListener { showMealDialog(null) }
        view.findViewById<Button>(R.id.addCategoryButton).setOnClickListener { showCategoryDialog() }

        loadMeals()
    }

    private fun loadMeals() {
        val keyword = searchInput.text.toString()
        val sort = SavedMealSort.values().getOrElse(sortSpinner.selectedItemPosition) { SavedMealSort.NEWEST }
        lifecycleScope.launch {
            val meals = withContext(Dispatchers.IO) { dao.querySavedMeals(keyword, sort) }
            adapter.submitList(meals)
            emptyText.isVisible = meals.isEmpty()
        }
    }

    private fun showMealDialog(existing: SavedMeal?) {
        lifecycleScope.launch {
            val categories = withContext(Dispatchers.IO) { dao.getAllCategories() }
            if (categories.isEmpty()) {
                Toast.makeText(requireContext(), "Create a category first", Toast.LENGTH_SHORT).show()
                return@launch
            }
            showMealDialogWithCategories(existing, categories)
        }
    }

    private fun showMealDialogWithCategories(existing: SavedMeal?, categories: List<MealCategory>) {
        val view = layoutInflater.inflate(R.layout.dialog_saved_meal, null)
        val categorySpinner = view.findViewById<Spinner>(R.id.dialogCategorySpinner)
        val titleInput = view.findViewById<EditText>(R.id.dialogMealTitle)
        val areaInput = view.findViewById<EditText>(R.id.dialogMealArea)
        val ratingInput = view.findViewById<EditText>(R.id.dialogMealRating)
        val notesInput = view.findViewById<EditText>(R.id.dialogMealNotes)
        val imageInput = view.findViewById<EditText>(R.id.dialogMealImage)
        val instructionsInput = view.findViewById<EditText>(R.id.dialogMealInstructions)

        categorySpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories.map { it.name })
        val selectedIndex = categories.indexOfFirst { it.id == existing?.categoryId }
        if (selectedIndex >= 0) categorySpinner.setSelection(selectedIndex)

        titleInput.setText(existing?.title.orEmpty())
        areaInput.setText(existing?.area.orEmpty())
        ratingInput.setText(existing?.rating?.toString().orEmpty())
        notesInput.setText(existing?.notes.orEmpty())
        imageInput.setText(existing?.imageUrl.orEmpty())
        instructionsInput.setText(existing?.instructions.orEmpty())

        AlertDialog.Builder(requireContext())
            .setTitle(if (existing == null) "Add saved meal" else "Edit saved meal")
            .setView(view)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ ->
                val title = titleInput.text.toString().trim()
                if (title.isBlank()) {
                    Toast.makeText(requireContext(), "Title required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val category = categories[categorySpinner.selectedItemPosition]
                val meal = SavedMeal(
                    id = existing?.id ?: 0,
                    categoryId = category.id,
                    mealApiId = existing?.mealApiId,
                    title = title,
                    area = areaInput.text.toString().trim().ifBlank { null },
                    instructions = instructionsInput.text.toString().trim().ifBlank { null },
                    imageUrl = imageInput.text.toString().trim().ifBlank { null },
                    notes = notesInput.text.toString().trim().ifBlank { null },
                    rating = ratingInput.text.toString().toIntOrNull()?.coerceIn(0, 5) ?: 0
                )
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        if (existing == null) dao.insertSavedMeal(meal) else dao.updateSavedMeal(meal)
                    }
                    loadMeals()
                }
            }
            .show()
    }

    private fun showCategoryDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_category, null)
        val input = view.findViewById<EditText>(R.id.categoryNameInput)
        AlertDialog.Builder(requireContext())
            .setTitle("Add category")
            .setView(view)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ ->
                val name = input.text.toString()
                lifecycleScope.launch {
                    runCatching { withContext(Dispatchers.IO) { dao.insertCategory(name) } }
                        .onSuccess { Toast.makeText(requireContext(), "Category saved", Toast.LENGTH_SHORT).show() }
                        .onFailure { Toast.makeText(requireContext(), it.localizedMessage ?: "Failed", Toast.LENGTH_SHORT).show() }
                }
            }
            .show()
    }

    private fun confirmDelete(meal: SavedMeal) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete meal")
            .setMessage("Delete ${meal.title} from local SQLite storage?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) { dao.deleteSavedMeal(meal.id) }
                    loadMeals()
                }
            }
            .show()
    }
}
