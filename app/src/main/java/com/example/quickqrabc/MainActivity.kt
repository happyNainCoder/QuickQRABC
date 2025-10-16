package com.example.quickqrabc

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.quickqrabc.databinding.ActivityMainBinding
import com.example.quickqrabc.ui.HistoryActivity
import com.example.quickqrabc.ui.QRGeneratorActivity
import com.example.quickqrabc.ui.QRScannerActivity

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupClickListeners()
    }
    
    private fun setupClickListeners() {
        binding.btnScanQR.setOnClickListener {
            startActivity(Intent(this, QRScannerActivity::class.java))
        }
        
        binding.btnGenerateQR.setOnClickListener {
            startActivity(Intent(this, QRGeneratorActivity::class.java))
        }
        
        binding.btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }
}