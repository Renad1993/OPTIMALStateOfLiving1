package com.example.optimalstateofliving

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val singinButton = findViewById<Button>(R.id.btsingin)



        // Initialize Firebase Analytics
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        // Initialize Firebase Authentication
        firebaseAuth = FirebaseAuth.getInstance()

        // Initialize Firebase Realtime Database
        firebaseDatabase = FirebaseDatabase.getInstance()

        singinButton.setOnClickListener {
            try {
                val intent = Intent(this, WelcomeActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("MainActivity", "Error starting WelcomeActivity: ${e.message}")
            }
        }

        // Test writing to Firebase Database
        firebaseDatabase.getReference("test").setValue("Test Value")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FirebaseDatabase", "Test value saved successfully!")
                } else {
                    Log.e("FirebaseDatabase", "Error: ${task.exception?.message}")
                }
            }

        // Create a new user
        createNewUser("testuser@example.com", "password123")
    }

    private fun createNewUser(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = task.result?.user?.uid
                    Log.d("FirebaseAuth", "User created successfully with ID: $userId")

                    // Save user data to Realtime Database
                    userId?.let {
                        saveUserToDatabase(it, "Test User", email)
                    }
                } else {
                    Log.e("FirebaseAuth", "Error creating user: ${task.exception?.message}")
                }
            }
    }

    private fun saveUserToDatabase(userId: String, name: String, email: String) {
        val userRef = firebaseDatabase.getReference("users").child(userId)

        val userData = mapOf(
            "name" to name,
            "email" to email
        )

        userRef.setValue(userData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FirebaseDatabase", "User data saved successfully!")
                } else {
                    Log.e("FirebaseDatabase", "Error saving user data: ${task.exception?.message}")
                }
            }
    }
}
