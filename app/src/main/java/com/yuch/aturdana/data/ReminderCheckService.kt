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

        // Lakukan pemeriksaan status pengingat
        checkReminders()

        // Jadwalkan pemeriksaan berulang setiap 30 menit
        scheduleNextCheck()

        return START_STICKY
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
                        if (reminder != null && reminder.reminderDate == todayDate && reminder.status != "selesai") {
                            // Jika pengingat belum selesai dan hari tenggatnya adalah hari ini, tampilkan notifikasi
                            showNotification(reminder)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ReminderCheckService", "Failed to load reminders", error.toException())
                }
            })
        }
    }

    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    private fun showNotification(reminder: ReminderModel) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create a notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "REMINDER_CHANNEL",
                "Reminder Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Build the notification
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
            .build()

        // Show the notification
        notificationManager.notify(reminder.reminderId.hashCode(), notification)
    }

    private fun scheduleNextCheck() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReminderCheckService::class.java)
        val pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        // Schedule next check every 30 minutes
        val intervalMillis = 30 * 60 * 1000 // 30 minutes in milliseconds
        val triggerAtMillis = SystemClock.elapsedRealtime() + intervalMillis
        alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, pendingIntent)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
