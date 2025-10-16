package com.example.quickqrabc.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.quickqrabc.R
import com.example.quickqrabc.databinding.ActivityQrScannerBinding
import com.example.quickqrabc.utils.PermissionUtils
import com.example.quickqrabc.viewmodel.QRViewModel
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult

class QRScannerActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityQrScannerBinding
    private lateinit var viewModel: QRViewModel
    private var isFlashOn = false
    private var isAutoCopyEnabled = false
    
    
    private val callback = BarcodeCallback { result ->
        handleScanResult(result)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            binding = ActivityQrScannerBinding.inflate(layoutInflater)
            setContentView(binding.root)
            
            viewModel = ViewModelProvider(this)[QRViewModel::class.java]
            
            setupToolbar()
            setupClickListeners()
            checkCameraPermission()
        } catch (e: Exception) {
            Toast.makeText(this, "Error initializing camera: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
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
            PermissionUtils.isCameraPermissionGranted(this) -> {
                startCamera()
            }
            PermissionUtils.shouldShowCameraPermissionRationale(this) -> {
                showPermissionRationale()
            }
            else -> {
                PermissionUtils.requestCameraPermission(this)
            }
        }
    }
    
    private fun showPermissionRationale() {
        AlertDialog.Builder(this)
            .setTitle("Camera Permission Required")
            .setMessage("Camera permission is needed to scan QR codes. Please grant permission to continue.")
            .setPositiveButton("Grant Permission") { _, _ ->
                PermissionUtils.requestCameraPermission(this)
            }
            .setNegativeButton("Cancel") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun startCamera() {
        try {
            binding.barcodeView.decodeContinuous(callback)
            binding.barcodeView.resume()
        } catch (e: Exception) {
            Toast.makeText(this, "Error starting camera: ${e.message}", Toast.LENGTH_LONG).show()
        }
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
        try {
            isFlashOn = !isFlashOn
            val cameraSettings = binding.barcodeView.cameraSettings
            if (isFlashOn) {
                cameraSettings.requestedCameraId = 0 // Back camera
                binding.barcodeView.setTorch(true)
                binding.btnFlash.setIconResource(R.drawable.ic_flash_on)
            } else {
                binding.barcodeView.setTorch(false)
                binding.btnFlash.setIconResource(R.drawable.ic_flash_off)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Flash not available", Toast.LENGTH_SHORT).show()
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
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        PermissionUtils.handlePermissionResult(
            requestCode = requestCode,
            permissions = permissions,
            grantResults = grantResults,
            onCameraGranted = {
                startCamera()
            },
            onCameraDenied = {
                Toast.makeText(this, "Camera permission is required to scan QR codes", Toast.LENGTH_LONG).show()
                finish()
            }
        )
    }
}
