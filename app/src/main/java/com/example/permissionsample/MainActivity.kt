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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        request_permission_btn.setOnClickListener { requestCameraPermission() }
    }

    private fun requestCameraPermission() {
        val permission = Manifest.permission.CAMERA
        val sharedPreferences = getSharedPreferences("permission_settings", MODE_PRIVATE)
        val permissionIsGranted =
                ContextCompat.checkSelfPermission(this, permission) == PERMISSION_GRANTED
        if (permissionIsGranted) {
            toast("Permission is granted")
            return
        } else {
            val shouldShowRequestPermissionRationale =
                    ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
            val isPermissionRequested = sharedPreferences.getBoolean(permission, false)
            val isPermissionDeniedForever = !shouldShowRequestPermissionRationale && isPermissionRequested
            if (isPermissionDeniedForever) {
                toast("Permission is denied forever")
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(permission), CAMERA_PERMISSION_REQUEST_CODE)
                sharedPreferences.edit { putBoolean(permission, true) }
            }
        }
    }

    private fun toast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
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
}