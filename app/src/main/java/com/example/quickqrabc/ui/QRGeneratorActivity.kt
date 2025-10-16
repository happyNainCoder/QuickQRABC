package com.example.quickqrabc.ui

import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.quickqrabc.R
import com.example.quickqrabc.databinding.ActivityQrGeneratorBinding
import com.example.quickqrabc.utils.PermissionUtils
import com.example.quickqrabc.viewmodel.QRViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class QRGeneratorActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityQrGeneratorBinding
    private lateinit var viewModel: QRViewModel
    private var generatedQRBitmap: Bitmap? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrGeneratorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        viewModel = ViewModelProvider(this)[QRViewModel::class.java]
        
        setupToolbar()
        setupClickListeners()
        setupInputTypeSelection()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun setupClickListeners() {
        binding.btnGenerate.setOnClickListener {
            generateQRCode()
        }
        
        binding.btnSaveToGallery.setOnClickListener {
            checkStoragePermissionAndSave()
        }
        
        binding.btnShareQR.setOnClickListener {
            shareQRCode()
        }
    }
    
    private fun setupInputTypeSelection() {
        binding.chipGroupType.setOnCheckedStateChangeListener { _, checkedIds ->
            when (checkedIds.firstOrNull()) {
                R.id.chipText -> setupTextInput()
                R.id.chipUrl -> setupUrlInput()
                R.id.chipWifi -> setupWifiInput()
                R.id.chipContact -> setupContactInput()
            }
        }
        
        // Default to text input
        setupTextInput()
    }
    
    private fun setupTextInput() {
        binding.inputContainer.removeAllViews()
        layoutInflater.inflate(R.layout.input_text, binding.inputContainer, true)
    }
    
    private fun setupUrlInput() {
        binding.inputContainer.removeAllViews()
        layoutInflater.inflate(R.layout.input_url, binding.inputContainer, true)
    }
    
    private fun setupWifiInput() {
        binding.inputContainer.removeAllViews()
        layoutInflater.inflate(R.layout.input_wifi, binding.inputContainer, true)
    }
    
    private fun setupContactInput() {
        binding.inputContainer.removeAllViews()
        layoutInflater.inflate(R.layout.input_contact, binding.inputContainer, true)
    }
    
    private fun generateQRCode() {
        val content = getInputContent()
        
        if (content.isBlank()) {
            Toast.makeText(this, "Please enter content to generate QR code", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            
            generatedQRBitmap = bitmap
            binding.imageViewQR.setImageBitmap(bitmap)
            binding.qrCodeCard.visibility = View.VISIBLE
            
            // Save to history
            viewModel.insertHistory("generate", content)
            
            Toast.makeText(this, "QR Code generated successfully", Toast.LENGTH_SHORT).show()
            
        } catch (e: WriterException) {
            Toast.makeText(this, "Error generating QR code: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun getInputContent(): String {
        return when (binding.chipGroupType.checkedChipId) {
            R.id.chipText -> {
                binding.inputContainer.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editTextContent)?.text?.toString() ?: ""
            }
            R.id.chipUrl -> {
                val urlInput = binding.inputContainer.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editTextUrl)
                urlInput?.text?.toString() ?: ""
            }
            R.id.chipWifi -> {
                val ssidInput = binding.inputContainer.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editTextSSID)
                val passwordInput = binding.inputContainer.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editTextPassword)
                val securitySpinner = binding.inputContainer.findViewById<android.widget.Spinner>(R.id.spinnerSecurity)
                
                val ssid = ssidInput?.text?.toString() ?: ""
                val password = passwordInput?.text?.toString() ?: ""
                val security = securitySpinner?.selectedItem?.toString() ?: "WPA"
                
                "WIFI:T:$security;S:$ssid;P:$password;;"
            }
            R.id.chipContact -> {
                val nameInput = binding.inputContainer.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editTextName)
                val phoneInput = binding.inputContainer.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editTextPhone)
                val emailInput = binding.inputContainer.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editTextEmail)
                
                val name = nameInput?.text?.toString() ?: ""
                val phone = phoneInput?.text?.toString() ?: ""
                val email = emailInput?.text?.toString() ?: ""
                
                "BEGIN:VCARD\nVERSION:3.0\nFN:$name\nTEL:$phone\nEMAIL:$email\nEND:VCARD"
            }
            else -> binding.inputContainer.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editTextContent)?.text?.toString() ?: ""
        }
    }
    
    private fun checkStoragePermissionAndSave() {
        when {
            PermissionUtils.isStoragePermissionGranted(this) -> {
                saveQRToGallery()
            }
            PermissionUtils.shouldShowStoragePermissionRationale(this) -> {
                showStoragePermissionRationale()
            }
            else -> {
                PermissionUtils.requestStoragePermission(this)
            }
        }
    }
    
    private fun showStoragePermissionRationale() {
        AlertDialog.Builder(this)
            .setTitle("Storage Permission Required")
            .setMessage("Storage permission is needed to save QR codes to your gallery. Please grant permission to continue.")
            .setPositiveButton("Grant Permission") { _, _ ->
                PermissionUtils.requestStoragePermission(this)
            }
            .setNegativeButton("Cancel") { _, _ ->
                Toast.makeText(this, "Permission denied. Cannot save QR code.", Toast.LENGTH_SHORT).show()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun saveQRToGallery() {
        generatedQRBitmap?.let { bitmap ->
            try {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, "QuickQR_${System.currentTimeMillis()}.png")
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/QuickQR")
                }
                
                val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let {
                    contentResolver.openOutputStream(it)?.use { outputStream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    }
                    Toast.makeText(this, "QR Code saved to gallery", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                Toast.makeText(this, "Error saving QR code: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun shareQRCode() {
        generatedQRBitmap?.let { bitmap ->
            try {
                val cachePath = File(cacheDir, "images")
                cachePath.mkdirs()
                val file = File(cachePath, "qr_code.png")
                val fileOutputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
                fileOutputStream.close()
                
                val contentUri = androidx.core.content.FileProvider.getUriForFile(
                    this,
                    "${packageName}.fileprovider",
                    file
                )
                
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    type = "image/png"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                startActivity(Intent.createChooser(shareIntent, "Share QR Code"))
                
            } catch (e: IOException) {
                Toast.makeText(this, "Error sharing QR code: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
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
            onStorageGranted = {
                saveQRToGallery()
            },
            onStorageDenied = {
                Toast.makeText(this, "Storage permission is required to save QR codes", Toast.LENGTH_LONG).show()
            }
        )
    }
}
