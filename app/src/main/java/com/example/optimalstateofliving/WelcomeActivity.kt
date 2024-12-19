package com.example.optimalstateofliving

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.optimalstateofliving.databinding.ActivityCreateAccountBinding

class WelcomeActivity: AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.welcome_screen)

        val takeAssesmentButton = findViewById<Button>(R.id.takeAssessmentButton)
        val viewExercisesButton = findViewById<Button>(R.id.viewExercisesButton)
        val viewHistoryButton = findViewById<Button>(R.id.viewHistoryButton)
        val settingsButton = findViewById<Button>(R.id.settingsButton)

        //get current user information
        val firstName = intent.getStringExtra("FirstName")
        val lastName = intent.getStringExtra("LastName")
        val role = intent.getStringExtra("Role")
        val welcomeTextView = findViewById<TextView>(R.id.welcomeTextView)
        welcomeTextView.text = "Welcome $firstName $lastName"



        takeAssesmentButton.setOnClickListener {
            val intent = Intent(this, SelectFeelingsActivity::class.java)
            startActivity(intent)

        }
        viewExercisesButton.setOnClickListener {
            val intent = Intent(this, ExercisesActivity::class.java)
            startActivity(intent)

        }
        viewHistoryButton.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)

        }
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)

        }
    }
}