package com.yuch.aturdana.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.yuch.aturdana.R
import com.yuch.aturdana.databinding.ActivityGuideBinding
import com.yuch.aturdana.view.main.MainActivity

class GuideActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGuideBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityGuideBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Panduan Pengguna"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                startActivity(Intent(this@GuideActivity, MainActivity::class.java))
                finish()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }
    override fun onSupportNavigateUp(): Boolean {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
        return true
    }
}