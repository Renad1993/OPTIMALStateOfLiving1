package com.example.optimalstateofliving

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class SelectFeelingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.feelings)


        val centerBubble = findViewById<CardView>(R.id.center_bubble)
        val topBubble = findViewById<CardView>(R.id.top_bubble)
        val rightBubble = findViewById<CardView>(R.id.right_bubble)
        val leftBubble = findViewById<CardView>(R.id.left_bubble)
        val submitButton = findViewById<Button>(R.id.button2)
        val backButton = findViewById<Button>(R.id.button)

        // function to create the Animation
        fun animateBubble(bubble: CardView) {
            val scaleUpX = ObjectAnimator.ofFloat(bubble, "scaleX", 1.0f, 1.5f, 1.0f)
            val scaleUpY = ObjectAnimator.ofFloat(bubble, "scaleY", 1.0f, 1.5f, 1.0f)
            val rotation = ObjectAnimator.ofFloat(bubble, "rotation", 0f, 360f)

            val animatorSet = AnimatorSet()
            animatorSet.playTogether(scaleUpX, scaleUpY, rotation)
            animatorSet.duration = 600
            animatorSet.start()
        }

        // Setting events when you click on Circles
        centerBubble.setOnClickListener {
            animateBubble(centerBubble)
            Toast.makeText(this, "You clicked the Center Bubble!", Toast.LENGTH_SHORT).show()
        }

        topBubble.setOnClickListener {
            animateBubble(topBubble)
            Toast.makeText(this, "You clicked the Top Bubble!", Toast.LENGTH_SHORT).show()
        }

        rightBubble.setOnClickListener {
            animateBubble(rightBubble)
            Toast.makeText(this, "You clicked the Right Bubble!", Toast.LENGTH_SHORT).show()
        }

        leftBubble.setOnClickListener {
            animateBubble(leftBubble)
            Toast.makeText(this, "You clicked the Left Bubble!", Toast.LENGTH_SHORT).show()
        }

        submitButton.setOnClickListener {
            val intent = Intent(this, ExercisesActivity::class.java)
            startActivity(intent) // move to the next screen
        }

        backButton.setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent) // move to the next screen
        }

    }
}
