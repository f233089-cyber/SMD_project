package com.smd.recipehub.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.smd.recipehub.BuildConfig
import com.smd.recipehub.R
import com.smd.recipehub.notification.NotificationHelper
import com.smd.recipehub.ui.api.ApiMealsFragment
import com.smd.recipehub.ui.auth.AuthActivity
import com.smd.recipehub.ui.cloud.CloudPlansFragment
import com.smd.recipehub.ui.profile.ProfileComposeFragment
import com.smd.recipehub.ui.saved.SavedMealsFragment
import com.smd.recipehub.worker.DailyRecipeReminderWorker
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private val notificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)
        NotificationHelper.createChannels(this)
        requestNotificationPermissionIfNeeded()
        scheduleDailyReminder()

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_explore -> openFragment(ApiMealsFragment())
                R.id.nav_saved -> openFragment(SavedMealsFragment())
                R.id.nav_cloud -> openFragment(CloudPlansFragment())
                R.id.nav_profile -> openFragment(ProfileComposeFragment())
                else -> false
            }
            true
        }

        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.nav_explore
        }
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()
        if (BuildConfig.WEB_CLIENT_ID.isNotBlank()) {
            val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.WEB_CLIENT_ID)
                .requestEmail()
                .build()
            GoogleSignIn.getClient(this, options).signOut()
        }
        startActivity(Intent(this, AuthActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    private fun openFragment(fragment: Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
        return true
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            if (!granted) notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun scheduleDailyReminder() {
        val request = PeriodicWorkRequestBuilder<DailyRecipeReminderWorker>(24, TimeUnit.HOURS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "recipehub_daily_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
