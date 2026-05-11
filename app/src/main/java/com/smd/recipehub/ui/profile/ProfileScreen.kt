package com.smd.recipehub.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun ProfileScreen(
    email: String,
    uid: String,
    localCount: Int,
    cloudPlanCount: Int,
    photoUri: String?,
    onPickPhoto: () -> Unit,
    onLogout: () -> Unit
) {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text("Profile Dashboard", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            if (!photoUri.isNullOrBlank()) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = "Selected profile photo",
                    modifier = Modifier.size(110.dp)
                )
                Spacer(Modifier.height(8.dp))
            }

            Button(onClick = onPickPhoto, modifier = Modifier.fillMaxWidth()) {
                Text("Pick profile photo")
            }

            Spacer(Modifier.height(16.dp))

            InfoCard(title = "Signed-in user", body = "Email: $email\nUID: $uid")
            InfoCard(title = "Local SQLite records", body = "$localCount saved meals available offline")
            InfoCard(title = "Cloud Firestore records", body = "$cloudPlanCount real-time meal plans")
            InfoCard(title = "Self-researched features", body = "WorkManager reminder + Android Photo Picker")

            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
                Text("Logout")
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, body: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(body)
        }
    }
}
