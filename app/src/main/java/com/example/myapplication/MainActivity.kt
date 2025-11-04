package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// ------------------- API Setup -------------------

data class ApiUser(
    val id: Int,
    val name: String,
    val email: String
)

interface ApiService {
    @GET("users")
    suspend fun getUsers(): List<ApiUser>
}

object RetrofitInstance {
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

// ------------------- Main Activity -------------------

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FirestoreApp()
        }
    }
}

@Composable
fun FirestoreApp() {
    val db = FirebaseFirestore.getInstance()

    var userId by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }

    var message by remember { mutableStateOf("👋 Enter user details and tap Add") }
    var userList by remember { mutableStateOf(listOf<String>()) }
    var loading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "🔥 Firestore + API Integration App",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // ID Input
            OutlinedTextField(
                value = userId,
                onValueChange = { userId = it },
                label = { Text("Enter ID") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Name Input
            OutlinedTextField(
                value = userName,
                onValueChange = { userName = it },
                label = { Text("Enter Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Email Input
            OutlinedTextField(
                value = userEmail,
                onValueChange = { userEmail = it },
                label = { Text("Enter Email") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Add User Button (Firestore)
            Button(
                onClick = {
                    if (userId.isBlank() || userName.isBlank() || userEmail.isBlank()) {
                        message = "⚠️ Please fill all fields!"
                        return@Button
                    }

                    val idValue = userId.toIntOrNull() ?: 0
                    val user = hashMapOf(
                        "id" to idValue,
                        "name" to userName,
                        "email" to userEmail
                    )

                    loading = true
                    db.collection("users")
                        .add(user)
                        .addOnSuccessListener {
                            message = "✅ Added user: $userName"
                            userId = ""
                            userName = ""
                            userEmail = ""
                            loading = false
                        }
                        .addOnFailureListener { e ->
                            message = "❌ Error adding user: ${e.message}"
                            loading = false
                        }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add User to Firestore")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Fetch from Firestore
            Button(
                onClick = {
                    loading = true
                    db.collection("users").get()
                        .addOnSuccessListener { result ->
                            val names = mutableListOf<String>()
                            for (doc in result) {
                                val name = doc.getString("name") ?: "Unnamed"
                                val email = doc.getString("email") ?: ""
                                names.add("$name - $email")
                            }
                            userList = names
                            message = "✅ Fetched ${names.size} users from Firestore!"
                            loading = false
                        }
                        .addOnFailureListener { e ->
                            message = "❌ Error fetching data: ${e.message}"
                            loading = false
                        }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Fetch Users from Firestore")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ------------------- Fetch from API -------------------
            Button(
                onClick = {
                    loading = true
                    message = "🌐 Fetching data from API..."
                    scope.launch {
                        try {
                            val users = RetrofitInstance.api.getUsers()
                            val names = users.map { "${it.name} - ${it.email}" }
                            userList = names
                            message = "✅ API fetch successful!"
                        } catch (e: Exception) {
                            message = "❌ API fetch failed: ${e.message}"
                        } finally {
                            loading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Fetch Users from API")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = message, style = MaterialTheme.typography.bodyMedium)

            if (loading) {
                Spacer(modifier = Modifier.height(10.dp))
                CircularProgressIndicator()
            }

            if (userList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Text("👥 Users:", style = MaterialTheme.typography.titleMedium)
                LazyColumn {
                    items(userList) { user ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = "• $user",
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

