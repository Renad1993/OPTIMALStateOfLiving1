package com.example.optimalstateofliving

import android.R.attr.text
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SelectFeelingsActivity : AppCompatActivity() {
    private var selectedColor: Int? = null
    private lateinit var databaseRef: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.feelings)

        databaseRef = FirebaseDatabase.getInstance().getReference("_user")
        val centerBubble = findViewById<CardView>(R.id.center_bubble)
        val topBubble = findViewById<CardView>(R.id.top_bubble)
        val rightBubble = findViewById<CardView>(R.id.right_bubble)
        val leftBubble = findViewById<CardView>(R.id.left_bubble)
        val submitButton = findViewById<Button>(R.id.button2)
        val backButton = findViewById<Button>(R.id.button)
        val editText: EditText = findViewById(R.id.editTextTextMultiLine2)

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
            val centerText = (centerBubble.getChildAt(0) as TextView).text.toString()
            editText.setText(centerText)
            selectedColor = centerBubble.backgroundTintList?.defaultColor
        }

        topBubble.setOnClickListener {
            animateBubble(topBubble)
            Toast.makeText(this, "You clicked the Top Bubble!", Toast.LENGTH_SHORT).show()
            val topText = (topBubble.getChildAt(0) as TextView).text.toString()
            editText.setText(topText)
            selectedColor = topBubble.backgroundTintList?.defaultColor
        }

        rightBubble.setOnClickListener {
            animateBubble(rightBubble)
            Toast.makeText(this, "You clicked the Right Bubble!", Toast.LENGTH_SHORT).show()
            val rightText = (rightBubble.getChildAt(0) as TextView).text.toString()
            editText.setText(rightText)
            selectedColor = rightBubble.backgroundTintList?.defaultColor
        }

        leftBubble.setOnClickListener {
            animateBubble(leftBubble)
            Toast.makeText(this, "You clicked the Left Bubble!", Toast.LENGTH_SHORT).show()
            val leftText = (leftBubble.getChildAt(0) as TextView).text.toString()
            editText.setText(leftText)
            selectedColor = leftBubble.backgroundTintList?.defaultColor

        }

        submitButton.setOnClickListener {
            val intent = Intent(this, ExercisesActivity::class.java)
            if (selectedColor != null) {
                saveDataToFirebase(selectedColor!!)
            } else {
                Log.e("Submit", "no circle selected.")
            }
        }
        backButton.setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent) // move to the next screen
        }

        //startActivity(intent) // move to the next screen
        }

    private fun saveDataToFirebase(color: Int) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        Log.d("FirebaseDebug", "Current User ID: $userId")
        if (userId == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        databaseRef.child(userId).get().addOnSuccessListener { snapshot ->
            Log.d("FirebaseDebug", "Snapshot exists: ${snapshot.exists()}")
            if (snapshot.exists()) {
                Log.d("FirebaseDebug", "Snapshot data: ${snapshot.value}")
                if (snapshot.hasChild("role")) {
                    val role = snapshot.child("role").value.toString().lowercase()
                    Log.d("FirebaseDebug", "Role found: $role")
                    if (role == "client") {
                        val calendar = Calendar.getInstance()
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        val currentDateTime = dateFormat.format(calendar.time)

                        val data = mapOf(
                            "color" to color,
                            "datetime" to currentDateTime
                        )

                        databaseRef.child(userId).updateChildren(data)
                            .addOnSuccessListener {
                                Log.d("FirebaseDebug", "Data stored successfully.")
                                Toast.makeText(this, "Data stored successfully.", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Log.e("Firebase", "Error storing data: ${e.message}")
                            }
                    } else {
                        Log.e("FirebaseDebug", "User is not a client")
                        Toast.makeText(this, "Only clients can submit data.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("FirebaseDebug", "Role field is missing.")
                    Toast.makeText(this, "Role field is missing in the database.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.e("FirebaseDebug", "User data not found.")
                Toast.makeText(this, "User data not found in the database.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            Log.e("FirebaseDebug", "Error fetching user data: ${e.message}")
            Toast.makeText(this, "Error connecting to the database. Try again.", Toast.LENGTH_SHORT).show()
        }
    }





}

