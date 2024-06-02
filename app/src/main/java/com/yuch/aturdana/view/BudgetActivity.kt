package com.yuch.aturdana.view

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.DatePicker
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
import com.yuch.aturdana.databinding.ActivityBudgetBinding
import java.util.Calendar

class BudgetActivity : AppCompatActivity() {
    private lateinit var binding : ActivityBudgetBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var database : DatabaseReference
    private val calendar: Calendar = Calendar.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBudgetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        updateKategori()
        setCurrentMonth()

        binding.apply {
            buttonTambahKategori.setOnClickListener {
                showAddCategoryDialog()
            }
            tvPilihBulan.setOnClickListener {
                showMonthPickerDialog()
            }
        }
    }

    private fun setCurrentMonth() {
        val monthYearFormat = "MMMM yyyy"
        val currentMonthYear = java.text.SimpleDateFormat(monthYearFormat, java.util.Locale("id", "ID")).format(calendar.time)
        Log.d("TAG", "setCurrentMonth: $currentMonthYear")
        binding.tvPilihBulan.text = currentMonthYear
    }

    private fun showMonthPickerDialog() {
        val monthsYearPicker = DatePickerDialog(
            this,
            { _, year, month, _ ->
                calendar.set(year, month)
                setCurrentMonth()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Hide the day spinner
        try {
            val datePickerField = monthsYearPicker.javaClass.getDeclaredField("mDatePicker")
            datePickerField.isAccessible = true
            val datePicker = datePickerField.get(monthsYearPicker) as DatePicker
            val daySpinnerField = datePicker.javaClass.getDeclaredField("mDaySpinner")
            daySpinnerField.isAccessible = true
            val daySpinner = daySpinnerField.get(datePicker) as View
            daySpinner.visibility = View.GONE
        } catch (e: Exception) {
            e.printStackTrace()
        }

        monthsYearPicker.show()
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
                    Toast.makeText(this@BudgetActivity, "Kategori tidak boleh kosong", Toast.LENGTH_SHORT).show()
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
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val categoryId = database.child("users").child(userId).child("categories").push().key
            if (categoryId != null) {
                val categoryMap = mapOf(
                    "name" to category,
                    "type" to "Pengeluaran",
                    "user_id" to userId
                )
                database.child("users").child(userId).child("categories").child(categoryId).setValue(categoryMap)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Kategori berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                        updateKategori()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Gagal menambahkan kategori", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun updateKategori() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            database.child("users").child(userId).child("categories")
                .orderByChild("type").equalTo("Pengeluaran")
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val categories = mutableListOf<String>()
                        for (categorySnapshot in snapshot.children) {
                            val category = categorySnapshot.child("name").getValue(String::class.java)
                            if (category != null) {
                                categories.add(category)
                            }
                        }
                        val adapter = ArrayAdapter(this@BudgetActivity, android.R.layout.simple_spinner_item, categories)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.spinnerKategori.adapter = adapter
                    }
                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
        }
    }
}