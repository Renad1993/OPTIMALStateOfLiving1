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
                login(email, password)
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

    private fun login(email: String, password: String) {
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var isUserFound = false

                for (userSnapshot in snapshot.children) {
                    val storedEmail = userSnapshot.child("email").getValue(String::class.java)?.trim()
                    val storedPassword = userSnapshot.child("password").getValue()?.toString()?.trim()

                    Log.d("Login", "Entered Email: $email, Stored Email: $storedEmail")
                    Log.d("Login", "Entered Password: $password, Stored Password: $storedPassword")

                    if (storedEmail?.equals(email, ignoreCase = true) == true) {
                        isUserFound = true
                        if (storedPassword == password) {
                            val role = userSnapshot.child("role").getValue(String::class.java)?.trim()
                            Log.d("Login", "User role: $role")

                            if (role == "client") {
                                val intent = Intent(this@MainActivity, WelcomeActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else if (role == "provider") {
                                val intent = Intent(this@MainActivity, WelcomeActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this@MainActivity, "Unknown role", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@MainActivity, "Invalid Password", Toast.LENGTH_SHORT).show()
                        }
                        break
                    }
                }

                if (!isUserFound) {
                    Toast.makeText(this@MainActivity, "User not found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

}
