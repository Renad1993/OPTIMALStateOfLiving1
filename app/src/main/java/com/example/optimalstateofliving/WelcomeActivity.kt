package com.example.optimalstateofliving

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.welcome_screen)
        val takeAssesmentButton = findViewById<Button>(R.id.takeAssessmentButton)
        takeAssesmentButton.setOnClickListener {
            val intent = Intent(this, SelectFeelingsActivity::class.java)
            startActivity(intent) // move to the next screen
        }
    }
}