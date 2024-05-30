package com.yuch.aturdana.view

import android.R
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.yuch.aturdana.databinding.ActivityAddBinding
import com.yuch.aturdana.view.main.MainActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private val calendar: Calendar = Calendar.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        setCurrentDateTime()
        binding.apply {
            buttonPendapatan.setOnClickListener {
                updateKategori("pendapatan")
            }
            buttonPengeluaran.setOnClickListener {
                updateKategori("pengeluaran")
            }

            updateKategori("pendapatan")

            buttonSimpan.setOnClickListener {
                saveTransaction()
            }
            datepickerTanggal.
            setOnClickListener {
                openDatePicker()
            }
            timepickerWaktu.setOnClickListener {
                openTimePicker()
            }
        }
    }
    private fun setCurrentDateTime() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        val currentDate = dateFormat.format(calendar.time)
        val currentTime = timeFormat.format(calendar.time)

        binding.datepickerTanggal.text = currentDate
        binding.timepickerWaktu.text = currentTime
    }
    private fun openDatePicker() {
        val datePicker = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            setCurrentDateTime()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        datePicker.show()
    }
    private fun openTimePicker() {
        val timePicker = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                setCurrentDateTime()
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePicker.show()
    }
    private fun updateKategori(type: String) {
        val categories = if (type == "pendapatan") {
            arrayOf("Gaji", "Bonus", "Investasi")
        } else {
            arrayOf("Belanja", "Transportasi", "Makan", "Tagihan")
        }

        val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spinnerKategori.adapter = adapter

        if (type == "pendapatan") {
            binding.buttonPendapatan.isEnabled = false
            binding.buttonPengeluaran.isEnabled = true
        } else {
            binding.buttonPendapatan.isEnabled = true
            binding.buttonPengeluaran.isEnabled = false
        }
    }

    private fun saveTransaction() {
        val type = if (binding.buttonPendapatan.isEnabled) "Pengeluaran" else "Pendapatan"
        val kategori = binding.spinnerKategori.selectedItem.toString()
        val jumlah = binding.edittextJumlah.text.toString()
        val catatan = binding.edittextCatatan.text.toString()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = dateFormat.format(calendar.time)
        val time = timeFormat.format(calendar.time)

        val userId = auth.currentUser?.uid
        if (userId != null) {
            val transactionId = database.child("transaction").push().key
            if (transactionId != null) {
                val transactionMap = mapOf(
                    "type" to type,
                    "category_id" to kategori,
                    "amount" to jumlah,
                    "description" to catatan,
                    "date" to date,
                    "time" to time,
                    "user_id" to userId
                )
                database.child("transaction").child(transactionId).setValue(transactionMap)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Transaction saved", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to save transaction", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

}