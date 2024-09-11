package com.yuch.aturdana.view

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.yuch.aturdana.databinding.ActivityReminderAddBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddReminderActivity : AppCompatActivity() {
    private lateinit var binding : ActivityReminderAddBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var database : DatabaseReference
    private val calendar: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReminderAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Buat Pengingat"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        setCurrentDateTime()
        binding.apply {
            reminderDueDate.setOnClickListener {
                openDatePicker()
            }
            buttonSetReminder.setOnClickListener {
                sendReminder()
            }
        }
    }
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
    private fun sendReminder() {
        val reminderDesc = binding.etReminderDescription.text.toString()
        val reminderAmount = binding.etReminderAmount.getCleanDoubleValue().toString()
        val formatDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val reminderDate = formatDate.format(calendar.time)

        if (reminderDesc.isEmpty()) {
            binding.etReminderDescription.error = "Deskripsi harus diisi"
            binding.etReminderDescription.requestFocus()
            return
        }

        val amountDouble: Double
        try {
            amountDouble = reminderAmount.toDouble()
            if (amountDouble <= 0) {
                binding.etReminderAmount.error = "Jumlah harus lebih dari 0"
                binding.etReminderAmount.requestFocus()
                return
            }
        } catch (e: NumberFormatException) {
            binding.etReminderAmount.error = "Masukkan jumlah yang valid"
            binding.etReminderAmount.requestFocus()
            return
        }

        val userId = auth.currentUser?.uid
        if (userId != null) {
            val reminderId = database.child("reminders").push().key
            if (reminderId != null) {
                val reminderMap = mapOf(
                    "user_id" to userId,
                    "reminderId" to reminderId,
                    "reminderDesc" to reminderDesc,
                    "reminderAmount" to reminderAmount,
                    "reminderDate" to reminderDate
                    )

                database.child("reminders").child(reminderId).setValue(reminderMap)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Reminder set successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to set reminder", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun openDatePicker() {
        val datePicker = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            setCurrentDateTime()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        datePicker.show()
    }

    private fun setCurrentDateTime() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(calendar.time)
        binding.reminderDueDate.text = currentDate
    }
}