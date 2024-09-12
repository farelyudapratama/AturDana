package com.yuch.aturdana.view.main

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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

    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        setupAction()

        if (isNetworkAvailable()) {
            val serviceIntent = Intent(this, ReminderCheckService::class.java)
            startService(serviceIntent)
            createNotificationChannels()
        } else {
            showNoInternetDialog()
        }
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            finishAffinity() // Menutup seluruh aplikasi
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Tekan sekali lagi untuk keluar", Toast.LENGTH_SHORT).show()

        // Reset kondisi setelah 2 detik
        Handler(Looper.getMainLooper()).postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)
    }



    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities =
                connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            return networkInfo != null && networkInfo.isConnected
        }
    }

    private fun showNoInternetDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Tidak Ada Koneksi Internet")
            .setMessage("Aplikasi memerlukan koneksi internet untuk melanjutkan.")
            .setPositiveButton("Keluar") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setCancelable(false)
            .show()
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