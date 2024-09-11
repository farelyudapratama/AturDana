package com.yuch.aturdana.data

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yuch.aturdana.R
import com.yuch.aturdana.data.pref.ReminderModel
import com.yuch.aturdana.view.DetailReminderActivity
import com.yuch.aturdana.view.toCurrencyFormat

class ReminderAdapter(private val reminders: List<ReminderModel>) :
    RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ReminderAdapter.ReminderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reminder, parent, false)
        return ReminderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReminderAdapter.ReminderViewHolder, position: Int) {
        val reminder = reminders[position]
        holder.bind(reminder)
    }

    override fun getItemCount(): Int = reminders.size

    inner class ReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val context: Context = itemView.context
        private val tvReminderDesc: TextView = itemView.findViewById(R.id.tv_reminder)
        private val tvReminderAmount: TextView = itemView.findViewById(R.id.tv_jumlah)
        private val tvReminderDate: TextView = itemView.findViewById(R.id.tv_tanggal)
        private val statusIndicator: View = itemView.findViewById(R.id.tv_status)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(reminder: ReminderModel) {
            tvReminderDesc.text = reminder.reminderDesc
            tvReminderAmount.text = reminder.reminderAmount?.toDoubleOrNull()?.toCurrencyFormat()
            tvReminderDate.text = reminder.reminderDate
            if (reminder.status == "selesai") {
                statusIndicator.visibility = View.VISIBLE
            } else {
                statusIndicator.visibility = View.GONE
                itemView.alpha = 1.0f
            }
        }

        override fun onClick(view: View) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val reminder = reminders[position]
                val intent = Intent(context, DetailReminderActivity::class.java).apply {
                    putExtra("reminderId", reminder.reminderId)
                    // Tambahkan data lainnya yang ingin di kirim ke DetailReminderActivity Untuk sementara hanya mengirim reminderId
                }
                context.startActivity(intent)
            }
        }
    }
}