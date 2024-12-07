package com.example.optimalstateofliving

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class CreateAccountActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateAccountBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        setupSpinners()

        binding.btnsingup.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val pass = binding.passwordEditText.text.toString()
            val Fname = binding.firstNameEditText.text.toString()
            val Lname = binding.lastNameEditText.text.toString()

            //الحصول على التاريخ
                val selectedDay = binding.spinnerDay.selectedItem.toString()
                val selectedMonth = binding.spinnerMonth.selectedItem.toString()
                val selectedYear = binding.spinnerYear.selectedItem.toString()
            val selectedOptionId = binding.rgOptions.checkedRadioButtonId

            // التحقق من الخيار المحدد
            val selectedOption = when (selectedOptionId) {
                binding.rbYes.id -> "Yes"
                binding.rbNo.id -> "No"
                else -> "No option selected"}

            if (email.isNotEmpty() && pass.isNotEmpty() ) {


                    firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                        if (it.isSuccessful) {
                            val intent = Intent(this, WelcomeActivity::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()

                        }
                    }

            } else {
                Toast.makeText(this, "Empty Fields Are not Allowed !!", Toast.LENGTH_SHORT).show()

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
}