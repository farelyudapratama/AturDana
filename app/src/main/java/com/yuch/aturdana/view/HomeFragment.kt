package com.yuch.aturdana.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yuch.aturdana.R
import com.yuch.aturdana.data.TransactionAdapter
import com.yuch.aturdana.data.pref.TransactionModel
import com.yuch.aturdana.databinding.FragmentHomeBinding
import java.util.Calendar

class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var _binding: FragmentHomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        onViews()
        displayTransactions()

        _binding.apply {
            cardPendapatan.setOnClickListener {
                val intent = Intent(requireContext(), CariTransaksiActivity::class.java)
                intent.putExtra("IS_INCOME", true)
                startActivity(intent)
            }
            cardPengeluaran.setOnClickListener {
                val intent = Intent(requireContext(), CariTransaksiActivity::class.java)
                intent.putExtra("IS_INCOME", false)
                startActivity(intent)
            }
        }

//        setupInsets()
    }
//    private fun setupInsets() {
//        ViewCompat.setOnApplyWindowInsetsListener(_binding.root) { view, insets ->
//            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            view.updatePadding(bottom = systemBarsInsets.bottom)
//            insets
//        }
//    }

    private fun displayTransactions() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val transactionRef = database.child("transaction")
            val query = transactionRef.orderByChild("user_id").equalTo(userId)

            query.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (!isAdded || context == null) return

                    val transactions = mutableListOf<TransactionModel>()
                    for (data in dataSnapshot.children) {
                        val transaction = data.getValue(TransactionModel::class.java)
                        if (transaction != null) {
                            transaction.transactionId = data.key
                            transactions.add(transaction)
                        }
                    }

                    if (transactions.isEmpty()) {
                        _binding.tvEmptyTransactions.visibility = View.VISIBLE
                        _binding.rvTransaksi.visibility = View.GONE
                    } else {
                        _binding.tvEmptyTransactions.visibility = View.GONE
                        _binding.rvTransaksi.visibility = View.VISIBLE
                    }

                    // Sort transactions by date
                    transactions.sortByDescending {
                        val parts = it.date!!.split("/")
                        val year = parts[2].toInt()
                        val month = parts[1].toInt()
                        val day = parts[0].toInt()
                        year * 10000 + month * 100 + day
                    }

                    // Set up RecyclerView and attach adapter
                    val adapter = TransactionAdapter(transactions)
                    _binding.rvTransaksi.adapter = adapter
                    _binding.rvTransaksi.layoutManager = LinearLayoutManager(requireContext())
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }

    private fun onViews() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val transactionRef = database.child("transaction")
            val query = transactionRef.orderByChild("user_id").equalTo(userId)

            query.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (!isAdded || context == null) return

                    var totalPendapatan = 0.0
                    var totalPengeluaran = 0.0
                    var lastUpdatePendapatan = ""
                    var lastUpdatePengeluaran = ""

                    val calendar = Calendar.getInstance()
                    val currentMonth = calendar.get(Calendar.MONTH) + 1 // Januari = 0, jadi ditambah 1
                    val currentYear = calendar.get(Calendar.YEAR)
                    var updatePendapatan = false
                    var updatePengeluaran = false

                    for (data in dataSnapshot.children) {
                        val type = data.child("type").getValue(String::class.java)
                        val amount = data.child("amount").getValue(String::class.java)
                        val date = data.child("date").getValue(String::class.java)
                        val time = data.child("time").getValue(String::class.java)

                        if (type != null && amount != null && date != null && time != null){
                            val amountDouble = amount.toDoubleOrNull() ?: 0.0
                            val dateParts = date.split("/")
                            if (dateParts.size == 3) {
                                val transDay = dateParts[0].toIntOrNull()
                                val transMonth = dateParts[1].toIntOrNull()
                                val transYear = dateParts[2].toIntOrNull()

                                if (transMonth == currentMonth && transYear == currentYear) {
                                    if (type == "Pendapatan") {
                                        totalPendapatan += amountDouble
                                        Log.d("HomeFragmentIfStatement", "Total Pendapatan: $totalPendapatan")
                                        lastUpdatePendapatan = "$date $time"
                                        updatePendapatan = true
                                    } else if (type == "Pengeluaran") {
                                        totalPengeluaran += amountDouble
                                        lastUpdatePengeluaran = "$date $time"
                                        updatePengeluaran = true
                                    }
                                }
                            }
                        }
                    }
                    Log.d("HomeFragment", "Total Pendapatan: $totalPendapatan")
                    _binding.apply {
                        tvTotalPendapatan.setFormattedCurrency(totalPendapatan)
                        tvTotalPengeluaran.setFormattedCurrency(totalPengeluaran)
                        tvTerakhirUpdatePendapatan.text = if (updatePendapatan) {
                            "Terakhir update : $lastUpdatePendapatan"
                        } else {
                            "Tidak ada transaksi pendapatan bulan ini"
                        }
                        tvTerakhirUpdatePengeluaran.text = if (updatePengeluaran) {
                            "Terakhir update : $lastUpdatePengeluaran"
                        } else {
                            "Tidak ada transaksi pengeluaran bulan ini"
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }
    }

    companion object
}