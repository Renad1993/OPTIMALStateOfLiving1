package com.example.optimalstateofliving

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
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

import java.util.*
class ClientDetailActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter
    private val historyList = mutableListOf<HistoryItem>()
    private lateinit var daySpinner: Spinner
    private lateinit var monthSpinner: Spinner
    private lateinit var yearSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_detail)

        // Initialize UI components
        try {
            recyclerView = findViewById(R.id.recyclerView)
            daySpinner = findViewById(R.id.daySpinner)
            monthSpinner = findViewById(R.id.monthSpinner)
            yearSpinner = findViewById(R.id.yearSpinner)

            recyclerView.layoutManager = LinearLayoutManager(this)
            historyAdapter = HistoryAdapter(historyList)
            recyclerView.adapter = historyAdapter

            database = FirebaseDatabase.getInstance().getReference("_user")

            val clientEmail = intent.getStringExtra("clientEmail")
            if (clientEmail.isNullOrEmpty()) {
                Toast.makeText(this, "Invalid client email", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            fetchAvailableDatesForClient(clientEmail)

            val listener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    loadHistoryForSelectedDate(clientEmail)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

            daySpinner.onItemSelectedListener = listener
            monthSpinner.onItemSelectedListener = listener
            yearSpinner.onItemSelectedListener = listener
        } catch (e: Exception) {
            Log.e("ClientDetailActivity", "Error initializing components", e)
            Toast.makeText(this, "Initialization error: ${e.message}", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun fetchAvailableDatesForClient(email: String) {
        database.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (clientSnapshot in snapshot.children) {
                        val userId = clientSnapshot.key // Get the userId for the matched email
                        if (userId != null) {
                            fetchAvailableDates(userId)
                            break // Assuming we need only the first matched user
                        }
                    }
                } else {
                    Toast.makeText(this@ClientDetailActivity, "No client found with the provided email", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ClientDetailActivity, "Failed to fetch client details: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchAvailableDates(userId: String) {
        database.child(userId).child("History").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val dayList = mutableListOf<String>()
                    val monthList = mutableListOf<String>()
                    val yearList = mutableListOf<String>()

                    for (entry in snapshot.children) {
                        val date = entry.key ?: continue // Get the date as a key
                        val dateParts = date.split("-")
                        if (dateParts.size == 3) {
                            yearList.add(dateParts[0])
                            monthList.add(getMonthName(dateParts[1]))
                            dayList.add(dateParts[2])
                        }
                    }

                    // Populate Spinners with distinct and sorted values
                    setupDateSpinner(daySpinner, dayList.distinct().sorted())
                    setupDateSpinner(monthSpinner, monthList.distinct())
                    setupDateSpinner(yearSpinner, yearList.distinct().sorted())
                } else {
                    Toast.makeText(this@ClientDetailActivity, "No history found for the client", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ClientDetailActivity, "Failed to load history: ${error.message}", Toast.LENGTH_SHORT).show()
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
                historyList.clear() // مسح القائمة القديمة
                if (snapshot.exists()) {
                    for (timeEntry in snapshot.children) {
                        val time = timeEntry.key ?: continue // الحصول على الوقت
                        val status = timeEntry.child("status").getValue(Int::class.java) ?: -1
                        val colorName = getColorName(status)
                        historyList.add(HistoryItem(time, colorName, status))
                    }
                    historyAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this@ClientDetailActivity, "No data found for selected date", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ClientDetailActivity, "Failed to load history: ${error.message}", Toast.LENGTH_SHORT).show()
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


/*
class ClientDetailActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter
    private val historyList = mutableListOf<HistoryItem>()
    private lateinit var daySpinner: Spinner
    private lateinit var monthSpinner: Spinner
    private lateinit var yearSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_detail)

        // Initialize UI components
        recyclerView = findViewById(R.id.recyclerView)
        daySpinner = findViewById(R.id.daySpinner)
        monthSpinner = findViewById(R.id.monthSpinner)
        yearSpinner = findViewById(R.id.yearSpinner)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        historyAdapter = HistoryAdapter(historyList)
        recyclerView.adapter = historyAdapter

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().getReference("_user")

        // Retrieve client email from Intent
        val clientEmail = intent.getStringExtra("clientEmail")
        if (!clientEmail.isNullOrEmpty()) {
            fetchAvailableDates(clientEmail)
        } else {
            Toast.makeText(this, "Invalid client email", Toast.LENGTH_SHORT).show()
        }

        // Set up spinners to load history on selection
        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (!clientEmail.isNullOrEmpty()) {
                    loadHistoryForSelectedDate(clientEmail)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        daySpinner.onItemSelectedListener = listener
        monthSpinner.onItemSelectedListener = listener
        yearSpinner.onItemSelectedListener = listener
    }

    private fun fetchAvailableDates(clientEmail: String) {
        database.child(clientEmail).child("History").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val dayList = mutableListOf<String>()
                    val monthList = mutableListOf<String>()
                    val yearList = mutableListOf<String>()

                    for (entry in snapshot.children) {
                        val date = entry.key ?: continue
                        val dateParts = date.split("-")
                        if (dateParts.size == 3) {
                            yearList.add(dateParts[0])
                            monthList.add(getMonthName(dateParts[1]))
                            dayList.add(dateParts[2])
                        }
                    }

                    setupDateSpinner(daySpinner, dayList.distinct().sorted())
                    setupDateSpinner(monthSpinner, monthList.distinct())
                    setupDateSpinner(yearSpinner, yearList.distinct().sorted())
                } else {
                    Toast.makeText(this@ClientDetailActivity, "No history found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ClientDetailActivity, "Failed to load dates: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadHistoryForSelectedDate(clientEmail: String) {
        val selectedDay = daySpinner.selectedItem?.toString()
        val selectedMonth = monthSpinner.selectedItem?.toString()
        val selectedYear = yearSpinner.selectedItem?.toString()

        if (!selectedDay.isNullOrEmpty() && !selectedMonth.isNullOrEmpty() && !selectedYear.isNullOrEmpty()) {
            val selectedDate = "$selectedYear-${getMonthNumber(selectedMonth)}-$selectedDay"
            fetchHistoryByDate(clientEmail, selectedDate)
        }
    }

    private fun fetchHistoryByDate(clientEmail: String, date: String) {
        database.child(clientEmail).child("History").child(date).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                historyList.clear()
                if (snapshot.exists()) {
                    for (timeEntry in snapshot.children) {
                        val time = timeEntry.key ?: continue
                        val status = timeEntry.child("status").getValue(Int::class.java) ?: -1
                        val colorName = getColorName(status)
                        historyList.add(HistoryItem(time, colorName, status))
                    }
                    historyAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this@ClientDetailActivity, "No data found for selected date", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ClientDetailActivity, "Failed to load history: ${error.message}", Toast.LENGTH_SHORT).show()
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

    private fun getColorName(status: Int): String {
        return when (status) {
            Color.RED -> "Red"
            Color.BLUE -> "Blue"
            Color.YELLOW -> "Yellow"
            Color.WHITE -> "White"
            else -> {
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
*/
