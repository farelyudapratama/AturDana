package com.yuch.aturdana.view

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.switchmaterial.SwitchMaterial
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
    private lateinit var switchNotification: SwitchMaterial

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentAkunBinding.bind(view)

        (activity as? AppCompatActivity)?.supportActionBar?.hide()

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

//        Log.d("ReminderCheckService", "Notification Enabled: $isNotificationEnabled")

        _binding?.let { binding ->
            switchNotification = binding.notificationPreference
            // Setup listener and get saved preference
            switchNotification.setOnCheckedChangeListener { _, isChecked ->
                // Save notification preference
                saveNotificationPreference(isChecked)
                if (isChecked) {
                    binding.notificationPreference.text = "Matikan Notifikasi"
                } else {
                    binding.notificationPreference.text = "Aktifkan Notifikasi"
                }
            }
            switchNotification.isChecked = getNotificationPreference()
        }
    }

    private fun saveNotificationPreference(isChecked: Boolean) {
        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("notification_preference", isChecked)
        editor.apply()
    }

    private fun getNotificationPreference(): Boolean {
        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("notification_preference", true)
    }

    private fun setupAction() {
        _binding?.apply {
            tvEditProfile.setOnClickListener {
                val intent = Intent(requireContext(), EditProfileActivity::class.java)
                startActivity(intent)
            }
            btnLogout.setOnClickListener {
                val dialogBuilder = AlertDialog.Builder(requireContext())
                dialogBuilder.setMessage("Apakah anda yakin untuk logout ini?")
                    .setCancelable(false)
                    .setPositiveButton("Ya, logout") { dialog, id ->
                        auth.signOut()
                        val intent = Intent(requireContext(), LoginActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish()
                    }
                    .setNegativeButton("Tidak") { dialog, id ->
                        dialog.dismiss()
                    }
                val alert = dialogBuilder.create()
                alert.setTitle("Logout")
                alert.show()
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