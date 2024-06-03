package com.yuch.aturdana.data

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yuch.aturdana.R
import com.yuch.aturdana.data.pref.BudgetModel

class BudgetAdapter(private var budgetList: List<BudgetModel>) :
    RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_budget, parent, false)
        return BudgetViewHolder(view)
    }
    override fun onBindViewHolder(holder: BudgetAdapter.BudgetViewHolder, position: Int) {
        val budget = budgetList[position]
        holder.bind(budget)
    }

    override fun getItemCount(): Int = budgetList.size

    inner class BudgetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val context: Context = itemView.context
        private val tvCategory: TextView = itemView.findViewById(R.id.textViewCategory)
        private val tvBudget: TextView = itemView.findViewById(R.id.textViewBudget)

        init {
            itemView.setOnClickListener(this)
        }
        fun bind(budget: BudgetModel) {
            tvCategory.text = budget.category_id
            tvBudget.text = "Anggaran = ${budget.amount}"
        }
        override fun onClick(v: View?) {
            TODO("Not yet implemented")
        }

    }
}