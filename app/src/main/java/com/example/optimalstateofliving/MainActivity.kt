package com.example.optimalstateofliving

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_main)

        // ربط العناصر مع واجهة المستخدم
        emailEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.btsingin)
        signUpUser = findViewById(R.id.newUserTextView)

        // مرجع قاعدة البيانات
        databaseRef = FirebaseDatabase.getInstance().getReference("_user")

        // الحدث عند الضغط على زر تسجيل الدخول
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginWithFirebaseAuth(email, password)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // الانتقال إلى شاشة تسجيل حساب جديد
        signUpUser.setOnClickListener {
            val intent = Intent(this, CreateAccountActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginWithFirebaseAuth(email: String, password: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    if (currentUser != null) {
                        val userId = currentUser.uid
                        Log.d("Login", "User ID: $userId")

                        // جلب بيانات المستخدم من قاعدة البيانات
                        databaseRef.child(userId).get().addOnSuccessListener { snapshot ->
                            if (snapshot.exists()) {
                                val role = snapshot.child("role").getValue(String::class.java)
                                if (role == "client") {
                                    val intent = Intent(this, WelcomeActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                } else if (role == "provider") {
                                    val intent = Intent(this, ProviderActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Toast.makeText(this, "Unknown role", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(this, "User data not found in database.", Toast.LENGTH_SHORT).show()
                            }
                        }.addOnFailureListener { e ->
                            Toast.makeText(this, "Error fetching user data: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    // حدث خطأ أثناء تسجيل الدخول
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
