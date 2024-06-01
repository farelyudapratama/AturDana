package com.yuch.aturdana.view

import android.app.NotificationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yuch.aturdana.data.pref.ReminderModel
import com.yuch.aturdana.databinding.ActivityDetailReminderBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DetailReminderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailReminderBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var reminderId: String
    private val calendar: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailReminderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        reminderId = intent.getStringExtra("reminderId") ?: return
        Log.d("DetailReminderActivity", "Received reminderId from notification: $reminderId")
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference


        fetchReminderDetails(reminderId)
        binding.btnSelesai.setOnClickListener {
            addTransactionAndFinish(reminderId)
        }
        binding.btnHapus.setOnClickListener {
            deleteReminder(reminderId)
        }
    }

    private fun deleteReminder(reminderId: String) {
        database.child("reminders").child(reminderId).removeValue()
            .addOnSuccessListener {
                // Hapus notifikasi
                val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(intent.getIntExtra("notification_id", 0))

                // Tampilkan pesan sukses
                Toast.makeText(this@DetailReminderActivity, "Reminder deleted successfully", Toast.LENGTH_SHORT).show()

                // Tutup activity
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this@DetailReminderActivity, "Failed to delete reminder", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addTransactionAndFinish(reminderId: String) {
        val userId = auth.currentUser?.uid

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val time = timeFormat.format(calendar.time)

        if (userId != null) {
            val transactionId = database.child("transactions").push().key
            if (transactionId != null) {
                database.child("reminders").child(reminderId).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val reminder = snapshot.getValue(ReminderModel::class.java)
                        if (reminder != null) {
                            val transactionMap = mapOf(
                                "type" to "Pengeluaran",
                                "user_id" to userId,
                                "transactionId" to transactionId,
                                "description" to reminder.reminderDesc,
                                "amount" to reminder.reminderAmount,
                                "category_id" to "tagihan",
                                "date" to reminder.reminderDate,
                                "time" to time
                            )
                            database.child("transaction").child(transactionId).setValue(transactionMap)
                                .addOnSuccessListener {
                                    markReminderAsCompleted(reminderId)
                                    // Hapus notifikasi
                                    val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                                    notificationManager.cancel(intent.getIntExtra("notification_id", 0))

                                    // Tutup activity
                                    finish()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this@DetailReminderActivity, "Failed to add transaction", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@DetailReminderActivity, "Failed to load reminder details", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }
    private fun markReminderAsCompleted(reminderId: String) {
        val reminderRef = database.child("reminders").child(reminderId)
        reminderRef.child("status").setValue("selesai")
    }
    private fun fetchReminderDetails(reminderId: String) {
        database.child("reminders").child(reminderId).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val reminder = snapshot.getValue(ReminderModel::class.java)
                reminder?.let {
                    binding.tvDesc.text = it.reminderDesc
                    binding.tvJumlah.text = it.reminderAmount?.toDoubleOrNull()?.toCurrencyFormat()
                    binding.tvDate.text = it.reminderDate
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                Toast.makeText(this@DetailReminderActivity, "Failed to load reminder details", Toast.LENGTH_SHORT).show()
            }
        })

    }
}