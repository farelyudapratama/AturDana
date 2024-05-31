package com.yuch.aturdana.view

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yuch.aturdana.R
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
                updateKategori("Pendapatan")
            }
            buttonPengeluaran.setOnClickListener {
                updateKategori("Pengeluaran")
            }

            updateKategori("Pendapatan")

            buttonSimpan.setOnClickListener {
                saveTransaction()
            }
            datepickerTanggal.setOnClickListener {
                openDatePicker()
            }
            timepickerWaktu.setOnClickListener {
                openTimePicker()
            }
            buttonTambahKategori.setOnClickListener {
                showAddCategoryDialog()
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
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                setCurrentDateTime()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
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
        val userId = auth.currentUser?.uid
        if (userId != null) {
            database.child("users").child(userId).child("categories")
                .orderByChild("type").equalTo(type)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val categories = mutableListOf<String>()
                        for (categorySnapshot in dataSnapshot.children) {
                            val category = categorySnapshot.child("name").getValue(String::class.java)
                            if (category != null) {
                                categories.add(category)
                            }
                        }
                        val adapter = ArrayAdapter(this@AddActivity, android.R.layout.simple_spinner_item, categories)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.spinnerKategori.adapter = adapter
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle error
                    }
                })
        }

        if (type == "Pendapatan") {
            binding.buttonPendapatan.isEnabled = false
            binding.buttonPengeluaran.isEnabled = true
        } else {
            binding.buttonPendapatan.isEnabled = true
            binding.buttonPengeluaran.isEnabled = false
        }
    }

    private fun showAddCategoryDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_add_category, null)
        val editTextCategory = dialogLayout.findViewById<EditText>(R.id.editTextCategory)

        with(builder) {
            setTitle("Tambah Kategori")
            setView(dialogLayout)
            setPositiveButton("Tambah") { dialog, _ ->
                val category = editTextCategory.text.toString()
                if (category.isNotEmpty()) {
                    saveCategoryToFirebase(category)
                } else {
                    Toast.makeText(this@AddActivity, "Kategori tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            show()
        }
    }

    private fun saveCategoryToFirebase(category: String) {
        val type = if (!binding.buttonPendapatan.isEnabled) "Pendapatan" else "Pengeluaran"
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val categoryId = database.child("users").child(userId).child("categories").push().key
            if (categoryId != null) {
                val categoryMap = mapOf(
                    "name" to category,
                    "type" to type,
                    "user_id" to userId
                )
                database.child("users").child(userId).child("categories").child(categoryId).setValue(categoryMap)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Kategori berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                        updateKategori(type)
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Gagal menambahkan kategori", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun saveTransaction() {
        val type = if (binding.buttonPendapatan.isEnabled) "Pengeluaran" else "Pendapatan"
        val category = binding.spinnerKategori.selectedItem.toString()
        val amount = binding.edittextJumlah.text.toString()
        val note = binding.edittextCatatan.text.toString()
        val date = binding.datepickerTanggal.text.toString()
        val time = binding.timepickerWaktu.text.toString()

        if (amount == null) {
            Toast.makeText(this, "Masukkan jumlah yang valid", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid
        val transactionId = database.child("transaction").push().key

        if (userId != null && transactionId != null) {
            val transaction = mapOf(
                "type" to type,
                "category_id" to category,
                "amount" to amount,
                "description" to note,
                "date" to date,
                "time" to time,
                "user_id" to userId
            )
            database.child("transaction").child(transactionId).setValue(transaction)
                .addOnSuccessListener {
                    Toast.makeText(this, "Transaksi berhasil disimpan", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal menyimpan transaksi", Toast.LENGTH_SHORT).show()
                }
        }
    }
}