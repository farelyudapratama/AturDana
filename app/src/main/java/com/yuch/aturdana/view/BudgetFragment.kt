package com.yuch.aturdana.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.yuch.aturdana.R
import com.yuch.aturdana.databinding.FragmentBudgetBinding

class BudgetFragment : Fragment(R.layout.fragment_budget) {
    private lateinit var _binding: FragmentBudgetBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBudgetBinding.bind(view)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        _binding.buttonAddBudget.setOnClickListener {
            val intent = Intent(requireContext(), BudgetActivity::class.java)
            startActivity(intent)
        }
    }
}