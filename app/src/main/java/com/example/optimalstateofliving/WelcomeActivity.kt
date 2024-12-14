package com.example.optimalstateofliving

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.optimalstateofliving.databinding.ActivityCreateAccountBinding

class WelcomeActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.welcome_screen)

        val takeAssesmentButton = findViewById<Button>(R.id.takeAssessmentButton)
        val viewExercisesButton = findViewById<Button>(R.id.viewExercisesButton)
        val viewHistoryButton = findViewById<Button>(R.id.viewHistoryButton)
        val settingsButton = findViewById<Button>(R.id.settingsButton)


        takeAssesmentButton.setOnClickListener {
            val intent = Intent(this, SelectFeelingsActivity::class.java)
            startActivity(intent) // move to the next screen
        }
        viewExercisesButton.setOnClickListener {
            val intent = Intent(this, ExercisesActivity::class.java)
            startActivity(intent) // move to the next screen
        }
        viewHistoryButton.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent) // move to the next screen
        }
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent) // move to the next screen
        }
    }
}