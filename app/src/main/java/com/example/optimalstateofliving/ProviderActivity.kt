package com.example.optimalstateofliving

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
/*
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
            // إنشاء مربع حوار لإضافة المستخدم
            val dialogView = layoutInflater.inflate(R.layout.dialog_add_user, null)
            val dialog = AlertDialog.Builder(this)
                .setTitle("Add User")
                .setView(dialogView)
                .setCancelable(true)
                .create()

            val etUserEmail = dialogView.findViewById<EditText>(R.id.etUserEmail)
            val btnAddUser = dialogView.findViewById<Button>(R.id.btnAddUser)

            btnAddUser.setOnClickListener {
                val email = etUserEmail.text.toString().trim()

                if (email.isNotEmpty()) {
                    // التحقق من وجود المستخدم في Firebase وإضافته
                    addUserToProvider(email) { success, message ->
                        if (success) {
                            dialog.dismiss()
                            Toast.makeText(this, "User added successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                }
            }

            dialog.show()
        }

    }

    private fun addUserToProvider(email: String, callback: (Boolean, String) -> Unit) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("_user")
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserId.isNullOrEmpty()) {
            Toast.makeText(this, "No logged-in user found", Toast.LENGTH_SHORT).show()
            return
        }

        // البحث عن المستخدم بالبريد الإلكتروني
        databaseRef.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (userSnapshot in snapshot.children) {
                            val userId = userSnapshot.key ?: ""
                            val role = userSnapshot.child("role").getValue(String::class.java)
                            val firstName = userSnapshot.child("FirstName").getValue(String::class.java) ?: ""
                            val lastName = userSnapshot.child("LastName").getValue(String::class.java) ?: ""

                            // التحقق من أن الدور هو "client"
                            if (role == "client") {
                                val entryKey = databaseRef.child(currentUserId).child("MyClient").push().key

                                if (entryKey != null) {
                                    val clientData = mapOf(
                                        "FirstName" to firstName,
                                        "LastName" to lastName,
                                        "email" to email
                                    )

                                    // إضافة البيانات إلى عقدة MyClient
                                    databaseRef.child(currentUserId).child("MyClient").child(entryKey)
                                        .setValue(clientData)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                callback(true, "Client added successfully")
                                            } else {
                                                callback(false, "Failed to add client: ${task.exception?.message}")
                                            }
                                        }
                                } else {
                                    callback(false, "Failed to generate a unique key")
                                }
                            } else {
                                // إذا لم يكن الدور "client"
                                callback(false, "This email does not belong to a client")
                            }

                            return
                        }
                    } else {
                        callback(false, "User not found")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, "Error: ${error.message}")
                }
            })
    }
    }
*/

class ProviderActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvProviderName: TextView
    private lateinit var imgAddIcon: ImageView
    private lateinit var providerAdapter: ProviderItemAdapter
    private val clientList = mutableListOf<String>()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_provider)

        // ربط العناصر من الواجهة
        tvProviderName = findViewById(R.id.tvProviderName)
        recyclerView = findViewById(R.id.recyclerView)
        imgAddIcon = findViewById(R.id.imgAddIcon)

        val firstName = intent.getStringExtra("FirstName")
        val lastName = intent.getStringExtra("LastName")
        tvProviderName.text = "Welcome $firstName $lastName"

        // إعداد RecyclerView
        setupRecyclerView()

        // جلب العملاء المرتبطين بالمزود الحالي
        fetchClientsFromFirebase()

        // التعامل مع الضغط على أيقونة الإضافة
        imgAddIcon.setOnClickListener {
            showAddUserDialog()
        }
    }

    private fun setupRecyclerView() {
        // إعداد RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        providerAdapter = ProviderItemAdapter(
            clientList,
            onDeleteClick = { clientName ->
                removeClientFromFirebase(clientName)
            },
            onItemClick = { clientName ->
                // يمكن تنفيذ إجراءات إضافية عند الضغط على العميل
                Toast.makeText(this, "Selected: $clientName", Toast.LENGTH_SHORT).show()
            }
        )
        recyclerView.adapter = providerAdapter
    }

    private fun fetchClientsFromFirebase() {
        val databaseRef = FirebaseDatabase.getInstance().getReference("_user")
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserId.isNullOrEmpty()) {
            Toast.makeText(this, "No logged-in user found", Toast.LENGTH_SHORT).show()
            return
        }

        // جلب العملاء من Firebase
        databaseRef.child(currentUserId).child("MyClient")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    clientList.clear()
                    for (clientSnapshot in snapshot.children) {
                        val firstName = clientSnapshot.child("FirstName").getValue(String::class.java)
                        val lastName = clientSnapshot.child("LastName").getValue(String::class.java)
                        val email = clientSnapshot.child("email").getValue(String::class.java)
                        if (firstName != null && lastName != null && email != null) {
                            clientList.add("$firstName $lastName ($email)")
                        }
                    }
                    providerAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ProviderActivity, "Failed to load clients: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun removeClientFromFirebase(clientName: String) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("_user")
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserId.isNullOrEmpty()) {
            Toast.makeText(this, "No logged-in user found", Toast.LENGTH_SHORT).show()
            return
        }

        // استخراج البريد الإلكتروني من اسم العميل
        val email = clientName.substringAfterLast("(", "").substringBefore(")")

        if (email.isEmpty()) {
            Toast.makeText(this, "Invalid client data", Toast.LENGTH_SHORT).show()
            return
        }

        databaseRef.child(currentUserId).child("MyClient").orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (childSnapshot in snapshot.children) {
                        childSnapshot.ref.removeValue()
                            .addOnSuccessListener {
                                clientList.remove(clientName)
                                providerAdapter.notifyDataSetChanged()
                                Toast.makeText(this@ProviderActivity, "Client removed", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this@ProviderActivity, "Failed to remove client: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ProviderActivity, "Failed to remove client: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showAddUserDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_user, null)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Add User")
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val etUserEmail = dialogView.findViewById<EditText>(R.id.etUserEmail)
        val btnAddUser = dialogView.findViewById<Button>(R.id.btnAddUser)

        btnAddUser.setOnClickListener {
            val email = etUserEmail.text.toString().trim()

            if (email.isNotEmpty()) {
                addUserToProvider(email) { success, message ->
                    if (success) {
                        dialog.dismiss()

                        Toast.makeText(this, "User added successfully", Toast.LENGTH_SHORT).show()
                        //setupRecyclerView()
                        fetchClientsFromFirebase()
                    } else {
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun addUserToProvider(email: String, callback: (Boolean, String) -> Unit) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("_user")
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserId.isNullOrEmpty()) {
            Toast.makeText(this, "No logged-in user found", Toast.LENGTH_SHORT).show()
            return
        }

        databaseRef.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (userSnapshot in snapshot.children) {
                            val role = userSnapshot.child("role").getValue(String::class.java)
                            val firstName = userSnapshot.child("FirstName").getValue(String::class.java) ?: ""
                            val lastName = userSnapshot.child("LastName").getValue(String::class.java) ?: ""

                            if (role == "client") {
                                val entryKey = databaseRef.child(currentUserId).child("MyClient").push().key
                                if (entryKey != null) {
                                    val clientData = mapOf(
                                        "FirstName" to firstName,
                                        "LastName" to lastName,
                                        "email" to email
                                    )

                                    databaseRef.child(currentUserId).child("MyClient").child(entryKey)
                                        .setValue(clientData)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                callback(true, "Client added successfully")
                                            } else {
                                                callback(false, "Failed to add client: ${task.exception?.message}")
                                            }
                                        }
                                } else {
                                    callback(false, "Failed to generate a unique key")
                                }
                            } else {
                                callback(false, "This email does not belong to a client")
                            }
                            return
                        }
                    } else {
                        callback(false, "User not found")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false, "Error: ${error.message}")
                }
            })
    }
}
