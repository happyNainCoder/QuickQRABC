package com.example.quickqrabc.ui

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.quickqrabc.R
import com.example.quickqrabc.databinding.ActivityQrScannerBinding
import com.example.quickqrabc.viewmodel.QRViewModel
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.camera.CameraSettings

class QRScannerActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityQrScannerBinding
    private lateinit var viewModel: QRViewModel
    private var isFlashOn = false
    private var isAutoCopyEnabled = false
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(this, "Camera permission is required to scan QR codes", Toast.LENGTH_LONG).show()
            finish()
        }
    }
    
    private val callback = BarcodeCallback { result ->
        handleScanResult(result)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(this)[QRViewModel::class.java]
        
        setupToolbar()
        setupClickListeners()
        checkCameraPermission()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun setupClickListeners() {
        binding.btnFlash.setOnClickListener {
            toggleFlash()
        }
        
        binding.btnAutoCopy.setOnClickListener {
            toggleAutoCopy()
        }
        
        binding.btnCopy.setOnClickListener {
            copyToClipboard(binding.resultContent.text.toString())
        }
        
        binding.btnShare.setOnClickListener {
            shareText(binding.resultContent.text.toString())
        }
    }
    
    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                startCamera()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
    
    private fun startCamera() {
        binding.barcodeView.decodeContinuous(callback)
        binding.barcodeView.resume()
    }
    
    private fun handleScanResult(result: BarcodeResult) {
        val scannedText = result.text
        
        // Save to history
        viewModel.insertHistory("scan", scannedText)
        
        // Show result
        binding.resultContent.text = scannedText
        binding.resultCard.visibility = android.view.View.VISIBLE
        
        // Auto copy if enabled
        if (isAutoCopyEnabled) {
            copyToClipboard(scannedText)
        }
        
        // Pause scanning to show result
        binding.barcodeView.pause()
        
        // Resume scanning after 3 seconds
        binding.root.postDelayed({
            binding.resultCard.visibility = android.view.View.GONE
            binding.barcodeView.resume()
        }, 3000)
    }
    
    private fun toggleFlash() {
        isFlashOn = !isFlashOn
        if (isFlashOn) {
            binding.barcodeView.setTorchOn()
            binding.btnFlash.setIconResource(R.drawable.ic_flash_on)
        } else {
            binding.barcodeView.setTorchOff()
            binding.btnFlash.setIconResource(R.drawable.ic_flash_off)
        }
    }
    
    private fun toggleAutoCopy() {
        isAutoCopyEnabled = !isAutoCopyEnabled
        binding.btnAutoCopy.text = if (isAutoCopyEnabled) "Auto Copy: ON" else "Auto Copy: OFF"
    }
    
    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("QR Code", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }
    
    private fun shareText(text: String) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "Share QR Code Content"))
    }
    
    override fun onResume() {
        super.onResume()
        binding.barcodeView.resume()
    }
    
    override fun onPause() {
        super.onPause()
        binding.barcodeView.pause()
    }
}
