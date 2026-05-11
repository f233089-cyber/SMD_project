package com.smd.recipehub.data.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.smd.recipehub.data.model.MealCategory
import com.smd.recipehub.data.model.SavedMeal
import com.smd.recipehub.data.model.SavedMealSort

class SavedMealDao(context: Context) {
    private val helper = DatabaseHelper(context.applicationContext)

    fun insertCategory(name: String): Long {
        val cleanName = name.trim()
        require(cleanName.isNotBlank()) { "Category name cannot be empty" }
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_CATEGORY_NAME, cleanName)
            put(DatabaseHelper.COL_CATEGORY_CREATED_AT, System.currentTimeMillis())
        }
        return helper.writableDatabase.insertWithOnConflict(
            DatabaseHelper.TABLE_CATEGORIES,
            null,
            values,
            android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE
        )
    }

    fun ensureCategory(name: String): Long {
        val cleanName = name.trim().ifBlank { "Favorites" }
        getCategoryByName(cleanName)?.let { return it.id }
        val inserted = insertCategory(cleanName)
        return if (inserted > 0) inserted else getCategoryByName(cleanName)?.id ?: 1L
    }

    fun getCategoryByName(name: String): MealCategory? {
        helper.readableDatabase.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABLE_CATEGORIES} WHERE ${DatabaseHelper.COL_CATEGORY_NAME} = ? LIMIT 1",
            arrayOf(name)
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.toCategory() else null
        }
    }

    fun getAllCategories(): List<MealCategory> {
        val list = mutableListOf<MealCategory>()
        helper.readableDatabase.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABLE_CATEGORIES} ORDER BY ${DatabaseHelper.COL_CATEGORY_NAME} ASC",
            emptyArray()
        ).use { cursor ->
            while (cursor.moveToNext()) list.add(cursor.toCategory())
        }
        return list
    }

    fun deleteCategory(categoryId: Long): Int {
        return helper.writableDatabase.delete(
            DatabaseHelper.TABLE_CATEGORIES,
            "${DatabaseHelper.COL_CATEGORY_ID} = ?",
            arrayOf(categoryId.toString())
        )
    }

    fun insertSavedMeal(meal: SavedMeal): Long {
        require(meal.title.trim().isNotBlank()) { "Meal title cannot be empty" }
        val now = System.currentTimeMillis()
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_SAVED_CATEGORY_ID, meal.categoryId)
            put(DatabaseHelper.COL_SAVED_API_ID, meal.mealApiId)
            put(DatabaseHelper.COL_SAVED_TITLE, meal.title.trim())
            put(DatabaseHelper.COL_SAVED_AREA, meal.area)
            put(DatabaseHelper.COL_SAVED_INSTRUCTIONS, meal.instructions)
            put(DatabaseHelper.COL_SAVED_IMAGE_URL, meal.imageUrl)
            put(DatabaseHelper.COL_SAVED_NOTES, meal.notes)
            put(DatabaseHelper.COL_SAVED_RATING, meal.rating.coerceIn(0, 5))
            put(DatabaseHelper.COL_SAVED_CREATED_AT, now)
            put(DatabaseHelper.COL_SAVED_UPDATED_AT, now)
        }
        return helper.writableDatabase.insertOrThrow(DatabaseHelper.TABLE_SAVED_MEALS, null, values)
    }

    fun updateSavedMeal(meal: SavedMeal): Int {
        require(meal.id > 0) { "Saved meal id is required" }
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_SAVED_CATEGORY_ID, meal.categoryId)
            put(DatabaseHelper.COL_SAVED_TITLE, meal.title.trim())
            put(DatabaseHelper.COL_SAVED_AREA, meal.area)
            put(DatabaseHelper.COL_SAVED_INSTRUCTIONS, meal.instructions)
            put(DatabaseHelper.COL_SAVED_IMAGE_URL, meal.imageUrl)
            put(DatabaseHelper.COL_SAVED_NOTES, meal.notes)
            put(DatabaseHelper.COL_SAVED_RATING, meal.rating.coerceIn(0, 5))
            put(DatabaseHelper.COL_SAVED_UPDATED_AT, System.currentTimeMillis())
        }
        return helper.writableDatabase.update(
            DatabaseHelper.TABLE_SAVED_MEALS,
            values,
            "${DatabaseHelper.COL_SAVED_ID} = ?",
            arrayOf(meal.id.toString())
        )
    }

    fun deleteSavedMeal(savedMealId: Long): Int {
        return helper.writableDatabase.delete(
            DatabaseHelper.TABLE_SAVED_MEALS,
            "${DatabaseHelper.COL_SAVED_ID} = ?",
            arrayOf(savedMealId.toString())
        )
    }

    fun querySavedMeals(keyword: String, sort: SavedMealSort): List<SavedMeal> {
        val orderBy = when (sort) {
            SavedMealSort.NEWEST -> "sm.${DatabaseHelper.COL_SAVED_CREATED_AT} DESC"
            SavedMealSort.TITLE_ASC -> "sm.${DatabaseHelper.COL_SAVED_TITLE} COLLATE NOCASE ASC"
            SavedMealSort.RATING_DESC -> "sm.${DatabaseHelper.COL_SAVED_RATING} DESC, sm.${DatabaseHelper.COL_SAVED_TITLE} ASC"
        }

        val like = "%${keyword.trim()}%"
        val list = mutableListOf<SavedMeal>()
        val sql = """
            SELECT
                sm.${DatabaseHelper.COL_SAVED_ID} AS saved_id,
                sm.${DatabaseHelper.COL_SAVED_CATEGORY_ID} AS category_id,
                c.${DatabaseHelper.COL_CATEGORY_NAME} AS category_name,
                sm.${DatabaseHelper.COL_SAVED_API_ID} AS meal_api_id,
                sm.${DatabaseHelper.COL_SAVED_TITLE} AS title,
                sm.${DatabaseHelper.COL_SAVED_AREA} AS area,
                sm.${DatabaseHelper.COL_SAVED_INSTRUCTIONS} AS instructions,
                sm.${DatabaseHelper.COL_SAVED_IMAGE_URL} AS image_url,
                sm.${DatabaseHelper.COL_SAVED_NOTES} AS notes,
                sm.${DatabaseHelper.COL_SAVED_RATING} AS rating,
                sm.${DatabaseHelper.COL_SAVED_CREATED_AT} AS created_at,
                sm.${DatabaseHelper.COL_SAVED_UPDATED_AT} AS updated_at
            FROM ${DatabaseHelper.TABLE_SAVED_MEALS} sm
            INNER JOIN ${DatabaseHelper.TABLE_CATEGORIES} c
                ON sm.${DatabaseHelper.COL_SAVED_CATEGORY_ID} = c.${DatabaseHelper.COL_CATEGORY_ID}
            WHERE sm.${DatabaseHelper.COL_SAVED_TITLE} LIKE ?
               OR sm.${DatabaseHelper.COL_SAVED_NOTES} LIKE ?
               OR c.${DatabaseHelper.COL_CATEGORY_NAME} LIKE ?
            ORDER BY $orderBy
        """.trimIndent()

        helper.readableDatabase.rawQuery(sql, arrayOf(like, like, like)).use { cursor ->
            while (cursor.moveToNext()) list.add(cursor.toSavedMeal())
        }
        return list
    }

    fun countSavedMeals(): Int {
        helper.readableDatabase.rawQuery("SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_SAVED_MEALS}", emptyArray()).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }
    }

    private fun Cursor.toCategory(): MealCategory {
        return MealCategory(
            id = getLong(getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY_ID)),
            name = getString(getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY_NAME)),
            createdAt = getLong(getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY_CREATED_AT))
        )
    }

    private fun Cursor.toSavedMeal(): SavedMeal {
        return SavedMeal(
            id = getLong(getColumnIndexOrThrow("saved_id")),
            categoryId = getLong(getColumnIndexOrThrow("category_id")),
            categoryName = getString(getColumnIndexOrThrow("category_name")),
            mealApiId = getStringOrNull("meal_api_id"),
            title = getString(getColumnIndexOrThrow("title")),
            area = getStringOrNull("area"),
            instructions = getStringOrNull("instructions"),
            imageUrl = getStringOrNull("image_url"),
            notes = getStringOrNull("notes"),
            rating = getInt(getColumnIndexOrThrow("rating")),
            createdAt = getLong(getColumnIndexOrThrow("created_at")),
            updatedAt = getLong(getColumnIndexOrThrow("updated_at"))
        )
    }

    private fun Cursor.getStringOrNull(columnName: String): String? {
        val index = getColumnIndexOrThrow(columnName)
        return if (isNull(index)) null else getString(index)
    }
}
