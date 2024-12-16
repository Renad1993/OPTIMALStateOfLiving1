package com.example.optimalstateofliving

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val channelId = "notification_channel"

        // إعداد الصوت
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        // إنشاء مدير الإشعارات
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // إنشاء قناة إشعارات (لإصدارات أندرويد 8 وما فوق)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Notification Channel", NotificationManager.IMPORTANCE_HIGH)
            channel.setSound(soundUri, null)
            notificationManager.createNotificationChannel(channel)
        }

        // بناء الإشعار
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.icon_notification) // تأكد من وجود أيقونة
            .setContentTitle("Reminder")
            .setContentText("This is your scheduled notification!")
            .setSound(soundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        // عرض الإشعار
        notificationManager.notify(1, builder.build())
    }
}
