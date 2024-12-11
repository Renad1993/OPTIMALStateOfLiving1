package com.example.optimalstateofliving

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.optimalstateofliving.databinding.ActivityCreateAccountBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar

class CreateAccountActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateAccountBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        setupSpinners()

        binding.btnsingup.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val pass = binding.passwordEditText.text.toString().trim()
            val firstName = binding.firstNameEditText.text.toString().trim()
            val lastName = binding.lastNameEditText.text.toString().trim()

            // الحصول على التاريخ
            val selectedDay = binding.spinnerDay.selectedItem.toString()
            val selectedMonth = binding.spinnerMonth.selectedItem.toString()
            val selectedYear = binding.spinnerYear.selectedItem.toString()
            val dob = "$selectedDay/$selectedMonth/$selectedYear"

            // التحقق من الخيار المحدد
            val selectedOptionId = binding.rgOptions.checkedRadioButtonId
            val provider = when (selectedOptionId) {
                binding.rbYes.id -> "Yes"
                binding.rbNo.id -> "No"
                else -> "No option selected"
            }

            if (email.isNotEmpty() && pass.isNotEmpty() && firstName.isNotEmpty() && lastName.isNotEmpty()) {

                firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                    if (it.isSuccessful) {
                        // إضافة البيانات إلى قاعدة بيانات Firebase
                        val userId = firebaseAuth.currentUser?.uid
                        val databaseRef = FirebaseDatabase.getInstance().getReference("_user")

                        val user = mapOf(
                            "DOB" to dob,
                            "FirstName" to firstName,
                            "LastName" to lastName,
                            "email" to email,
                            "password" to pass,
                            "role" to if (provider == "Yes") "provider" else "client"
                        )

                        if (userId != null) {
                            databaseRef.child(userId).setValue(user).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this, WelcomeActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Toast.makeText(this, "Failed to save user data: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(this, "Failed to get user ID", Toast.LENGTH_SHORT).show()
                        }

                    } else {
                        Toast.makeText(this, it.exception?.message ?: "Failed to create account", Toast.LENGTH_SHORT).show()
                    }
                }

            } else {
                Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSpinners() {
        // إعداد الأيام (1-31)
        val days = (1..31).map { it.toString() }
        val dayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, days)
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDay.adapter = dayAdapter

        // إعداد الأشهر
        val months = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        val monthAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMonth.adapter = monthAdapter

        // إعداد السنوات (1900 - السنة الحالية)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (1900..currentYear).map { it.toString() }
        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerYear.adapter = yearAdapter
    }
}
