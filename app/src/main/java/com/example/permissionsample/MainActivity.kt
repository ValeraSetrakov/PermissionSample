package com.example.permissionsample

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.core.content.edit
import kotlinx.android.synthetic.main.activity_main.*

private const val CAMERA_PERMISSION_REQUEST_CODE = 111

class MainActivity : AppCompatActivity() {

    private val permissionSettings by lazy {
        getSharedPreferences("permission_settings", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        request_permission_btn.setOnClickListener {
            requestPermissionOr(Manifest.permission.CAMERA, CAMERA_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            val permissionIsGranted = grantResults.any { it == PERMISSION_GRANTED }
            if (permissionIsGranted) {
                toast("Permission is granted")
            } else {
                toast("Permission is denied")
            }
        }
    }

    private fun requestPermissionOr(permission: String, permissionCode: Int) {
        val statusOfPermission = getStatusOfPermission(permission)
        when(statusOfPermission) {
            PermissionStatus.GRANTED -> {
                toast("Permission is granted")
            }
            PermissionStatus.NOT_REQUESTED -> {
                toast("Permission is not requested")
                requestPermission(permission, permissionCode)
            }
            PermissionStatus.DENIED -> {
                toast("Permission was denied")
                requestPermission(permission, permissionCode)
            }
            else -> {
                toast("Permission is denied forever")
            }
        }
    }

    private fun requestPermission(permission: String, permissionCode: Int) {
        ActivityCompat.requestPermissions(this, arrayOf(permission), permissionCode)
        setPermissionRequested(permission)
    }

    private fun getStatusOfPermission(permission: String): PermissionStatus {
        return when {
            isPermissionGranted(permission) -> PermissionStatus.GRANTED
            !isPermissionRequested(permission) -> PermissionStatus.NOT_REQUESTED
            isPermissionDenied(permission) -> PermissionStatus.DENIED
            else -> PermissionStatus.DENIED_FOREVER
        }
    }

    private fun isPermissionDenied(permission: String): Boolean {
        return isShouldShowRequestPermissionRationale(permission) &&
                !isPermissionGranted(permission)
    }

    private fun isPermissionGranted(permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(this, permission) == PERMISSION_GRANTED
    }

    private fun isPermissionRequested(permission: String): Boolean {
        return permissionSettings.getBoolean(permission, false)
    }

    private fun setPermissionRequested(permission: String) {
        permissionSettings.edit { putBoolean(permission, true) }
    }

    private fun isShouldShowRequestPermissionRationale(permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
    }

    private fun toast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }
}

enum class PermissionStatus {
    GRANTED, NOT_REQUESTED, DENIED, DENIED_FOREVER
}