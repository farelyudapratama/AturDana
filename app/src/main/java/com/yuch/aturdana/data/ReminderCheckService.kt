package com.yuch.aturdana.data

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yuch.aturdana.R
import com.yuch.aturdana.data.pref.ReminderModel
import com.yuch.aturdana.view.DetailReminderActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ReminderCheckService : Service() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        startForegroundService()

        checkReminders()

        scheduleNextCheck()

        return START_STICKY
    }
    private fun getNotificationPreference(): Boolean {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("notification_preference", true)
    }

    private fun startForegroundService() {
        val isNotificationEnabled = getNotificationPreference()
        Log.d("ReminderCheckService", "Notification Enabled: $isNotificationEnabled")

        if (isNotificationEnabled) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    "REMINDER_CHANNEL",
                    "Reminder Notifications",
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationManager.createNotificationChannel(channel)
            }

            val notification = NotificationCompat.Builder(this, "REMINDER_CHANNEL")
                .setContentTitle("Reminder Service")
                .setContentText("Service is running")
                .setSmallIcon(R.drawable.baseline_notifications_24)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()

            // Panggil startForeground() di sini
            startForeground(1, notification)
        } else {
            // Jika notifikasi dinonaktifkan, panggil stopForeground() untuk menghapus notifikasi
            stopForeground(true)
        }
    }

    private fun checkReminders() {
        val userId = auth.currentUser?.uid
        val todayDate = getCurrentDate()

        if (userId != null) {
            val reminderRef = database.child("reminders")
            val query = reminderRef.orderByChild("user_id").equalTo(userId)

            query.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (reminderSnapshot in snapshot.children) {
                        val reminder = reminderSnapshot.getValue(ReminderModel::class.java)
                        Log.d("ReminderCheckService", "Reminder: $reminder")
//                        if (reminder != null && reminder.reminderDate!! >= todayDate && reminder.status != "selesai") {
//                            // Jika pengingat belum selesai dan hari tenggatnya adalah hari ini, tampilkan notifikasi
//                            showNotification(reminder)
//                        }
                        val isNotificationEnabled = getNotificationPreference()
                        if (isNotificationEnabled && reminder?.reminderDate != null && reminder.status != "selesai") {
                            if (isDateTodayOrPast(reminder.reminderDate, todayDate)) {
                                // If the reminder is not completed and the deadline is today or passed, show notification
                                showNotification(reminder)
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ReminderCheckService", "Failed to load reminders", error.toException())
                }
            })
        }
    }
    private fun isDateTodayOrPast(reminderDate: String, todayDate: String): Boolean {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return try {
            val reminderDateParsed = dateFormat.parse(reminderDate)
            val todayDateParsed = dateFormat.parse(todayDate)
            reminderDateParsed != null && todayDateParsed != null && reminderDateParsed <= todayDateParsed
        } catch (e: Exception) {
            Log.e("ReminderCheckService", "Error parsing dates", e)
            false
        }
    }
    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    private fun showNotification(reminder: ReminderModel) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "REMINDER_CHANNEL",
                "Reminder Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(this, DetailReminderActivity::class.java).apply {
            putExtra("reminderId", reminder.reminderId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(this, "REMINDER_CHANNEL")
            .setSmallIcon(R.drawable.baseline_notifications_24)
            .setContentTitle("Reminder")
            .setContentText("Reminder: ${reminder.reminderDesc}")
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setOngoing(true)
            .build()

        notificationManager.notify(reminder.reminderId.hashCode(), notification)
    }

    private fun scheduleNextCheck() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReminderCheckService::class.java)
        val pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val intervalMillis = 30 * 60 * 1000 // 30 minutes in milliseconds
        val triggerAtMillis = SystemClock.elapsedRealtime() + intervalMillis

        // Continue with scheduling the exact alarm
        try {
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, pendingIntent)
        } catch (e: SecurityException) {
            // Handle SecurityException (permission denied)
            Log.e("ReminderCheckService", "Permission denied for scheduling exact alarm", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}