package com.example.optimalstateofliving

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ProviderActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvProviderName: TextView
    private lateinit var imgAddIcon: ImageView

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_provider)

        // ربط العناصر
        tvProviderName = findViewById(R.id.tvProviderName)
        recyclerView = findViewById(R.id.recyclerView)
        imgAddIcon = findViewById(R.id.imgAddIcon)

        val firstName = intent.getStringExtra("FirstName")
        val lastName = intent.getStringExtra("LastName")
        val role = intent.getStringExtra("Role")
        // إعداد اسم المزود
        tvProviderName.text ="Welcome $firstName $lastName"


        // بيانات الاختبار
        val items = mutableListOf("History Item 1", "History Item 2", "History Item 3")

        // إعداد RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ProviderItemAdapter(
            items,
            onDeleteClick = { item ->
                items.remove(item)
                recyclerView.adapter?.notifyDataSetChanged()
            },
            onItemClick = { item ->
                // الانتقال إلى صفحة التاريخ عند الضغط
                val intent = Intent(this, HistoryActivity::class.java)
                //intent.putExtra("itemName", item)
                startActivity(intent)

            }
        )

        // عند الضغط على أيقونة الإضافة
        imgAddIcon.setOnClickListener {
            // يمكنك تنفيذ الإجراء المطلوب هنا (إضافة عنصر جديد مثلاً)
        }
    }

}
