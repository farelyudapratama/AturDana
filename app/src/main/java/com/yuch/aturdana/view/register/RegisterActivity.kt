package com.yuch.aturdana.view.register

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.yuch.aturdana.databinding.ActivityRegisterBinding
import com.yuch.aturdana.view.login.LoginActivity

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        setupAction()
    }
    private fun addDefaultCategories(userId: String) {
        val defaultCategories = mapOf(
            "Pendapatan" to listOf("Gaji", "Bonus", "Investasi"),
            "Pengeluaran" to listOf("Belanja", "Transportasi", "Makan", "Tagihan")
        )

        for ((type, categories) in defaultCategories) {
            for (category in categories) {
                val categoryId = database.child("users").child(userId).child("categories").push().key
                if (categoryId != null) {
                    val categoryMap = mapOf(
                        "name" to category,
                        "type" to type,
                        "user_id" to userId
                    )
                    database.child("users").child(userId).child("categories").child(categoryId).setValue(categoryMap)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d(TAG, "Category $category, type : $type, added successfully for user $userId")
                            } else {
                                Log.w(TAG, "Failed to add category $category for user $userId", task.exception)
                            }
                        }
                } else {
                    Log.w(TAG, "Failed to generate category ID for user $userId")
                }
            }
        }
    }

    private fun setupAction() {
        binding.registerButton.setOnClickListener {
            val name = binding.edRegisterName.text?.toString() ?: ""
            val email = binding.edRegisterEmail.text?.toString() ?: ""
            val password = binding.edRegisterPassword.text?.toString() ?: ""

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "createUserWithEmail:success")
                        val uid = auth.currentUser?.uid // Get UID of the newly created user
                        if (uid != null) {
                            Log.d(TAG, "User ID: $uid")
                            val userMap = hashMapOf(
                                "username" to name,
                                "email" to email,
                                "avatarUrl" to "https://firebasestorage.googleapis.com/v0/b/financial-management-ddcdb.appspot.com/o/images%2Ffoto.jpg?alt=media&token=a8a480ea-b463-4c76-8be1-ef929af3e461",
                                "createdAt" to System.currentTimeMillis()
                            )
                            database.child("users").child(uid).setValue(userMap)
                                .addOnCompleteListener { dbTask ->
                                    if (dbTask.isSuccessful) {
                                        Toast.makeText(
                                            this,
                                            "Registration successful",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        addDefaultCategories(uid)
                                        startActivity(Intent(this, LoginActivity::class.java))
                                        finish()
                                    } else {
                                        Toast.makeText(
                                            this,
                                            "Failed to save user data",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        Log.w(TAG, "Failed to save user data", dbTask.exception)
                                    }
                                }
                        } else {
                            Toast.makeText(this, "User ID is null", Toast.LENGTH_SHORT).show()
                            Log.w(TAG, "User ID is null")
                        }
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext,
                            "Authentication failed.",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
        }
        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}