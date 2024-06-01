package com.yuch.aturdana.data

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.yuch.aturdana.R
import com.yuch.aturdana.data.pref.TransactionModel
import com.yuch.aturdana.view.setFormattedCurrency

class TransactionAdapter(private val transactions: List<TransactionModel>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaksi, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.bind(transaction)
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(transaction: TransactionModel) {
            itemView.findViewById<TextView>(R.id.tv_tanggal).text = transaction.date
            itemView.findViewById<TextView>(R.id.tv_keterangan).text = transaction.description
            itemView.findViewById<TextView>(R.id.tv_waktu).text = transaction.time
            itemView.findViewById<TextView>(R.id.tv_kategori).text = transaction.category_id

            val amountTextView = itemView.findViewById<TextView>(R.id.tv_jumlah)
            val amount = transaction.amount?.toDoubleOrNull() ?: 0.0
            amountTextView.setFormattedCurrency(amount)

            val cardView = itemView.findViewById<CardView>(R.id.card_view_transaksi)
            val backgroundColor = if (transaction.type == "Pendapatan") R.color.green else R.color.red
            cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, backgroundColor))
        }
    }
}
