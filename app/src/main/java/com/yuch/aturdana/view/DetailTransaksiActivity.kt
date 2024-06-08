package com.yuch.aturdana.view

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yuch.aturdana.R
import com.yuch.aturdana.data.pref.BudgetStatusModel
import com.yuch.aturdana.data.pref.TransactionModel
import com.yuch.aturdana.databinding.ActivityDetailTransaksiBinding

class DetailTransaksiActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailTransaksiBinding
    private lateinit var transactionId: String

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailTransaksiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        transactionId = intent.getStringExtra("key") ?: return

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        fetchTransactionDetails(transactionId)
        binding.btnHapus.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun showDeleteConfirmationDialog() {
        // Menampilkan dialog konfirmasi penghapusan
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Penghapusan")
            .setMessage("Apakah Anda yakin ingin menghapus transaksi ini?")
            .setPositiveButton("Ya") { dialog, _ ->
                deleteTransaction()
                dialog.dismiss()
            }
            .setNegativeButton("Tidak") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun deleteTransaction() {
        database.child("transaction").child(transactionId).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Berhasil menghapus transaksi", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menghapus transaksi", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchTransactionDetails(transactionId: String) {
        database.child("transaction").child(transactionId).addListenerForSingleValueEvent( object :
            ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val transaction = snapshot.getValue(TransactionModel::class.java)
                    transaction?.let {
                        binding.apply {
                            tvTanggal.text = it.date
                            tvWaktu.text = it.time
                            tvCatatan.text = it.description
                            tvJumlah.text = it.amount?.toDoubleOrNull()?.toCurrencyFormat()
                            tvType.text = it.type
                        }

                        if (it.type == "Pendapatan") {
                            binding.tvJumlah.setTextColor(ContextCompat.getColor(this@DetailTransaksiActivity, R.color.green))
                            binding.tvType.setTextColor(ContextCompat.getColor(this@DetailTransaksiActivity, R.color.green))
                        } else {
                            binding.tvJumlah.setTextColor(ContextCompat.getColor(this@DetailTransaksiActivity, R.color.red))
                            binding.tvType.setTextColor(ContextCompat.getColor(this@DetailTransaksiActivity, R.color.red))
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}