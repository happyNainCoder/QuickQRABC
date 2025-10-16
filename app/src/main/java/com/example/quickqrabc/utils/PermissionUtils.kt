package com.example.quickqrabc.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionUtils {
    
    const val CAMERA_PERMISSION_REQUEST_CODE = 100
    const val STORAGE_PERMISSION_REQUEST_CODE = 101
    
    // Required permissions
    val CAMERA_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    
    val STORAGE_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    } else {
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
    
    /**
     * Check if camera permission is granted
     */
    fun isCameraPermissionGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Check if storage permissions are granted
     */
    fun isStoragePermissionGranted(context: Context): Boolean {
        return STORAGE_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Request camera permission
     */
    fun requestCameraPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            CAMERA_PERMISSIONS,
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }
    
    /**
     * Request storage permissions
     */
    fun requestStoragePermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            STORAGE_PERMISSIONS,
            STORAGE_PERMISSION_REQUEST_CODE
        )
    }
    
    /**
     * Check if permission should show rationale
     */
    fun shouldShowCameraPermissionRationale(activity: Activity): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            Manifest.permission.CAMERA
        )
    }
    
    /**
     * Check if storage permission should show rationale
     */
    fun shouldShowStoragePermissionRationale(activity: Activity): Boolean {
        return STORAGE_PERMISSIONS.any { permission ->
            ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        }
    }
    
    /**
     * Handle permission result
     */
    fun handlePermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        onCameraGranted: () -> Unit = {},
        onCameraDenied: () -> Unit = {},
        onStorageGranted: () -> Unit = {},
        onStorageDenied: () -> Unit = {}
    ) {
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onCameraGranted()
                } else {
                    onCameraDenied()
                }
            }
            STORAGE_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    onStorageGranted()
                } else {
                    onStorageDenied()
                }
            }
        }
    }
}
