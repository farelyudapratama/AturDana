package com.yuch.aturdana.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.yuch.aturdana.R
import com.yuch.aturdana.data.pref.BudgetStatusModel
import com.yuch.aturdana.view.setFormattedCurrency

class BudgetAdapter(
    private val budgetStatusList: List<BudgetStatusModel>,
    private val onDeleteClick: (BudgetStatusModel) -> Unit
) : RecyclerView.Adapter<BudgetAdapter.BudgetStatusViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetStatusViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_budget, parent, false)
        return BudgetStatusViewHolder(view)
    }

    override fun onBindViewHolder(holder: BudgetStatusViewHolder, position: Int) {
        val budgetStatus = budgetStatusList[position]
        holder.bind(budgetStatus)
    }

    override fun getItemCount(): Int {
        return budgetStatusList.size
    }

    inner class BudgetStatusViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryTextView: TextView = itemView.findViewById(R.id.textViewCategory)
        private val budgetAmountView = itemView.findViewById<TextView>(R.id.textViewBudget)
        private val usedTextView: TextView = itemView.findViewById(R.id.textViewUsed)
        private val tvInfoRemainingOrOver: TextView = itemView.findViewById(R.id.tvInfoRemainingOrOver)
        private val remainingTextView: TextView = itemView.findViewById(R.id.textViewRemaining)
        private val dateTextView: TextView = itemView.findViewById(R.id.textViewDate)
        private val deleteButton: Button = itemView.findViewById(R.id.ibDelete)

        fun bind(budgetStatus: BudgetStatusModel) {
            val months = arrayOf(
                "", "Januari", "Februari", "Maret", "April", "Mei", "Juni",
                "Juli", "Agustus", "September", "Oktober", "November", "Desember"
            )

            val monthName = budgetStatus.month.toIntOrNull()?.let { months[it] }
            val year = budgetStatus.year
            val formattedDate = "$monthName $year"

            dateTextView.text = formattedDate

            categoryTextView.text = budgetStatus.categoryId
            val budgetAmount = budgetStatus.budgetAmount
            budgetAmountView.setFormattedCurrency(budgetAmount)
            val totalExpenses = budgetStatus.totalExpenses
            usedTextView.setFormattedCurrency(totalExpenses)

            val overBudgetAmount = budgetStatus.overBudgetAmount
            val remainingBudget = budgetStatus.remainingBudget
            if (budgetStatus.isOverBudget) {
                tvInfoRemainingOrOver.text = "Melebihi Anggaran = -"
                remainingTextView.setFormattedCurrency(overBudgetAmount)
                tvInfoRemainingOrOver.setTextColor(ContextCompat.getColor(itemView.context, R.color.red))
                remainingTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.red))
            } else {
                tvInfoRemainingOrOver.text = "Sisa = "
                remainingTextView.setFormattedCurrency(remainingBudget)
                tvInfoRemainingOrOver.setTextColor(ContextCompat.getColor(itemView.context, R.color.green))
                remainingTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.green))
            }

            deleteButton.setOnClickListener {
                onDeleteClick(budgetStatus)
            }
        }
    }
}
