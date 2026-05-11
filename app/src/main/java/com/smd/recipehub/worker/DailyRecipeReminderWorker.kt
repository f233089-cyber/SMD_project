package com.smd.recipehub.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.smd.recipehub.notification.NotificationHelper

class DailyRecipeReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        NotificationHelper.showNotification(
            applicationContext,
            "RecipeHub reminder",
            "Open RecipeHub and plan one healthy meal for today."
        )
        return Result.success()
    }
}
