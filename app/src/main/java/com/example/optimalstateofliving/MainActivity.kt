package com.example.optimalstateofliving

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var databaseRef: DatabaseReference
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var signUpUser: TextView
    private lateinit var forgetPassword : TextView
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_main)

        firebaseAuth = FirebaseAuth.getInstance()


        // Connecting elements with user interface
        emailEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.btsingin)
        signUpUser = findViewById(R.id.newUserTextView)
        forgetPassword = findViewById(R.id.forgotPasswordTextView)


        // Database Reference
        databaseRef = FirebaseDatabase.getInstance().getReference("_user")

        // Event when you hit the login button
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginWithFirebaseAuth(email, password)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Go to a new account registration screen
        signUpUser.setOnClickListener {
            val intent = Intent(this, CreateAccountActivity::class.java)
            startActivity(intent)
        }

        //  Forgot Password Button
        forgetPassword.setOnClickListener {
            showResetPasswordDialog()
        }
    }

    // check user Authentication
    private fun loginWithFirebaseAuth(email: String, password: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    if (currentUser != null) {
                        val userId = currentUser.uid
                        Log.d("Login", "User ID: $userId")

                        // Bringing user data from the database
                        databaseRef.child(userId).get().addOnSuccessListener { snapshot ->
                            if (snapshot.exists()) {
                                val role = snapshot.child("role").getValue(String::class.java)
                                val firstName = snapshot.child("FirstName").getValue(String::class.java)?.trim()
                                val lastName = snapshot.child("LastName").getValue(String::class.java)?.trim()
                                val intent = if (role == "client") {
                                    Intent(this, WelcomeActivity::class.java)

                                } else {
                                    Intent(this, ProviderActivity::class.java)

                                }

                                // Pass User information to the following activity
                                intent.putExtra("FirstName", firstName)
                                intent.putExtra("LastName", lastName)
                                intent.putExtra("Role", role)

                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this, "User data not found in database.", Toast.LENGTH_SHORT).show()
                            }
                        }.addOnFailureListener { e ->
                            Toast.makeText(this, "Error fetching user data: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    // Error occurred during login
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    //Forget password dialog box
    private fun showResetPasswordDialog() {
        // Email Entry Dialog Box
        val emailInput = EditText(this)
        emailInput.hint = "Enter your email"

        AlertDialog.Builder(this)
            .setTitle("Reset Password")
            .setMessage("Enter your email to receive a password reset link.")
            .setView(emailInput)
            .setPositiveButton("Send") { _, _ ->
                val email = emailInput.text.toString()
                if (email.isNotEmpty()) {
                    sendPasswordResetEmail(email)
                } else {
                    Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun sendPasswordResetEmail(email: String) {
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Password reset email sent!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }


}


