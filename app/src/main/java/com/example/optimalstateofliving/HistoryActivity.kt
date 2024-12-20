package com.example.optimalstateofliving

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.optimalstateofliving.models.HistoryItem
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
        recyclerView = findViewById(R.id.recyclerView)
        daySpinner = findViewById(R.id.daySpinner)
        monthSpinner = findViewById(R.id.monthSpinner)
        yearSpinner = findViewById(R.id.yearSpinner)

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        historyAdapter = HistoryAdapter(historyList)
        recyclerView.adapter = historyAdapter

        // Linking the database
        database = FirebaseDatabase.getInstance().getReference("_user")

        // Bring your current user's UID
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserUid != null) {
            fetchAvailableDates(currentUserUid)
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }


        backButton.setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
            finish()
        }

        // Monitor Spinners changes to download data automatically
        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                loadHistoryForSelectedDate(currentUserUid)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        daySpinner.onItemSelectedListener = listener
        monthSpinner.onItemSelectedListener = listener
        yearSpinner.onItemSelectedListener = listener
    }

    private fun fetchAvailableDates(userId: String) {
        database.child(userId).child("History").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val dayList = mutableListOf<String>()
                    val monthList = mutableListOf<String>()
                    val yearList = mutableListOf<String>()

                    for (entry in snapshot.children) {
                        val date = entry.key ?: continue // Getting history as a node
                        val dateParts = date.split("-")
                        if (dateParts.size == 3) {
                            yearList.add(dateParts[0])
                            monthList.add(getMonthName(dateParts[1]))
                            dayList.add(dateParts[2])
                        }
                    }

                    // Setting up Spinners
                    setupDateSpinner(daySpinner, dayList.distinct().sorted())
                    setupDateSpinner(monthSpinner, monthList.distinct())
                    setupDateSpinner(yearSpinner, yearList.distinct().sorted())
                } else {
                    Toast.makeText(this@HistoryActivity, "No history found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HistoryActivity, "Failed to load dates: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadHistoryForSelectedDate(userId: String?) {
        if (userId == null) return

        val selectedDay = daySpinner.selectedItem?.toString()
        val selectedMonth = monthSpinner.selectedItem?.toString()
        val selectedYear = yearSpinner.selectedItem?.toString()

        if (!selectedDay.isNullOrEmpty() && !selectedMonth.isNullOrEmpty() && !selectedYear.isNullOrEmpty()) {
            val selectedDate = "$selectedYear-${getMonthNumber(selectedMonth)}-$selectedDay"
            fetchHistoryByDate(userId, selectedDate)
        }
    }

    private fun fetchHistoryByDate(userId: String, date: String) {
        database.child(userId).child("History").child(date).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                historyList.clear() // Clear Old List
                if (snapshot.exists()) {
                    val timeFormat24 = SimpleDateFormat("HH:mm", Locale.getDefault()) // صيغة 24 ساعة
                    val timeFormat12 = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    for (timeEntry in snapshot.children) {
                        val time = timeEntry.key ?: continue // get the time
                        val timein24= timeFormat24.parse(time)
                        val timein12=timeFormat12.format(timein24!!)
                        val status = timeEntry.child("status").getValue(Int::class.java) ?: -1
                        val colorName = getColorName(status)
                        historyList.add(HistoryItem(timein12, colorName, status))
                    }
                    historyAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this@HistoryActivity, "No data found for selected date", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HistoryActivity, "Failed to load history: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupDateSpinner(spinner: Spinner, dataList: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, dataList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun getMonthNumber(month: String): String {
        return when (month) {
            "Jan" -> "01"
            "Feb" -> "02"
            "Mar" -> "03"
            "Apr" -> "04"
            "May" -> "05"
            "Jun" -> "06"
            "Jul" -> "07"
            "Aug" -> "08"
            "Sep" -> "09"
            "Oct" -> "10"
            "Nov" -> "11"
            "Dec" -> "12"
            else -> "01"
        }
    }

    private fun getMonthName(monthNumber: String): String {
        return when (monthNumber) {
            "01" -> "Jan"
            "02" -> "Feb"
            "03" -> "Mar"
            "04" -> "Apr"
            "05" -> "May"
            "06" -> "Jun"
            "07" -> "Jul"
            "08" -> "Aug"
            "09" -> "Sep"
            "10" -> "Oct"
            "11" -> "Nov"
            "12" -> "Dec"
            else -> "Jan"
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
