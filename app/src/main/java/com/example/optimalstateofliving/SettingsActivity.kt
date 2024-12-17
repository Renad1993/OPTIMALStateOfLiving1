package com.example.optimalstateofliving

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.EmailAuthProvider
import java.util.*

class SettingsActivity : AppCompatActivity() {

    // متغير لتحديد نتيجة طلب الإذن
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show()
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)


        val changeSettingsButton: Button = findViewById(R.id.changeSettingsButton)
        val notificationsButton: Button = findViewById(R.id.notificationsButton)
        val shareButton: Button = findViewById(R.id.shareButton)
        val backButton: Button = findViewById(R.id.logoutButton)

        changeSettingsButton.setOnClickListener {
            val currentUser = FirebaseAuth.getInstance().currentUser
            val database = FirebaseDatabase.getInstance().getReference("_user")

            // إنشاء Dialog
            val dialog = android.app.AlertDialog.Builder(this)
            dialog.setTitle("Update User Settings")

            // إنشاء حقول الإدخال
            val layout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 16, 16, 16)
            }

            val nameInput = EditText(this).apply { hint = "Enter new name (optional)" }
            val emailInput = EditText(this).apply { hint = "Enter new email (optional)" }
            val passwordInput = EditText(this).apply {
                hint = "Enter new password (optional)"
                inputType = android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            val currentPasswordInput = EditText(this).apply {
                hint = "Enter current password"
                inputType = android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            }

            layout.apply {
                addView(nameInput)
                addView(emailInput)
                addView(passwordInput)
                addView(currentPasswordInput)
            }

            dialog.setView(layout)

            // زر الحفظ
            dialog.setPositiveButton("Save") { _, _ ->
                val newName = nameInput.text.toString().trim()
                val newEmail = emailInput.text.toString().trim()
                val newPassword = passwordInput.text.toString().trim()
                val currentPassword = currentPasswordInput.text.toString().trim()

                currentUser?.let { user ->
                    val userId = user.uid

                    // التحقق من إدخال كلمة المرور الحالية
                    if (currentPassword.isNotEmpty()) {
                        val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
                        user.reauthenticate(credential).addOnCompleteListener { reAuthTask ->
                            if (reAuthTask.isSuccessful) {
                                Log.e("ReAuth", "Re-authentication successful")
                                Toast.makeText(this, "Re-authentication successful", Toast.LENGTH_SHORT).show()

                                // تحديث الاسم
                                if (newName.isNotEmpty()) {
                                    database.child(userId).child("FirstName").setValue(newName)
                                        .addOnSuccessListener {
                                            Log.e("UpdateSuccess", "Name updated successfully")
                                            Toast.makeText(this, "Name updated successfully", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("UpdateFailure", "Failed to update name: ${e.message}")
                                            Toast.makeText(this, "Failed to update name: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }

                                // تحديث البريد الإلكتروني
                                if (newEmail.isNotEmpty()) {
                                    user.updateEmail(newEmail).addOnSuccessListener {
                                        database.child(userId).child("email").setValue(newEmail)
                                        Log.e("UpdateSuccess", "Email updated successfully")
                                        Toast.makeText(this, "Email updated successfully", Toast.LENGTH_SHORT).show()
                                    }.addOnFailureListener { e ->
                                        Log.e("UpdateFailure", "Failed to update email: ${e.message}")
                                        Toast.makeText(this, "Failed to update email: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                // تحديث كلمة المرور
                                if (newPassword.isNotEmpty()) {
                                    user.updatePassword(newPassword).addOnSuccessListener {
                                        database.child(userId).child("password").setValue(newPassword)
                                        Log.e("UpdateSuccess", "Password updated successfully")
                                        Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show()
                                    }.addOnFailureListener { e ->
                                        Log.e("UpdateFailure", "Failed to update password: ${e.message}")
                                        Toast.makeText(this, "Failed to update password: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                Log.e("ReAuthFailure", "Re-authentication failed: ${reAuthTask.exception?.message}")
                                Toast.makeText(this, "Re-authentication failed: ${reAuthTask.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Log.e("ValidationError", "Current password is required for updating email or password")
                        Toast.makeText(this, "Current password is required for updating email or password", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // زر الإلغاء
            dialog.setNegativeButton("Cancel", null)

            // عرض Dialog
            dialog.show()
        }





        // تحديد أوقات الإشعارات
        notificationsButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            // عرض TimePickerDialog للمستخدم
            val timePicker = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                // ضبط الوقت الذي اختاره المستخدم
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                calendar.set(Calendar.MINUTE, selectedMinute)
                calendar.set(Calendar.SECOND, 0)

                // التحقق من إذن POST_NOTIFICATIONS
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        scheduleNotification(calendar)
                    } else {
                        // طلب إذن POST_NOTIFICATIONS من المستخدم
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                } else {
                    // للأجهزة التي تعمل على Android أقل من 13
                    scheduleNotification(calendar)
                }

                Toast.makeText(this, "Notification set for $selectedHour:$selectedMinute", Toast.LENGTH_SHORT).show()
            }, hour, minute, true)

            timePicker.show()
        }



        // اختيار الأشخاص للمشاركة معهم
        shareButton.setOnClickListener {
            val shareText = "Check out Optimal State of Living App! Download it now."

            // إنشاء Intent للمشاركة
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
            }

            // عرض التطبيقات التي تدعم المشاركة
            val chooser = Intent.createChooser(shareIntent, "Share via")

            // التحقق إذا كان WhatsApp أو Messenger مثبتين على الجهاز
            val packageManager = packageManager
            if (packageManager.resolveActivity(shareIntent, 0) != null) {
                startActivity(chooser)
            } else {
                Toast.makeText(this, "No apps available to share!", Toast.LENGTH_SHORT).show()
            }
        }

        // تسجيل الخروج
        backButton.setOnClickListener {
            // تنفيذ عملية تسجيل الخروج
            Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleNotification(calendar: Calendar) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }
}
