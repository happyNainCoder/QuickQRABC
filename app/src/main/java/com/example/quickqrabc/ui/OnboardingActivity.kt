package com.example.quickqrabc.ui

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.quickqrabc.MainActivity
import com.example.quickqrabc.R
import com.example.quickqrabc.databinding.ActivityOnboardingBinding
import com.example.quickqrabc.utils.PermissionUtils

class OnboardingActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var sharedPreferences: SharedPreferences
    private var currentPage = 0
    
    private val onboardingData = listOf(
        OnboardingSlide(
            image = R.drawable.ic_qr_scanner,
            title = "Welcome to QuickQR",
            description = "Your all-in-one QR code solution. Scan, generate, and manage QR codes with ease.",
            isPermissionSlide = false
        ),
        OnboardingSlide(
            image = R.drawable.ic_qr_scanner,
            title = "Scan QR Codes Instantly",
            description = "Point your camera at any QR code and get instant results. Copy, share, or save to history.",
            isPermissionSlide = true,
            permissionTitle = "Camera Permission Required",
            permissionDescription = "We need camera access to scan QR codes. Your privacy is protected - we don't store or share any images."
        ),
        OnboardingSlide(
            image = R.drawable.ic_qr_generate,
            title = "Generate QR Codes",
            description = "Create QR codes for text, URLs, Wi-Fi, contacts, and more. Save them to your gallery or share instantly.",
            isPermissionSlide = true,
            permissionTitle = "Storage Permission (Optional)",
            permissionDescription = "Storage access allows you to save generated QR codes to your gallery. You can skip this and still use the app."
        ),
        OnboardingSlide(
            image = R.drawable.ic_history,
            title = "Track Your History",
            description = "All your scanned and generated QR codes are automatically saved. Access them anytime from the history section.",
            isPermissionSlide = false
        )
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check if onboarding was already completed
        sharedPreferences = getSharedPreferences("QuickQR_Prefs", MODE_PRIVATE)
        if (sharedPreferences.getBoolean("onboarding_completed", false)) {
            navigateToMain()
            return
        }
        
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViewPager()
        setupClickListeners()
        updateUI()
    }
    
    private fun setupViewPager() {
        val adapter = OnboardingPagerAdapter(this)
        binding.viewPager.adapter = adapter
        
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentPage = position
                updateUI()
            }
        })
        
        setupIndicators()
    }
    
    private fun setupIndicators() {
        val indicators = arrayOfNulls<ImageView>(onboardingData.size)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(8, 0, 8, 0)
        
        for (i in indicators.indices) {
            indicators[i] = ImageView(applicationContext)
            indicators[i]?.let {
                it.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_inactive
                    )
                )
                it.layoutParams = layoutParams
                binding.indicatorContainer.addView(it)
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.btnNext.setOnClickListener {
            when (currentPage) {
                1 -> { // Camera permission slide
                    if (PermissionUtils.isCameraPermissionGranted(this)) {
                        // Permission already granted, move to next
                        binding.viewPager.currentItem = currentPage + 1
                    } else {
                        // Request camera permission
                        requestCameraPermission()
                    }
                }
                2 -> { // Storage permission slide
                    if (PermissionUtils.isStoragePermissionGranted(this)) {
                        // Permission already granted, move to next
                        binding.viewPager.currentItem = currentPage + 1
                    } else {
                        // Ask if user wants to grant storage permission
                        showStoragePermissionDialog()
                    }
                }
                else -> {
                    if (currentPage < onboardingData.size - 1) {
                        binding.viewPager.currentItem = currentPage + 1
                    } else {
                        completeOnboarding()
                    }
                }
            }
        }
        
        binding.btnSkip.setOnClickListener {
            completeOnboarding()
        }
    }
    
    private fun requestCameraPermission() {
        if (PermissionUtils.shouldShowCameraPermissionRationale(this)) {
            AlertDialog.Builder(this)
                .setTitle("Camera Permission Required")
                .setMessage("QuickQR needs camera access to scan QR codes. This is essential for the app to work properly.")
                .setPositiveButton("Grant Permission") { _, _ ->
                    PermissionUtils.requestCameraPermission(this)
                }
                .setNegativeButton("Skip") { _, _ ->
                    binding.viewPager.currentItem = currentPage + 1
                }
                .show()
        } else {
            PermissionUtils.requestCameraPermission(this)
        }
    }
    
    private fun showStoragePermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Storage Permission (Optional)")
            .setMessage("Storage permission allows you to save QR codes to your gallery. You can skip this and still use all other features.")
            .setPositiveButton("Grant Permission") { _, _ ->
                PermissionUtils.requestStoragePermission(this)
            }
            .setNegativeButton("Skip") { _, _ ->
                binding.viewPager.currentItem = currentPage + 1
            }
            .setNeutralButton("Learn More") { _, _ ->
                showStoragePermissionDetails()
            }
            .show()
    }
    
    private fun showStoragePermissionDetails() {
        AlertDialog.Builder(this)
            .setTitle("Why Storage Permission?")
            .setMessage("• Save generated QR codes to your gallery\n• Export QR history as files\n• Share QR codes as images\n\nWithout this permission, you can still:\n• Scan QR codes\n• Generate QR codes\n• View history\n• Copy and share text content")
            .setPositiveButton("Grant Permission") { _, _ ->
                PermissionUtils.requestStoragePermission(this)
            }
            .setNegativeButton("Skip") { _, _ ->
                binding.viewPager.currentItem = currentPage + 1
            }
            .show()
    }
    
    private fun updateUI() {
        // Update indicators
        for (i in 0 until binding.indicatorContainer.childCount) {
            val imageView = binding.indicatorContainer.getChildAt(i) as ImageView
            if (i == currentPage) {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.indicator_active)
                )
            } else {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.indicator_inactive)
                )
            }
        }
        
        // Update button text
        if (currentPage == onboardingData.size - 1) {
            binding.btnNext.text = "Get Started"
            binding.btnSkip.visibility = View.GONE
        } else {
            binding.btnNext.text = "Next"
            binding.btnSkip.visibility = View.VISIBLE
        }
    }
    
    private fun completeOnboarding() {
        sharedPreferences.edit()
            .putBoolean("onboarding_completed", true)
            .apply()
        
        navigateToMain()
    }
    
    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
    
    private inner class OnboardingPagerAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
        
        override fun getItemCount(): Int = onboardingData.size
        
        override fun createFragment(position: Int): Fragment {
            return OnboardingSlideFragment.newInstance(onboardingData[position])
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
            onCameraGranted = {
                Toast.makeText(this, "Camera permission granted! You can now scan QR codes.", Toast.LENGTH_SHORT).show()
                binding.viewPager.currentItem = currentPage + 1
            },
            onCameraDenied = {
                showCameraPermissionDeniedDialog()
            },
            onStorageGranted = {
                Toast.makeText(this, "Storage permission granted! You can now save QR codes to gallery.", Toast.LENGTH_SHORT).show()
                binding.viewPager.currentItem = currentPage + 1
            },
            onStorageDenied = {
                showStoragePermissionDeniedDialog()
            }
        )
    }
    
    private fun showCameraPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Camera Permission Skipped")
            .setMessage("No problem! You can still:\n\n• Generate QR codes\n• View scan history\n• Enable camera later in Settings")
            .setPositiveButton("Continue") { _, _ ->
                binding.viewPager.currentItem = currentPage + 1
            }
            .setNegativeButton("Enable in Settings") { _, _ ->
                openAppSettings()
            }
            .show()
    }
    
    private fun showStoragePermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Storage Permission Skipped")
            .setMessage("That's okay! You can still:\n\n• Generate and view QR codes\n• Share QR codes directly\n• Scan QR codes\n• Save to gallery later via Settings")
            .setPositiveButton("Continue") { _, _ ->
                binding.viewPager.currentItem = currentPage + 1
            }
            .setNegativeButton("Enable in Settings") { _, _ ->
                openAppSettings()
            }
            .show()
    }
    
    private fun openAppSettings() {
        try {
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = android.net.Uri.fromParts("package", packageName, null)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Cannot open settings", Toast.LENGTH_SHORT).show()
        }
        binding.viewPager.currentItem = currentPage + 1
    }
    
    data class OnboardingSlide(
        val image: Int,
        val title: String,
        val description: String,
        val isPermissionSlide: Boolean = false,
        val permissionTitle: String = "",
        val permissionDescription: String = ""
    ) : java.io.Serializable
}
