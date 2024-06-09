package com.yuch.aturdana.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yuch.aturdana.R
import com.yuch.aturdana.data.TransactionAdapter
import com.yuch.aturdana.data.pref.TransactionModel
import com.yuch.aturdana.databinding.ActivityExpenseBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ExpenseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityExpenseBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        binding.apply {
            cvDateFilter.setOnClickListener {
                showDateRangePicker()
            }
            tvFilterAll.setOnClickListener {
                fetchTransactionsForAllTime()
                binding.tvTotalExpenseTitle.text = "Total Pengeluaran Anda :"
            }
        }

        currentMonthIncome()
    }

    private fun currentMonthIncome() {
        val calendar = Calendar.getInstance()
        val currentMonthStart = calendar.apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val currentMonthEnd = calendar.apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        binding.tvDate.text = "${convertTimestampToDate(currentMonthStart)} - ${convertTimestampToDate(currentMonthEnd)}"
        fetchTransactionsForDateRange(currentMonthStart, currentMonthEnd)
    }

    // Fungsi untuk mengambil data transaksi untuk semua waktu
    private fun fetchTransactionsForAllTime() {
        val userId = auth.currentUser?.uid ?: return
        val transactionRef = database.child("transaction")
        val query = transactionRef.orderByChild("user_id").equalTo(userId)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var totalPengeluaran = 0.0

                val transactions = mutableListOf<TransactionModel>()
                for (data in dataSnapshot.children) {
                    val transaction = data.getValue(TransactionModel::class.java)
                    val type = data.child("type").getValue(String::class.java)
                    val amount = data.child("amount").getValue(String::class.java)
                    val amountDouble = amount?.toDoubleOrNull() ?: 0.0
                    if (type == "Pengeluaran" && amount != null && transaction != null) {
                        transaction.transactionId = data.key
                        transactions.add(transaction)
                        totalPengeluaran += amountDouble
                    }
                }
                val adapter = TransactionAdapter(transactions)
                binding.rvTransaksi.adapter = adapter
                binding.rvTransaksi.layoutManager = LinearLayoutManager(this@ExpenseActivity)

                binding.tvTotalExpense.setFormattedCurrency(totalPengeluaran)
                binding.tvTotalExpenseTitle.text = "Total Pendapatan Anda Selama Ini"
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun showDateRangePicker() {
        val dateRangePicker = MaterialDatePicker.Builder
            .dateRangePicker()
            .setTitleText("Select Date")
            .build()

        dateRangePicker.show(
            supportFragmentManager,
            "date_range_picker"
        )

        dateRangePicker.addOnPositiveButtonClickListener { dateSelected ->
            val startDate = dateSelected.first
            val endDate = dateSelected.second

            if (startDate != null && endDate != null) {
                // Do something with the selected dates
                binding.tvDate.text = "${convertTimestampToDate(startDate)} - ${convertTimestampToDate(endDate)}"
                fetchTransactionsForDateRange(startDate, endDate)
            }
        }
    }

    private fun fetchTransactionsForDateRange(startDate: Long, endDate: Long) {
        val userId = auth.currentUser?.uid ?: return
        val transactionRef = database.child("transaction")
        val query = transactionRef.orderByChild("user_id").equalTo(userId)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var totalPengeluaran = 0.0

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                val transactions = mutableListOf<TransactionModel>()
                for (data in dataSnapshot.children) {
                    val transaction = data.getValue(TransactionModel::class.java)

                    val type = data.child("type").getValue(String::class.java)
                    val amount = data.child("amount").getValue(String::class.java)
                    val date = data.child("date").getValue(String::class.java)

                    if (type == "Pengeluaran" && amount != null && date != null && transaction != null) {
                        val transactionDate = sdf.parse(date).time
                        if (transactionDate in startDate..endDate) {
                            transaction.transactionId = data.key
                            transactions.add(transaction)
                            totalPengeluaran += amount.toDouble()
                        }
                    }
                }
                val adapter = TransactionAdapter(transactions)
                binding.rvTransaksi.adapter = adapter
                binding.rvTransaksi.layoutManager = LinearLayoutManager(this@ExpenseActivity)

                binding.tvTotalExpense.setFormattedCurrency(totalPengeluaran)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun convertTimestampToDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(timestamp)
    }
}