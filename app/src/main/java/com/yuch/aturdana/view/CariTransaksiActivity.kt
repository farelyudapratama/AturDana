package com.yuch.aturdana.view

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.yuch.aturdana.data.TransactionAdapter
import com.yuch.aturdana.data.pref.TransactionModel
import com.yuch.aturdana.databinding.ActivityCariTransaksiBinding
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class CariTransaksiActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCariTransaksiBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var isIncome: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCariTransaksiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isIncome = intent.getBooleanExtra("IS_INCOME", false)
        supportActionBar?.title = if (isIncome) "Lacak Pendapatan" else "Lacak Pengeluaran"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        binding.apply {
            cvDateFilter.setOnClickListener {
                showDateRangePicker()
            }
            tvFilterAll.setOnClickListener {
                fetchTransactionsForAllTime()
                binding.tvTotalTransactionTitle.text = if (isIncome) "Total Pendapatan Anda :" else "Total Pengeluaran Anda :"
            }
        }

        fetchCurrentMonthTransactions()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun fetchCurrentMonthTransactions() {
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

    private fun fetchTransactionsForAllTime() {
        val userId = auth.currentUser?.uid ?: return
        val transactionRef = database.child("transaction")
        val query = transactionRef.orderByChild("user_id").equalTo(userId)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var totalTransaction = 0.0

                val transactions = mutableListOf<TransactionModel>()
                for (data in dataSnapshot.children) {
                    val transaction = data.getValue(TransactionModel::class.java)
                    val type = data.child("type").getValue(String::class.java)
                    val amount = data.child("amount").getValue(String::class.java)
                    val amountDouble = amount?.toDoubleOrNull() ?: 0.0
                    if ((isIncome && type == "Pendapatan" && amount != null && transaction != null) || (!isIncome && type == "Pengeluaran" && amount != null && transaction != null)) {
                        transaction.transactionId = data.key
                        transactions.add(transaction)
                        totalTransaction += amountDouble
                    }
                }
                if (transactions.isEmpty()) {
                    binding.tvNoTransaction.visibility = View.VISIBLE
                    binding.rvTransaksi.visibility = View.GONE
                } else {
                    binding.tvNoTransaction.visibility = View.GONE
                    binding.rvTransaksi.visibility = View.VISIBLE
                    val adapter = TransactionAdapter(transactions)
                    binding.rvTransaksi.adapter = adapter
                    binding.rvTransaksi.layoutManager = LinearLayoutManager(this@CariTransaksiActivity)
                    binding.tvTotalTransaction.setFormattedCurrency(totalTransaction)
                    binding.tvTotalTransactionTitle.text = if (isIncome) "Total Pendapatan Keseluruhan" else "Total Pengeluaran Keseluruhan"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun showDateRangePicker() {
        val dateRangePicker = MaterialDatePicker.Builder
            .dateRangePicker()
            .setTitleText("Pilih Tanggal")
            .build()

        dateRangePicker.show(
            supportFragmentManager,
            "date_range_picker"
        )

        dateRangePicker.addOnPositiveButtonClickListener { dateSelected ->
            val startDate = dateSelected.first
            val endDate = dateSelected.second

            if (startDate != null && endDate != null) {
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = startDate
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val adjustedStartDate = calendar.timeInMillis

                calendar.apply {
                    timeInMillis = endDate
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                val adjustedEndDate = calendar.timeInMillis

                binding.tvDate.text = "${convertTimestampToDate(adjustedStartDate)} - ${convertTimestampToDate(adjustedEndDate)}"
                fetchTransactionsForDateRange(adjustedStartDate, adjustedEndDate)
            }
        }
    }

    private fun fetchTransactionsForDateRange(startDate: Long, endDate: Long) {
        val userId = auth.currentUser?.uid ?: return
        val transactionRef = database.child("transaction")
        val query = transactionRef.orderByChild("user_id").equalTo(userId)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var totalPendapatan = 0.0

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val transactions = mutableListOf<TransactionModel>()

                for (data in dataSnapshot.children) {
                    val transaction = data.getValue(TransactionModel::class.java)
                    val type = data.child("type").getValue(String::class.java)
                    val amount = data.child("amount").getValue(String::class.java)
                    val date = data.child("date").getValue(String::class.java)

                    Log.d("Date Check", "Transaction date: $date")

                    if ((isIncome && type == "Pendapatan" && amount != null && date != null && transaction != null) ||
                        (!isIncome && type == "Pengeluaran" && amount != null && date != null && transaction != null)) {
                        val transactionDate = try {
                            sdf.parse(date)?.time
                        } catch (e: ParseException) {
                            null
                        }

                        Log.d("Date Check", "Parsed date: $transactionDate")

                        if (transactionDate != null && transactionDate in startDate..endDate) {
                            transaction.transactionId = data.key
                            transactions.add(transaction)
                            totalPendapatan += amount.toDouble()
                        }
                    }
                }

                val adapter = TransactionAdapter(transactions)
                binding.rvTransaksi.adapter = adapter
                binding.rvTransaksi.layoutManager = LinearLayoutManager(this@CariTransaksiActivity)
                binding.tvTotalTransaction.setFormattedCurrency(totalPendapatan)
                binding.tvTotalTransactionTitle.text = if (isIncome) "Total Pendapatan" else "Total Pengeluaran"
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
