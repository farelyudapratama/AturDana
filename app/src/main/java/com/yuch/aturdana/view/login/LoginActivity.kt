package com.yuch.aturdana.view.login

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.yuch.aturdana.databinding.ActivityLoginBinding
import com.yuch.aturdana.view.main.MainActivity
import com.yuch.aturdana.view.register.RegisterActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        setupAction()
    }

    private fun setupAction() {
        binding.loginButton.setOnClickListener {
            val email = binding.edLoginEmail.text.toString()
            val password = binding.edLoginPassword.text.toString()

            if (email.isEmpty()) {
                binding.edLoginEmail.error = "Email harus diisi"
                binding.edLoginEmail.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                binding.edLoginPassword.error = "Password harus diisi"
                binding.edLoginPassword.requestFocus()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success")
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        // If sign in fails, display a message to the user.
                        try {
                            throw task.exception!!
                        } catch (e: FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(
                                baseContext,
                                "Email atau Password salah",
                                Toast.LENGTH_SHORT,
                            ).show()
                            binding.edLoginEmail.requestFocus()
                            binding.edLoginPassword.requestFocus()
                        } catch (e: Exception) {
                            Log.w(TAG, "signInWithEmail:failure", e)
                            Toast.makeText(
                                baseContext,
                                "Autentikasi gagal.",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
                }
        }
        binding.btnSignUp.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }
}