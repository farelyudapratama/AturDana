package com.yuch.aturdana.view

import android.content.Intent
import android.os.Bundle
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
import com.yuch.aturdana.data.ReminderAdapter
import com.yuch.aturdana.data.pref.ReminderModel
import com.yuch.aturdana.databinding.FragmentReminderBinding

class ReminderFragment : Fragment(R.layout.fragment_reminder) {
    private lateinit var _binding : FragmentReminderBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var database : DatabaseReference
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentReminderBinding.bind(view)
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        _binding.apply {
            buttonSetReminder.setOnClickListener {
                val intent = Intent(requireContext(), AddReminderActivity::class.java)
                startActivity(intent)
            }
        }

        val userId = auth.currentUser?.uid
        if (userId != null) {
            val reminderRef = database.child("reminders")
            val query = reminderRef.orderByChild("user_id").equalTo(userId)

            query.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded || context == null) return

                    val reminderList = mutableListOf<ReminderModel>()
                    for (reminderSnapshot in snapshot.children) {
                        val reminder = reminderSnapshot.getValue(ReminderModel::class.java)
                        if (reminder != null) {
                            reminderList.add(reminder)
                        }
                    }

                    if (reminderList.isEmpty()) {
                        _binding.tvEmptyReminder.visibility = View.VISIBLE
                        _binding.reminderList.visibility = View.GONE
                    } else {
                        _binding.tvEmptyReminder.visibility = View.GONE
                        _binding.reminderList.visibility = View.VISIBLE
                    }

                    reminderList.sortByDescending {
                        val parts = it.reminderDate!!.split("/")
                        val year = parts[2].toInt()
                        val month = parts[1].toInt()
                        val day = parts[0].toInt()
                        year * 10000 + month * 100 + day
                    }
                    val adapter = ReminderAdapter(reminderList)
                    _binding.reminderList.adapter = adapter
                    _binding.reminderList.layoutManager = LinearLayoutManager(requireContext())
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }
}