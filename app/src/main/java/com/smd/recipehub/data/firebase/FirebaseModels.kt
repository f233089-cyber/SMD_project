package com.smd.recipehub.data.firebase

data class AppUser(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUri: String? = null,
    val fcmToken: String? = null,
    val updatedAt: Long = 0
)

data class CloudMealPlan(
    val id: String = "",
    val ownerId: String = "",
    val title: String = "",
    val targetDate: String = "",
    val note: String = "",
    val updatedAt: Long = 0
)

data class CloudMealItem(
    val id: String = "",
    val planId: String = "",
    val mealTitle: String = "",
    val checked: Boolean = false,
    val updatedAt: Long = 0
)
