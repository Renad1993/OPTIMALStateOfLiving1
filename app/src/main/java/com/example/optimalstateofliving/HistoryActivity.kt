package com.example.optimalstateofliving

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter
    private val historyList = mutableListOf<HistoryItem>()
    private lateinit var daySpinner: Spinner
    private lateinit var monthSpinner: Spinner
    private lateinit var yearSpinner: Spinner
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        backButton = findViewById(R.id.backButton)
        // ربط RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        historyAdapter = HistoryAdapter(historyList)
        recyclerView.adapter = historyAdapter

        // ربط Spinners
        daySpinner = findViewById(R.id.daySpinner)
        monthSpinner = findViewById(R.id.monthSpinner)
        yearSpinner = findViewById(R.id.yearSpinner)

        // ربط قاعدة البيانات
        database = FirebaseDatabase.getInstance().getReference("_user")

        // جلب UID الخاص بالمستخدم الحالي
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserUid != null) {
            fetchHistoryData(currentUserUid)
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }

        backButton.setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
            finish()
        }

    }

    private fun fetchHistoryData(userId: String) {
        database.child(userId).child("History").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val dayList = mutableListOf<String>()
                    val monthList = mutableListOf<String>()
                    val yearList = mutableListOf<String>()

                    for (entry in snapshot.children) {
                        val rawTime = entry.child("Time").getValue(String::class.java) ?: ""
                        val dateParts = extractDateParts(rawTime)
                        if (dateParts != null) {
                            dayList.add(dateParts.first)
                            monthList.add(dateParts.second)
                            yearList.add(dateParts.third)
                        }

                        // تنسيق الوقت وعرضه في RecyclerView
                        val formattedTime = formatTime(rawTime)
                        val rawStatus = entry.child("status").getValue(Int::class.java) ?: -1
                        val colorName = getColorName(rawStatus)
                        historyList.add(HistoryItem(formattedTime, colorName, rawStatus))
                    }

                    // تحديث RecyclerView
                    historyAdapter.notifyDataSetChanged()

                    // إعداد Spinners لليوم، الشهر، السنة
                    setupDateSpinner(daySpinner, dayList.distinct())
                    setupDateSpinner(monthSpinner, monthList.distinct())
                    setupDateSpinner(yearSpinner, yearList.distinct())
                } else {
                    Toast.makeText(this@HistoryActivity, "No history found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HistoryActivity, "Failed to load history: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun extractDateParts(rawTime: String): Triple<String, String, String>? {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(rawTime)
            val calendar = Calendar.getInstance()
            calendar.time = date ?: return null

            val day = calendar.get(Calendar.DAY_OF_MONTH).toString()
            val month = SimpleDateFormat("MMM", Locale.getDefault()).format(date) // استخراج الشهر كـ Jan, Feb
            val year = calendar.get(Calendar.YEAR).toString()

            Triple(day, month, year)
        } catch (e: Exception) {
            null // في حال حدوث خطأ، لا يتم إرجاع قيمة
        }
    }

    private fun setupDateSpinner(spinner: Spinner, dataList: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, dataList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun formatTime(rawTime: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            val date = inputFormat.parse(rawTime)
            outputFormat.format(date ?: rawTime)
        } catch (e: Exception) {
            rawTime // إذا كان هناك خطأ، ارجع للقيمة الأصلية
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

// نموذج البيانات
data class HistoryItem(val checkTime: String, val statusName: String, val statusColor: Int)
