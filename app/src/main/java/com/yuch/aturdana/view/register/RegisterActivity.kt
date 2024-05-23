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

    private fun setupAction() {
        binding.registerButton.setOnClickListener {
            val name = binding.edRegisterName.text?.toString()?:""
            val email = binding.edRegisterEmail.text?.toString()?:""
            val password = binding.edRegisterPassword.text?.toString()?:""

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "createUserWithEmail:success")
                        val uid = auth.currentUser!!.uid // Get UID of the newly created user
                        val userMap = hashMapOf(
                            "username" to name,
                            "email" to email,
                            "created_at" to System.currentTimeMillis()
                        )
                        database.child("users").child(uid).setValue(userMap)
                            .addOnCompleteListener { dbTask ->
                                if (dbTask.isSuccessful) {
                                    Toast.makeText(
                                        this,
                                        "Registration successful",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    startActivity(Intent(this, LoginActivity::class.java))
                                    finish()
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Failed to save user data",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
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