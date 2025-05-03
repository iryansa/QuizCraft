package com.DreamDev.quizcraft

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.DreamDev.quizcraft.login.Login
import com.DreamDev.quizcraft.login.Signup

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Delay for 5 seconds and navigate to Login screen
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, Signup::class.java)
            startActivity(intent)
            finish()
        }, 3000) // 5000 milliseconds = 5 seconds
    }
}