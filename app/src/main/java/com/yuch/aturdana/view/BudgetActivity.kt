package com.yuch.aturdana.view

import android.app.AlertDialog
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

        val months = arrayOf(
            "Pilih Bulan", "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        )
        val monthAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMonth.adapter = monthAdapter
        binding.spinnerMonth.setSelection(0)

        binding.apply {
            buttonTambahKategori.setOnClickListener {
                showAddCategoryDialog()
            }
            buttonSimpan.setOnClickListener {
                saveBudget()
            }
        }
    }

    private fun showMonthPickerDialog() {
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

    private fun saveBudget() {
        val category = binding.spinnerKategori.selectedItem.toString()
        val amount = binding.etBudget.getCleanDoubleValue().toString()
        val month = binding.spinnerMonth.selectedItemPosition.toString()
        val year = binding.editTextYear.text.toString()

        if (category.isEmpty() || amount.isEmpty() || year.isEmpty() || month == "0") {
            Toast.makeText(this, "Kategori, jumlah, bulan, dan tahun harus valid", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid
        if (userId != null) {
            val budgetId = database.child("budgets").push().key
            if (budgetId != null) {
                val budget  = mapOf(
                    "category_id" to category,
                    "amount" to amount,
                    "month" to month,
                    "year" to year,
                    "user_id" to userId
                )
                database.child("budgets").child(budgetId).setValue(budget)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Budget berhasil disimpan", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Gagal menyimpan budget", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}