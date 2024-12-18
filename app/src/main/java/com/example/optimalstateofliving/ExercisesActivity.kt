package com.example.optimalstateofliving

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.play.core.integrity.x
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
    private lateinit var webView: WebView

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
            databaseRef.child(userId).child("History").orderByKey().limitToLast(1)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            // استخراج العنصر الأخير
                            for (entry in snapshot.children) {
                                // قراءة الحقول من العنصر الأخير
                                val rawStatus = entry.child("status").getValue(Int::class.java) ?: -1
                                 colorName = getColorName(rawStatus)
                                Toast.makeText(
                                    baseContext,
                                    colorName,
                                    Toast.LENGTH_SHORT
                                ).show()

                            }
                        } else {
                            println("No data found in History.")
                        }

                    }

                    override fun onCancelled(error: DatabaseError) {
                        println("Error: ${error.message}")
                    }
                })

        }



        breathingExerciseButton.setOnClickListener {
            // جلب بيانات المستخدم من قاعدة البيانات
            databaseRef = FirebaseDatabase.getInstance()
                .getReference("_breathingExercises")
            var x = getValueBasedOnColor(colorName)

            var exerciseKey = "exercise$x" // تحديد المفتاح المطلوب (exercise1, exercise2, ...)

            // استدعاء الوظيفة لقراءة البيانات
            readExercise(exerciseKey,x)
        }

        otherExercisesButton.setOnClickListener {


            // جلب بيانات المستخدم من قاعدة البيانات
            databaseRef =
                FirebaseDatabase.getInstance().getReference("_OtherExercises")
            // استدعاء الوظيفة لقراءة البيانات
            var x = getValueBasedOnColor(colorName)

            var exerciseKey = "exercise$x"
            readExercise(exerciseKey,x)


        }
        recommendedFoodsButton.setOnClickListener {


            // جلب بيانات المستخدم من قاعدة البيانات
            databaseRef =
                FirebaseDatabase.getInstance().getReference("_RecommendedFoods")
            // استدعاء الوظيفة لقراءة البيانات
            var x = getValueBasedOnColor(colorName)

            var FoodKey = "food$x"
            readExercise(FoodKey,x)

        }
        watchVideoButton.setOnClickListener {

            // جلب بيانات المستخدم من قاعدة البيانات
            databaseRef = FirebaseDatabase.getInstance()
                .getReference("_Vedio")
            var x = getValueBasedOnColor(colorName)

            var vedioKey = "vedio$x" // تحديد المفتاح المطلوب (vedio1, vedio2, ...)

            // استدعاء الوظيفة لقراءة البيانات
            readVedio(vedioKey,x)

        }

    }


    private fun showCustomDialog(
        titel: String,
        steps: String,
        description: String,
        x: Int
    ) {
        // إنشاء Dialog
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_custom)

        val dialogLayout = dialog.findViewById<LinearLayout>(R.id.dialoglay)

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
        // ضبط البيانات داخل الـ Dialog
        val tvData1 = dialog.findViewById<TextView>(R.id.tvData1)
        val tvData2 = dialog.findViewById<TextView>(R.id.tvData2)
        val tvData3 = dialog.findViewById<TextView>(R.id.tvData3)
        tvData1.text = titel
        tvData2.text = steps
        tvData3.text = description

        // زر الإغلاق
        val btnClose = dialog.findViewById<TextView>(R.id.btnClose)
        btnClose.setOnClickListener {
            dialog.dismiss() // يغلق الـ Dialog
        }

        // عرض الـ Dialog
        dialog.show()
        dialog.setCancelable(false)
    }

    private fun readExercise(exerciseKey: String,x :Int) {

        databaseRef.child(exerciseKey).addListenerForSingleValueEvent(object :
            ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // استرداد البيانات كـ Map أو كائن
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


                            //عرض البيانات
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


    fun getValueBasedOnColor(colorName: String): Int {
        return when (colorName) {
            "Red" -> 3
            "Blue" -> 4
            "Yellow" -> 1
            "White" -> 2
            else -> 1 // القيمة الافتراضية
        }
    }
    private fun getColorName(status: Int): String {
        return when (status) {
            Color.RED -> "Red"
            Color.BLUE -> "Blue"
            Color.YELLOW -> "Yellow"
            Color.WHITE -> "White"
            else -> {
                // إذا كانت الألوان مخصصة
                when (status) {
                    Color.parseColor("#22177A") -> "Blue" // لون أحمر مخصص
                    Color.parseColor("#C80036") -> "Red" // لون أزرق مخصص
                    Color.parseColor("#FCC737") -> "Gold" // لون أصفر مخصص
                    else -> "Unknown"
                }
            }
        }
    }
}