package com.yuch.aturdana.view

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
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

        supportActionBar?.title = "Tambah Transaksi"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                startActivity(Intent(this@AddActivity, MainActivity::class.java))
                finish()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }
    override fun onSupportNavigateUp(): Boolean {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
        return true
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
        val amount = binding.edittextJumlah.getCleanDoubleValue().toString()
        val note = binding.edittextCatatan.text.toString()
        val date = binding.datepickerTanggal.text.toString()
        val time = binding.timepickerWaktu.text.toString()

        if (category.isEmpty()) {
            Toast.makeText(this, "Pilih kategori", Toast.LENGTH_SHORT).show()
            return
        }

        val amountDouble: Double
        try {
            amountDouble = amount.toDouble()
            if (amountDouble <= 0) {
                binding.edittextJumlah.error = "Jumlah harus lebih dari 0"
                binding.edittextJumlah.requestFocus()
                return
            }
        } catch (e: NumberFormatException) {
            binding.edittextJumlah.error = "Masukkan jumlah yang valid"
            binding.edittextJumlah.requestFocus()
            return
        }

        val userId = auth.currentUser?.uid
        if (userId != null) {
            // Ambil bulan dan tahun dari tanggal
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val parsedDate = dateFormat.parse(date) ?: return
            val month = SimpleDateFormat("M", Locale.getDefault()).format(parsedDate)
            val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(parsedDate)

            // Ambil anggaran dari Firebase berdasarkan kategori, bulan, dan tahun
            database.child("budgets")
                .orderByChild("user_id").equalTo(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var budget = 0.0
                        for (budgetSnapshot in snapshot.children) {
                            val budgetCategory = budgetSnapshot.child("category_id").getValue(String::class.java)
                            val budgetMonth = budgetSnapshot.child("month").getValue(String::class.java)
                            val budgetYear = budgetSnapshot.child("year").getValue(String::class.java)
                            val budgetAmount = budgetSnapshot.child("amount").getValue(String::class.java)?.toDouble() ?: 0.0

                            if (category == budgetCategory && month == budgetMonth && year == budgetYear) {
                                budget = budgetAmount
                                break
                            }
                        }

                        if (budget == 0.0) {
                            saveTransactionToFirebase(userId, type, category, amountDouble, note, date, time)
                            return
                        }

                        // Hitung total pengeluaran untuk kategori ini di bulan dan tahun yang sama
                        database.child("transaction")
                            .orderByChild("category_id").equalTo(category)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(transactionSnapshot: DataSnapshot) {
                                    var totalSpent = 0.0
                                    for (transaction in transactionSnapshot.children) {
                                        val transactionDate = transaction.child("date").getValue(String::class.java) ?: ""
                                        val transactionParsedDate = dateFormat.parse(transactionDate)
                                        val transactionMonth = SimpleDateFormat("M", Locale.getDefault()).format(transactionParsedDate)
                                        val transactionYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(transactionParsedDate)

                                        if (transactionMonth == month && transactionYear == year) {
                                            val transactionAmount = transaction.child("amount").getValue(String::class.java)?.toDouble() ?: 0.0
                                            totalSpent += transactionAmount
                                        }
                                    }

                                    // Hanya tampilkan dialog jika total pengeluaran melebihi anggaran
                                    if (totalSpent + amountDouble > budget) {
                                        // Anggaran habis, ini opsi untuk memaksa transaksi
                                        AlertDialog.Builder(this@AddActivity).apply {
                                            setTitle("Anggaran habis")
                                            setMessage("Anggaran untuk kategori ini pada bulan dan tahun ini sudah habis. Apakah Anda ingin tetap menyimpan transaksi dan membuat anggaran menjadi negatif?")
                                            setPositiveButton("Ya") { _, _ ->
                                                saveTransactionToFirebase(userId, type, category, amountDouble, note, date, time)
                                            }
                                            setNegativeButton("Tidak") { dialog, _ ->
                                                dialog.dismiss()
                                            }
                                            show()
                                        }
                                    } else {
                                        // Jika anggaran masih cukup, simpan transaksi tanpa dialog
                                        saveTransactionToFirebase(userId, type, category, amountDouble, note, date, time)
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    // Handle error
                                }
                            })
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle error
                    }
                })
        }
    }

    // Method untuk menyimpan transaksi ke Firebase
    private fun saveTransactionToFirebase(userId: String, type: String, category: String, amount: Double, note: String, date: String, time: String) {
        val transactionId = database.child("transaction").push().key
        if (transactionId != null) {
            val transaction = mapOf(
                "type" to type,
                "category_id" to category,
                "amount" to amount.toString(),
                "description" to note,
                "date" to date,
                "time" to time,
                "user_id" to userId
            )
            database.child("transaction").child(transactionId).setValue(transaction)
                .addOnSuccessListener {
                    Toast.makeText(this@AddActivity, "Transaksi berhasil disimpan", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@AddActivity, MainActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this@AddActivity, "Gagal menyimpan transaksi", Toast.LENGTH_SHORT).show()
                }
        }
    }
}