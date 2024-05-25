package com.yuch.aturdana.view

import android.content.ContentValues.TAG
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.yuch.aturdana.R
import com.yuch.aturdana.data.pref.UserModel
import com.yuch.aturdana.databinding.ActivityEditProfileBinding

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: StorageReference
    private lateinit var originalUser: UserModel

    private var uri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("users")
        storage = FirebaseStorage.getInstance().getReference("images")

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid

            database.child(uid).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(UserModel::class.java)
                    user?.let {
                        originalUser = it
                        binding.apply {
                            etNama.setText(it.username)
                            etEmail.setText(it.email)
                            Log.d(TAG, "onDataChange: ${it.avatarUrl}")
                            Glide.with(this@EditProfileActivity)
                                .load(it.avatarUrl)
                                .placeholder(R.drawable.baseline_person_24)
                                .error(R.drawable.baseline_person_24)
                                .into(ivAvatar)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle possible errors.
                    Toast.makeText(this@EditProfileActivity, error.message, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "onCancelled: ${error.message}")
                }
            })
        }

        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) {imageUri ->
            if (imageUri != null) {
                uri = imageUri
                Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.baseline_person_24)
                    .error(R.drawable.baseline_person_24)
                    .into(binding.ivAvatar)
            }
        }

        binding.fabEdit.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.btnSave.setOnClickListener {
            if (uri != null) {
                uploadImageAndSaveData(uri!!)
            } else {
                saveUserProfile(null) // Save user data without changing the avatar
            }
        }
    }
    private fun uploadImageAndSaveData(uri: Uri) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            val imageRef = storage.child("$uid/avatar.jpg")

            imageRef.putFile(uri)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        saveUserProfile(downloadUrl.toString())
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "uploadImageAndSaveData: ${exception.message}")
                }
        }
    }
    private fun saveUserProfile(avatarUrl: String?) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            val username = binding.etNama.text.toString()
            val email = binding.etEmail.text.toString()

            val newUsername = if (username != originalUser.username) username else originalUser.username
            val newEmail = if (email != originalUser.email) email else originalUser.email
            val newAvatarUrl = avatarUrl ?: originalUser.avatarUrl // Pastikan avatarUrl tidak null
            val createdAt = originalUser.createdAt // Tetap gunakan createAt yang sudah ada

            val user = UserModel(newUsername, newEmail, newAvatarUrl, createdAt)

            database.child(uid).setValue(user)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "saveUserProfile: ${exception.message}")
                }
        }
    }

}