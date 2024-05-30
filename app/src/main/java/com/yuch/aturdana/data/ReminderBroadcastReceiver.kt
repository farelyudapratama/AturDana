package com.yuch.aturdana.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.yuch.aturdana.R
import com.yuch.aturdana.view.DetailReminderActivity

class ReminderBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create a notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "REMINDER_CHANNEL",
                "Reminder Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
        val reminderId = intent?.getStringExtra("reminderId") // ambil reminderId dari intent

        // Intent untuk memulai DetailReminderActivity
        val notificationIntent = Intent(context, DetailReminderActivity::class.java).apply {
            putExtra("reminderId", reminderId) // tambahkan reminderId ke intent
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        // Build the notification
        val notification = NotificationCompat.Builder(context, "REMINDER_CHANNEL")
            .setSmallIcon(R.drawable.baseline_notifications_24)
            .setContentTitle("Reminder")
            .setContentText(intent?.getStringExtra("description"))
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setOngoing(true)
            .build()

        notificationManager.notify(intent?.getIntExtra("notification_id", 0) ?: 0, notification)
    }
}