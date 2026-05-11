package com.smd.recipehub.data.model

data class SavedMeal(
    val id: Long = 0,
    val categoryId: Long = 0,
    val categoryName: String = "",
    val mealApiId: String? = null,
    val title: String = "",
    val area: String? = null,
    val instructions: String? = null,
    val imageUrl: String? = null,
    val notes: String? = null,
    val rating: Int = 0,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
)

enum class SavedMealSort(val label: String) {
    NEWEST("Newest first"),
    TITLE_ASC("Title A-Z"),
    RATING_DESC("Rating high-low")
}
