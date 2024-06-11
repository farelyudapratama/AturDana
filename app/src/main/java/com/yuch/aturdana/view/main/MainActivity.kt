package com.yuch.aturdana.view.main

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.yuch.aturdana.R
import com.yuch.aturdana.data.ReminderCheckService
import com.yuch.aturdana.databinding.ActivityMainBinding
import com.yuch.aturdana.view.AddActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        setupAction()

        val serviceIntent = Intent(this, ReminderCheckService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        createNotificationChannels()

    }
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "BUDGET_NOTIFICATIONS",
                "Notifikasi Anggaran",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi untuk pembaruan anggaran"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
    private fun setupAction() {

        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        binding.apply {
            ivNav1.setOnClickListener {
                navController.navigate(R.id.homeFragment)
            }
            ivNav2.setOnClickListener {
                navController.navigate(R.id.budgetFragment)
            }
            fabAdd.setOnClickListener {
                startActivity(Intent(this@MainActivity, AddActivity::class.java))
                finish()
            }
            ivNav4.setOnClickListener {
                navController.navigate(R.id.reminderFragment)
            }
            ivNav5.setOnClickListener {
                navController.navigate(R.id.akunFragment)
            }
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment -> updateIconColors(R.id.ivNav1)
                R.id.budgetFragment -> updateIconColors(R.id.ivNav2)
                R.id.reminderFragment -> updateIconColors(R.id.ivNav4)
                R.id.akunFragment -> updateIconColors(R.id.ivNav5)
            }
        }
    }
    private fun updateIconColors(activeId: Int) {
        val activeColor = ContextCompat.getColor(this, R.color.active_color)
        val inactiveColor = ContextCompat.getColor(this, R.color.inactive_color)

        binding.apply {
            ivNav1.setColorFilter(if (activeId == R.id.ivNav1) activeColor else inactiveColor)
            ivNav2.setColorFilter(if (activeId == R.id.ivNav2) activeColor else inactiveColor)
            ivNav4.setColorFilter(if (activeId == R.id.ivNav4) activeColor else inactiveColor)
            ivNav5.setColorFilter(if (activeId == R.id.ivNav5) activeColor else inactiveColor)
        }
    }
}