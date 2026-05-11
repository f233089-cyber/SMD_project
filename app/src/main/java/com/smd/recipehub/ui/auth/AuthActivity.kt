package com.smd.recipehub.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.smd.recipehub.BuildConfig
import com.smd.recipehub.R
import com.smd.recipehub.data.firebase.AuthRepository
import com.smd.recipehub.data.firebase.FirestoreRepository
import com.smd.recipehub.ui.main.MainActivity
import kotlinx.coroutines.launch

class AuthActivity : AppCompatActivity() {
    private val authRepository = AuthRepository()
    private val firestoreRepository = FirestoreRepository()

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var progress: ProgressBar
    private lateinit var statusText: TextView

    private val googleLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        runCatching { task.result }
            .onSuccess { account ->
                val idToken = account.idToken
                if (idToken.isNullOrBlank()) {
                    showStatus("Google token missing. Check WEB_CLIENT_ID and SHA-1 in Firebase.")
                } else {
                    lifecycleScope.launch {
                        performAuth {
                            val user = authRepository.signInWithGoogle(idToken)
                            firestoreRepository.ensureUserProfile(user.uid, user.email, user.displayName, user.photoUrl?.toString())
                            openMain()
                        }
                    }
                }
            }
            .onFailure { showStatus(it.localizedMessage ?: "Google sign-in failed") }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (authRepository.currentUser != null) {
            openMain()
            return
        }

        setContentView(R.layout.activity_auth)
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        progress = findViewById(R.id.authProgress)
        statusText = findViewById(R.id.statusText)

        findViewById<Button>(R.id.loginButton).setOnClickListener { login() }
        findViewById<Button>(R.id.registerButton).setOnClickListener { register() }
        findViewById<Button>(R.id.googleButton).setOnClickListener { startGoogleSignIn() }
    }

    private fun login() {
        val email = emailInput.text.toString()
        val password = passwordInput.text.toString()
        if (!validate(email, password)) return

        lifecycleScope.launch {
            performAuth {
                val user = authRepository.signInWithEmail(email, password)
                firestoreRepository.ensureUserProfile(user.uid, user.email, user.displayName, user.photoUrl?.toString())
                openMain()
            }
        }
    }

    private fun register() {
        val email = emailInput.text.toString()
        val password = passwordInput.text.toString()
        if (!validate(email, password)) return

        lifecycleScope.launch {
            performAuth {
                val user = authRepository.registerWithEmail(email, password)
                firestoreRepository.ensureUserProfile(user.uid, user.email, user.displayName, user.photoUrl?.toString())
                openMain()
            }
        }
    }

    private fun startGoogleSignIn() {
        if (BuildConfig.WEB_CLIENT_ID.isBlank()) {
            showStatus("Add WEB_CLIENT_ID in local.properties before using Google Sign-In.")
            return
        }
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.WEB_CLIENT_ID)
            .requestEmail()
            .build()
        val client = GoogleSignIn.getClient(this, options)
        googleLauncher.launch(client.signInIntent)
    }

    private suspend fun performAuth(block: suspend () -> Unit) {
        progress.visibility = View.VISIBLE
        statusText.text = ""
        runCatching { block() }
            .onFailure { showStatus(it.localizedMessage ?: "Authentication error") }
        progress.visibility = View.GONE
    }

    private fun validate(email: String, password: String): Boolean {
        return when {
            email.isBlank() || !email.contains("@") -> {
                showStatus("Enter a valid email.")
                false
            }
            password.length < 6 -> {
                showStatus("Password must be at least 6 characters.")
                false
            }
            else -> true
        }
    }

    private fun showStatus(message: String) {
        statusText.text = message
    }

    private fun openMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
