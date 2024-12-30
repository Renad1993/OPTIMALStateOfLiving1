package com.example.optimalstateofliving

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.optimalstateofliving.databinding.ActivityCreateAccountBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.Calendar

class CreateAccountActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateAccountBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance().getReference("_user")
        setupSpinners()

        binding.btnsingup.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val pass = binding.passwordEditText.text.toString().trim()
            val firstName = binding.firstNameEditText.text.toString().trim()
            val lastName = binding.lastNameEditText.text.toString().trim()

            val selectedDay = binding.spinnerDay.selectedItem.toString()
            val selectedMonth = binding.spinnerMonth.selectedItem.toString()
            val selectedYear = binding.spinnerYear.selectedItem.toString()
            val dob = "$selectedDay/$selectedMonth/$selectedYear"

            val selectedOptionId = binding.rgOptions.checkedRadioButtonId
            val provider = when (selectedOptionId) {
                binding.rbYes.id -> "Yes"
                binding.rbNo.id -> "No"
                else -> "No option selected"
            }

            if (email.isNotEmpty() && pass.isNotEmpty() && firstName.isNotEmpty() && lastName.isNotEmpty()) {
                checkIfUsernameExists(firstName, lastName) { isUnique ->
                    if (isUnique) {
                        createAccount(firstName, lastName, email, pass, dob, provider)
                    } else {
                        Toast.makeText(this, "Username already exists. Please choose another.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Check if the combination of FirstName and LastName already exists
    private fun checkIfUsernameExists(firstName: String, lastName: String, callback: (Boolean) -> Unit) {
        databaseRef.orderByChild("FirstName").equalTo(firstName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (userSnapshot in snapshot.children) {
                            val dbLastName = userSnapshot.child("LastName").getValue(String::class.java)
                            if (dbLastName == lastName) {
                                callback(false)
                                return
                            }
                        }
                    }
                    callback(true)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@CreateAccountActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    callback(false)
                }
            })
    }

    // Create a new account in Firebase
    private fun createAccount(firstName: String, lastName: String, email: String, password: String, dob: String, provider: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = firebaseAuth.currentUser?.uid
                    if (userId != null) {
                        val user = mapOf(
                            "FirstName" to firstName,
                            "LastName" to lastName,
                            "email" to email,
                            "DOB" to dob,
                            "role" to if (provider == "Yes") "provider" else "client"
                        )
                        databaseRef.child(userId).setValue(user).addOnCompleteListener { saveTask ->
                            if (saveTask.isSuccessful) {
                                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this, "Failed to save user data: ${saveTask.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, task.exception?.message ?: "Failed to create account", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun setupSpinners() {
        val days = (1..31).map { it.toString() }
        val dayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, days)
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDay.adapter = dayAdapter

        val months = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        val monthAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMonth.adapter = monthAdapter

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (1900..currentYear).map { it.toString() }
        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerYear.adapter = yearAdapter
    }
}
