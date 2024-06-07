package com.yuch.aturdana.view

import android.app.NotificationManager
import android.os.Bundle
import android.util.Log
import android.view.View
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

    // edit
    private var isEditMode = false

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

        // Tombol Edit
        binding.btnEdit.setOnClickListener {
            isEditMode = true
            setupUI()
        }
        binding.btnCancel.setOnClickListener {
            isEditMode = false
            setupUI()
        }
        binding.btnSave.setOnClickListener {
            // Lakukan validasi input
            if (isValidInput()) {
                // Simpan perubahan ke database
                updateReminder(reminderId)
            }
        }
    }

    private fun setupUI() {
        if (isEditMode) {
            // Mode Edit
            binding.textView.text = "Edit Reminder"

            binding.tvDesc.visibility = View.GONE
            binding.tvJumlah.visibility = View.GONE
            binding.tvDate.visibility = View.GONE

            binding.etDesc.visibility = View.VISIBLE
            binding.etJumlah.visibility = View.VISIBLE
            binding.etDate.visibility = View.VISIBLE

            binding.etDesc.isEnabled = true
            binding.etJumlah.isEnabled = true
            binding.etDate.isEnabled = true

            binding.btnEdit.visibility = View.GONE
            binding.btnCancel.visibility = View.VISIBLE
            binding.btnHapus.visibility = View.GONE
            binding.btnSelesai.visibility = View.GONE
            binding.btnSave.visibility = View.VISIBLE
        } else {
            // Mode Tampilan
            binding.textView.text = "Detail Reminder"

            binding.tvDesc.visibility = View.VISIBLE
            binding.tvJumlah.visibility = View.VISIBLE
            binding.tvDate.visibility = View.VISIBLE

            binding.etDesc.visibility = View.GONE
            binding.etJumlah.visibility = View.GONE
            binding.etDate.visibility = View.GONE

            binding.etDesc.isEnabled = false
            binding.etJumlah.isEnabled = false
            binding.etDate.isEnabled = false
            binding.btnEdit.visibility = View.VISIBLE
            binding.btnHapus.visibility = View.VISIBLE
            binding.btnSelesai.visibility = View.VISIBLE
            binding.btnCancel.visibility = View.GONE
            binding.btnSave.visibility = View.GONE
        }
    }

    private fun isValidInput(): Boolean {
        // Lakukan validasi input sesuai kebutuhan (misalnya, deskripsi tidak boleh kosong)
        // Anda dapat menambahkan validasi tambahan sesuai kebutuhan
        val desc = binding.tvDesc.text.toString().trim()
        val jumlah = binding.tvJumlah.text.toString().trim()
        val date = binding.tvDate.text.toString().trim()

        return desc.isNotEmpty() && jumlah.isNotEmpty() && date.isNotEmpty()
    }

    private fun updateReminder(reminderId: String) {
        val userId = auth.currentUser?.uid

        // Perbarui data pengingat di Firebase Realtime Database
        // Anda dapat menggunakan database.update() untuk memperbarui data yang ada
        val desc = binding.etDesc.text.toString().trim()
        val jumlah = binding.etJumlah.text.toString().trim()
        val date = binding.etDate.text.toString().trim()

        val reminderUpdate = mapOf(
            "user_id" to userId,
            "reminderId" to reminderId,
            "reminderDesc" to desc,
            "reminderAmount" to jumlah,
            "reminderDate" to date
        )
        database.child("reminders").child(reminderId).setValue(reminderUpdate)
            .addOnSuccessListener {
                Toast.makeText(this, "Pengingat berhasil diperbarui", Toast.LENGTH_SHORT).show()
                isEditMode = false
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memperbarui pengingat", Toast.LENGTH_SHORT).show()
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
                                "category_id" to "Tagihan",
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

                    binding.etDesc.setText(it.reminderDesc)
                    binding.etJumlah.setText(it.reminderAmount)
                    binding.etDate.setText(it.reminderDate)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                Toast.makeText(this@DetailReminderActivity, "Failed to load reminder details", Toast.LENGTH_SHORT).show()
            }
        })

    }
}