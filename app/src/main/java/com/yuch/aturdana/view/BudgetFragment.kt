package com.yuch.aturdana.view

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yuch.aturdana.R
import com.yuch.aturdana.data.BudgetAdapter
import com.yuch.aturdana.data.pref.BudgetModel
import com.yuch.aturdana.data.pref.BudgetStatusModel
import com.yuch.aturdana.data.pref.TransactionModel
import com.yuch.aturdana.databinding.FragmentBudgetBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BudgetFragment : Fragment(R.layout.fragment_budget) {
    private lateinit var _binding: FragmentBudgetBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBudgetBinding.bind(view)
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        _binding.buttonAddBudget.setOnClickListener {
            val intent = Intent(requireContext(), BudgetActivity::class.java)
            startActivity(intent)
        }

        getBudget()
    }

    private fun getBudget(){
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val budgetRef = database.child("budgets")
            val query = budgetRef.orderByChild("user_id").equalTo(userId)

            query.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded || context == null) return

                    val budgetList = mutableListOf<BudgetModel>()
                    for (budgetSnapshot in snapshot.children) {
                        val budget = budgetSnapshot.getValue(BudgetModel::class.java)
                        if (budget != null) {
                            budgetList.add(budget)
                        }
                    }

                    if (budgetList.isEmpty()) {
                        _binding.tvEmptyBudget.visibility = View.VISIBLE
                        _binding.recyclerViewBudgets.visibility = View.GONE
                    } else {
                        _binding.tvEmptyBudget.visibility = View.GONE
                        _binding.recyclerViewBudgets.visibility = View.VISIBLE
                        getTransaction(budgetList)
                    }

//                    displayBudgetList(budgetList)

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }
    }
    private fun getTransaction(budgetList: MutableList<BudgetModel>) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val transactionRef = database.child("transaction")
            val query = transactionRef.orderByChild("user_id").equalTo(userId)
            query.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded || context == null) return
                    val transactionList = mutableListOf<TransactionModel>()
                    for (transactionSnapshot in snapshot.children) {
                        val transaction = transactionSnapshot.getValue(TransactionModel::class.java)
                        if (transaction != null) {
                            val date = transaction.date
                            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            val parsedDate = dateFormat.parse(date.toString())
                            if (parsedDate != null) {
                                val cal = Calendar.getInstance().apply { time = parsedDate }
                                val transMonth = (cal.get(Calendar.MONTH) + 1).toString()
                                val transYear = cal.get(Calendar.YEAR).toString()
                                transaction.month = transMonth
                                transaction.year = transYear
                                transactionList.add(transaction)
                            }
                        }
                    }
                    processBudgetAndTransaction(budgetList,transactionList)
                }
                override fun onCancelled(error: DatabaseError) {

                }
            })
        }
    }

    private fun processBudgetAndTransaction(budgetList: MutableList<BudgetModel>, transactionList: MutableList<TransactionModel>) {
        val budgetStatusList = mutableListOf<BudgetStatusModel>()
        for (budget in budgetList) {
            // Menghitung total pengeluaran untuk kategori yang sesuai dengan anggaran
            val totalExpenses = transactionList
                .filter { it.category_id == budget.category_id  && it.month == budget.month && it.year == budget.year }
                .sumOf { it.amountAsDouble }

            Log.d("BudgetFragment", "Total pengeluaran: $totalExpenses")

            val budgetAmount = budget.amountAsDouble

            val remainingBudget = budgetAmount - totalExpenses
            Log.d("BudgetFragment", "Sisa anggaran: $remainingBudget")

            val isOverBudget = totalExpenses > budgetAmount
            val overBudgetAmount = if (isOverBudget) totalExpenses - budgetAmount else 0.0

            val month = budget.month
            val year = budget.year

            val budgetStatus = budget.category_id?.let {
                BudgetStatusModel(
                    categoryId = it,
                    budgetAmount = budgetAmount,
                    totalExpenses = totalExpenses,
                    remainingBudget = remainingBudget,
                    isOverBudget = isOverBudget,
                    overBudgetAmount = overBudgetAmount,
                    month = month!!,
                    year = year!!
                )
            }

            if (budgetStatus != null) {
                budgetStatusList.add(budgetStatus)
            }

            if (isOverBudget) {
                sendNotification("Anggaran Melebihi", "Anggaran Anda untuk ${budget.category_id} melebihi.")
            } else if (totalExpenses >= 0.8 * budgetAmount) {
                sendNotification("Peringatan Anggaran", "Anggaran Anda untuk ${budget.category_id} sudah mencapai 80%.")
            }
        }

        val sortedBudgetStatusList = budgetStatusList.sortedByDescending {
            val yearInt = it.year.toIntOrNull() ?: 0
            val monthInt = it.month.toIntOrNull() ?: 0
            val monthString = if (monthInt < 10) "0$monthInt" else monthInt.toString()
            yearInt * 100 + monthString.toInt()
        }

        displayBudgetStatusList(sortedBudgetStatusList)
    }
    private fun sendNotification(title: String, message: String) {
        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationBuilder = NotificationCompat.Builder(requireContext(), "BUDGET_NOTIFICATIONS")
            .setSmallIcon(R.drawable.baseline_notifications_24)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
    private fun displayBudgetStatusList(budgetStatusList: List<BudgetStatusModel>) {
        val recyclerView = _binding.recyclerViewBudgets
        val adapter = BudgetAdapter(budgetStatusList) { budgetStatus ->
            showDeleteConfirmationDialog(budgetStatus)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun showDeleteConfirmationDialog(budgetStatus: BudgetStatusModel) {
        val categoryId = budgetStatus.categoryId
        val months = arrayOf(
            "", "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        )

        val monthName = budgetStatus.month.toIntOrNull()?.let { months[it] }
        val year = budgetStatus.year
        val formatted = "$monthName $year"

        // Menampilkan dialog konfirmasi penghapusan
        AlertDialog.Builder(requireContext())
            .setTitle("Konfirmasi Penghapusan")
            .setMessage("Apakah Anda yakin ingin menghapus anggaran dari $categoryId pada $formatted?")
            .setPositiveButton("Ya") { dialog, _ ->
                deleteBudget(budgetStatus)
                dialog.dismiss()
            }
            .setNegativeButton("Tidak") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
    private fun deleteBudget(budgetStatus: BudgetStatusModel) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val budgetRef = database.child("budgets")
            val query = budgetRef.orderByChild("user_id").equalTo(userId)

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (budgetSnapshot in snapshot.children) {
                        val budget = budgetSnapshot.getValue(BudgetModel::class.java)
                        if (budget != null && budget.category_id == budgetStatus.categoryId && budget.month == budgetStatus.month && budget.year == budgetStatus.year) {
                            budgetSnapshot.ref.removeValue()
                            break
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }
    }
}