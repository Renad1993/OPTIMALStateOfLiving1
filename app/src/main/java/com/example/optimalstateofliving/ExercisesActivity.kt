package com.example.optimalstateofliving


import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

class ExercisesActivity: AppCompatActivity() {

    private lateinit var databaseRef: DatabaseReference
    private lateinit var breathingExerciseButton: Button
    private lateinit var otherExercisesButton: Button
    private lateinit var recommendedFoodsButton: Button
    private lateinit var watchVideoButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.excersises)
        breathingExerciseButton = findViewById(R.id.breathingExerciseButton)
        otherExercisesButton = findViewById(R.id.otherExercisesButton)
        recommendedFoodsButton = findViewById(R.id.recommendedFoodsButton)
        watchVideoButton = findViewById(R.id.watchVideoButton)



        var colorName=""

        databaseRef = FirebaseDatabase.getInstance().getReference("_user")
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null)
        {

            val userId = currentUser.uid
            //Bring the final user status in Firebase
            databaseRef.child(userId).child("History").orderByKey().limitToLast(1)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            // الخطوة 1: استخراج آخر تاريخ (العقدة الأخيرة)
                            val lastDateSnapshot = snapshot.children.lastOrNull()
                            if (lastDateSnapshot != null) {
                                val lastDate = lastDateSnapshot.key // اسم العقدة (مثلاً 2024-12-19)

                                // الخطوة 2: استخراج آخر وقت ضمن هذا التاريخ
                                val lastTimeSnapshot = lastDateSnapshot.children.lastOrNull()
                                if (lastTimeSnapshot != null) {
                                    val lastTime = lastTimeSnapshot.key // اسم العقدة (مثلاً 11:30 PM)

                                    // الخطوة 3: استخراج الحالة المرتبطة بهذا الوقت
                                    val rawStatus = lastTimeSnapshot.child("status").getValue(Int::class.java) ?: -1
                                    colorName = getColorName(rawStatus)

                                    // طباعة النتيجة (للتجربة)
                                    Toast.makeText(
                                        baseContext,
                                        "Last Status on $lastDate at $lastTime: $colorName",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    // يمكنك الآن استخدام `colorName` في باقي منطق الكود
                                } else {
                                    Toast.makeText(
                                        baseContext,
                                        "No times found for the latest date.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                Toast.makeText(
                                    baseContext,
                                    "No dates found in History.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(baseContext, "History is empty.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        println("Error: ${error.message}")
                    }
                })

        }



        breathingExerciseButton.setOnClickListener {

            databaseRef = FirebaseDatabase.getInstance()
                .getReference("_breathingExercises")
            var x = getValueBasedOnColor(colorName)

            var exerciseKey = "exercise$x" //  (exercise1, exercise2, ...)

            readExercise(exerciseKey,x)
        }

        otherExercisesButton.setOnClickListener {


            // Bringing user data from the database
            databaseRef =
                FirebaseDatabase.getInstance().getReference("_OtherExercises")
            //  Recall function to read data
            var x = getValueBasedOnColor(colorName)

            var exerciseKey = "exercise$x"
            readExercise(exerciseKey,x)


        }
        recommendedFoodsButton.setOnClickListener {


            // Bringing user data from the database
            databaseRef =
                FirebaseDatabase.getInstance().getReference("_RecommendedFoods")
            // Recall function to read data
            var x = getValueBasedOnColor(colorName)

            var FoodKey = "food$x"
            readExercise(FoodKey,x)

        }
        watchVideoButton.setOnClickListener {

            databaseRef = FirebaseDatabase.getInstance()
                .getReference("_Vedio")
            var x = getValueBasedOnColor(colorName)

            var vedioKey = "vedio$x" // Select the required key (vedio1, vedio2, ...)

            // Recall function to read data
            readVedio(vedioKey,x)

        }

    }


    private fun showCustomDialog(
        titel: String,
        steps: String,
        description: String,
        x: Int
    ) {
        // create dialog
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_custom)

        val dialogLayout = dialog.findViewById<LinearLayout>(R.id.dialoglay)
        //background base on color status
        if(x==1) {
            dialogLayout.background =
                ContextCompat.getDrawable(this, R.drawable.img_1)
        }
        if(x==2) {
            dialogLayout.background =
                ContextCompat.getDrawable(this, R.drawable.img_2)
        }
        if(x==3) {
            dialogLayout.background =
                ContextCompat.getDrawable(this, R.drawable.img_3)
        }
        if(x==4) {
            dialogLayout.background =
                ContextCompat.getDrawable(this, R.drawable.img_4)
        }
        // Adjust data within the Dialog
        val tvData1 = dialog.findViewById<TextView>(R.id.tvData1)
        val tvData2 = dialog.findViewById<TextView>(R.id.tvData2)
        val tvData3 = dialog.findViewById<TextView>(R.id.tvData3)
        tvData1.text = titel
        tvData2.text = steps
        tvData3.text = description

        val btnClose = dialog.findViewById<TextView>(R.id.btnClose)
        btnClose.setOnClickListener {
            dialog.dismiss() // Dialog closes
        }


        dialog.show()
        dialog.setCancelable(false)
    }

    //function to read exercise base on user status
    private fun readExercise(exerciseKey: String,x :Int) {

        databaseRef.child(exerciseKey).addListenerForSingleValueEvent(object :
            ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val exercise = snapshot.getValue<Map<String, String>>()
                        if (exercise != null) {
                            val name = exercise["title"]
                            val description = exercise["steps"]
                            val duration = exercise["description"]
                            if (name != null) {
                                if (description != null) {
                                    if (duration != null) {
                                        showCustomDialog(
                                            name,
                                            description,
                                            duration,
                                            x
                                        )
                                    }
                                }
                            }

                            //display data
                            Toast.makeText(
                                baseContext,
                                "Name: $name\nDescription: $description\nDuration: $duration",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            baseContext,
                            "Exercise not found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    baseContext,
                    "Error: ${error.message}",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
            })
    }



    //function to read vedio base on user status
    private fun readVedio(vedioKey: String, x: Int) {

        databaseRef.child(vedioKey).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val vedio = snapshot.getValue<Map<String, String>>()
                    if (vedio != null) {
                        val videoUrl = vedio["url"]
                        if (!videoUrl.isNullOrEmpty()) {
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data = Uri.parse(videoUrl)
                            startActivity(intent)

                        } else {
                            Toast.makeText(baseContext, "Video URL is empty", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(baseContext, "Video data is null", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(baseContext, "Video not found for key: $vedioKey", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    baseContext,
                    "Error reading database: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }



    //map each color to specific number
    fun getValueBasedOnColor(colorName: String): Int {
        return when (colorName) {
            "Red" -> 3
            "Blue" -> 4
            "Yellow" -> 1
            "White" -> 2
            else -> 1
        }
    }

    //map color status stored in database to the color name
    private fun getColorName(status: Int): String {
        return when (status) {
            Color.RED -> "Red"
            Color.BLUE -> "Blue"
            Color.YELLOW -> "Yellow"
            Color.WHITE -> "White"
            else -> {
                // If the colors are customized
                when (status) {
                    Color.parseColor("#22177A") -> "Blue"
                    Color.parseColor("#C80036") -> "Red"
                    Color.parseColor("#FCC737") -> "Gold"
                    else -> "Unknown"
                }
            }
        }
    }


}