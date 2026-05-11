package com.smd.recipehub

import android.app.Application
import com.smd.recipehub.notification.NotificationHelper

class RecipeHubApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
    }
}
