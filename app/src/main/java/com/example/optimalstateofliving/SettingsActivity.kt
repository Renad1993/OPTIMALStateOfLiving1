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

        // تغيير المعلومات الشخصية
        changeSettingsButton.setOnClickListener {
            // فتح شاشة جديدة لتغيير المعلومات
            //val intent = Intent(this, UpdateProfileActivity::class.java)
            startActivity(intent)
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
