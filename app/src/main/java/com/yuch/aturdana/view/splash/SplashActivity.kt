package com.yuch.aturdana.view.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.yuch.aturdana.databinding.ActivitySplashBinding
import com.yuch.aturdana.view.login.LoginActivity

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        Handler(Looper.getMainLooper()).postDelayed({
//            viewModel.getSession().observe(this) { user ->
//                if (!user.isLogin) {
//                    Log.d("token", "onCreate: ${user.token}")
//                    startActivity(Intent(this, ActLogin::class.java))
//                    finish()
//                } else {
//                    Log.d("token", "onCreate: ${user.token}")
//                    startActivity(Intent(this, MainActivity::class.java))
//                    finish()
//                }
//            }
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 1000)
    }
}