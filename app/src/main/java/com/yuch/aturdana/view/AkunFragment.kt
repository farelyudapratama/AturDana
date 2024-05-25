package com.yuch.aturdana.view

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.yuch.aturdana.R
import com.yuch.aturdana.data.pref.UserModel
import com.yuch.aturdana.databinding.FragmentAkunBinding
import com.yuch.aturdana.view.login.LoginActivity

class AkunFragment : Fragment(R.layout.fragment_akun) {
    private var _binding: FragmentAkunBinding? = null
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentAkunBinding.bind(view)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        val currentUser = auth.currentUser
        if (currentUser != null) {
//            val username = user.displayName
//            val email = user.email
            val uid = currentUser.uid
//            val photoUrl = user.photoUrl.toString()

            database.child("users").child(uid).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(UserModel::class.java)
                    user?.let {
                        _binding?.apply {
                            tvName.text = it.username
                            tvEmail.text = it.email
//                            tvCreateAt.text = it.create_at
                            Log.d(ContentValues.TAG, "onDataChange: ${it.avatarUrl}")
                            Glide.with(this@AkunFragment)
                                .load(it.avatarUrl)
                                .placeholder(R.drawable.baseline_person_24)
                                .error(R.drawable.baseline_person_24)
                                .into(ivAvatar)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle possible errors.
                    Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
                }
            })
        }
        setupAction()
    }

    private fun setupAction() {
        _binding?.apply {
            tvEditProfile.setOnClickListener {
                val intent = Intent(requireContext(), EditProfileActivity::class.java)
                startActivity(intent)
            }
            btnLogout.setOnClickListener {
                auth.signOut()
                val intent = Intent(requireContext(), LoginActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
//        outState.putString(EXTRA_USERNAME, username)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}