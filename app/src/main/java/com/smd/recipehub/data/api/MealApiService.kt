package com.smd.recipehub.data.api

import com.smd.recipehub.data.model.MealsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MealApiService {
    @GET("filter.php")
    suspend fun filterByCategory(@Query("c") category: String): MealsResponse

    @GET("search.php")
    suspend fun searchMeals(@Query("s") search: String): MealsResponse

    @GET("lookup.php")
    suspend fun lookupMeal(@Query("i") id: String): MealsResponse
}
