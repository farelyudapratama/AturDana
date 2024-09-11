package com.yuch.aturdana.view

import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
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
import java.io.ByteArrayOutputStream

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

        supportActionBar?.title = "Profil"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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
            val dialogBuilder = AlertDialog.Builder(this)
            dialogBuilder.setMessage("Apakah anda yakin untuk menyimpan data ini?")
                .setCancelable(false)
                .setPositiveButton("Ya, simpan") { dialog, id ->
                    if (uri != null) {
                        uploadImageAndSaveData(uri!!)
                    } else {
                        saveUserProfile(null)
                    }
                }
                .setNegativeButton("Tidak") { dialog, id ->
                    dialog.dismiss()
                }
            val alert = dialogBuilder.create()
            alert.setTitle("Simpan Perubahan")
            alert.show()
        }
        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun compressImage(uri: Uri): ByteArray {
        val inputStream = contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
        return outputStream.toByteArray()
    }


    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun uploadImageAndSaveData(uri: Uri) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            val imageRef = storage.child("$uid/avatar.jpg")

            val compressedImage = compressImage(uri)

            binding.progressBar.visibility = View.VISIBLE

            imageRef.putBytes(compressedImage)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        saveUserProfile(downloadUrl.toString())
                    }.addOnCompleteListener {
                        binding.progressBar.visibility = View.GONE
                    }
                }
                .addOnFailureListener { exception ->
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveUserProfile(avatarUrl: String?) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            val username = binding.etNama.text.toString()

            val newUsername = if (username != originalUser.username) username else originalUser.username
            val email = originalUser.email
            val categories = originalUser.categories
            val newAvatarUrl = avatarUrl ?: originalUser.avatarUrl
            val createdAt = originalUser.createdAt

            val user = UserModel(newUsername, email, categories, newAvatarUrl, createdAt)

            binding.progressBar.visibility = View.VISIBLE

            database.child(uid).setValue(user)
                .addOnSuccessListener {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { exception ->
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "saveUserProfile: ${exception.message}")
                }
        }
    }


}