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
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.EmailAuthProvider
import java.util.*

class SettingsActivity : AppCompatActivity() {

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

            val title = TextView(this).apply {
                text = "Update User Settings"
                setPadding(20, 20, 20, 20)
                textSize = 20f
                setTextColor(ContextCompat.getColor(this@SettingsActivity, android.R.color.holo_blue_dark)) // تغيير اللون
                setTypeface(null, android.graphics.Typeface.BOLD)
            }


            val dialog = android.app.AlertDialog.Builder(this)
                .setCustomTitle(title)
                .setCancelable(false)


            val layout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 16, 16, 16)
            }

            val nameInput = EditText(this).apply { hint = "Enter new First name (optional)"
                setHintTextColor(ContextCompat.getColor(this@SettingsActivity, android.R.color.darker_gray))}
            val LastName = EditText(this).apply { hint = "Enter new Last name (optional)"
                setHintTextColor(ContextCompat.getColor(this@SettingsActivity, android.R.color.darker_gray))}
            val passwordInput = EditText(this).apply {
                hint = "Enter new password (optional)"
                setHintTextColor(ContextCompat.getColor(this@SettingsActivity, android.R.color.darker_gray))
                inputType = android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                transformationMethod = android.text.method.PasswordTransformationMethod.getInstance()
            }
            val currentPasswordInput = EditText(this).apply {
                hint = "Enter current password"
                setHintTextColor(ContextCompat.getColor(this@SettingsActivity, android.R.color.darker_gray))
                inputType = android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                transformationMethod = android.text.method.PasswordTransformationMethod.getInstance()
            }

            layout.apply {
                addView(nameInput)
                addView(LastName)
                addView(passwordInput)
                addView(currentPasswordInput)
            }

            dialog.setView(layout)


            dialog.setPositiveButton("Save") { _, _ ->
                val newName = nameInput.text.toString().trim()
                val newLastName = LastName.text.toString().trim()
                val newPassword = passwordInput.text.toString().trim()
                val currentPassword = currentPasswordInput.text.toString().trim()
                currentUser?.let { user ->
                    val userId = user.uid

                    // Check your current password entry
                    if (currentPassword.isNotEmpty()) {
                        val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
                        user.reauthenticate(credential).addOnCompleteListener { reAuthTask ->
                            if (reAuthTask.isSuccessful) {
                                Log.e("ReAuth", "Re-authentication successful")
                                Toast.makeText(this, "Re-authentication successful", Toast.LENGTH_SHORT).show()

                                // First Name Update
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
                                // Last Name Update
                                if (newLastName.isNotEmpty()) {
                                    database.child(userId).child("LastName").setValue(newLastName)
                                        .addOnSuccessListener {
                                            Log.e("UpdateSuccess", "Name updated successfully")
                                            Toast.makeText(this, "Name updated successfully", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("UpdateFailure", "Failed to update name: ${e.message}")
                                            Toast.makeText(this, "Failed to update name: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }


                                // password update
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
            dialog.setNegativeButton("Cancel", null)

            val alertDialog = dialog.create()
            alertDialog.setOnShowListener {
                alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)?.setTextColor(
                    ContextCompat.getColor(this, android.R.color.holo_green_dark)
                )
                alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)?.setTextColor(
                    ContextCompat.getColor(this, android.R.color.holo_red_dark)
                )
            }

            alertDialog.window?.setBackgroundDrawable(
                ColorDrawable(Color.parseColor("#4C585B"))
            )

            alertDialog.show()


        }



        // notification times
        notificationsButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val is24HourFormat = android.text.format.DateFormat.is24HourFormat(this)
            // View TimePickerDialog for user
            val timePicker = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                // Set the time chosen by the user
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                calendar.set(Calendar.MINUTE, selectedMinute)
                calendar.set(Calendar.SECOND, 0)

                //Create message based on user system
                val message = if (is24HourFormat) {
                    //24 hour format
                    "Notification set for ${String.format("%02d", selectedHour)}:${String.format("%02d", selectedMinute)}"
                } else {
                    //12 hour format
                    val formattedHour = if (selectedHour % 12 == 0) 12 else selectedHour % 12
                    val amPm = if (selectedHour >= 12) "PM" else "AM"
                    "Notification set for $formattedHour:${String.format("%02d", selectedMinute)} $amPm"
                }


                // Verification of the POST_NOTIFICATIONS 's authorization
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        scheduleNotification(calendar)
                    } else {
                        // Request POST_NOTIFICATIONS permission from the user
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                } else {

                    scheduleNotification(calendar)
                }

                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }, hour, minute, is24HourFormat)


            timePicker.setOnShowListener {
                timePicker.getButton(android.app.AlertDialog.BUTTON_POSITIVE)?.setTextColor(
                    ContextCompat.getColor(this, android.R.color.holo_green_dark)
                )
                timePicker.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)?.setTextColor(
                    ContextCompat.getColor(this, android.R.color.holo_red_dark)
                )
            }

            timePicker.show()
        }


        shareButton.setOnClickListener {
            val shareText = "Check out Optimal State of Living App! Download it now."
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
            }

            // View applications that support sharing
            val chooser = Intent.createChooser(shareIntent, "Share via")

            // Check if WhatsApp or Messenger are installed on the device
            val packageManager = packageManager
            if (packageManager.resolveActivity(shareIntent, 0) != null) {
                startActivity(chooser)
            } else {
                Toast.makeText(this, "No apps available to share!", Toast.LENGTH_SHORT).show()
            }
        }

        // log out
        backButton.setOnClickListener {
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