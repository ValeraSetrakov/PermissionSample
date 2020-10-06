package com.example.permissionsample

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.activity_main.*

private const val CAMERA_PERMISSION_REQUEST_CODE = 111

class MainActivity : AppCompatActivity(), SimpleDialog.SimpleDialogListener {

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

    override fun onPositiveClick(permission: String) {
        requestPermission(permission, CAMERA_PERMISSION_REQUEST_CODE)
    }

    private fun requestPermissionOr(permission: String, permissionCode: Int) {
        val statusOfPermission = getStateOfPermission(this, permissionSettings, permission)
        when(statusOfPermission) {
            PermissionState.GRANTED -> {
                toast("Permission is granted")
            }
            PermissionState.NOT_REQUESTED -> {
                toast("Permission is not requested")
                requestPermission(permission, permissionCode)
            }
            PermissionState.DENIED -> {
                toast("Permission was denied")
                showPermissionRationale(permission)
            }
            else -> {
                toast("Permission is denied forever")
            }
        }
    }

    private fun requestPermission(permission: String, permissionCode: Int) {
        ActivityCompat.requestPermissions(this, arrayOf(permission), permissionCode)
        setPermissionRequested(permissionSettings, permission)
    }

    private fun showPermissionRationale(permission: String) {
        SimpleDialog.show(supportFragmentManager, permission)
    }

    private fun toast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Состояние разрешения
 */
enum class PermissionState {
    /**
     * Пользователь выдал разрешение
     */
    GRANTED,
    /**
     * Пользователь не запрашивал разрешение
     */
    NOT_REQUESTED,
    /**
     * Пользователь отклонил разрешение
     */
    DENIED,
    /**
     * Пользователь навсегда отклонил разрешение
     */
    DENIED_FOREVER
}

/**
 * Определяет состояние разрешение
 * @param permission - проверяемое разрешение
 *
 * @return состояние разрешения
 */
private fun getStateOfPermission(
    activity: Activity,
    permissionSettings: SharedPreferences,
    permission: String
): PermissionState {
    return when {
        !isPermissionRequested(permissionSettings, permission) -> PermissionState.NOT_REQUESTED
        isPermissionGranted(activity, permission) -> PermissionState.GRANTED
        isPermissionDenied(activity, permission) -> PermissionState.DENIED
        else -> PermissionState.DENIED_FOREVER
    }
}

/**
 * Отказано ли в выдаче разрешения
 * @param permission - проверяемое разрешение
 */
private fun isPermissionDenied(activity: Activity, permission: String): Boolean {
    return isShouldShowRequestPermissionRationale(activity, permission)
}

/**
 * Выдано ли разрешение
 * @param permission - проверяемое разрешение
 */
private fun isPermissionGranted(context: Context, permission: String): Boolean {
    return ActivityCompat.checkSelfPermission(context, permission) == PERMISSION_GRANTED
}

/**
 * Запрашивалось ли разрешение ранее
 * @param permission - проверяемое разрешение
 */
private fun isPermissionRequested(permissionSettings: SharedPreferences, permission: String): Boolean {
    return permissionSettings.getBoolean(permission, false)
}

/**
 * Фиксация того, что разрешение запрошено
 */
private fun setPermissionRequested(permissionSettings: SharedPreferences, permission: String) {
    permissionSettings.edit { putBoolean(permission, true) }
}

/**
 * Нужно ли объяснить пользователю необходимость выдачи разрешения
 * @param permission - проверяемое разрешение
 */
private fun isShouldShowRequestPermissionRationale(activity: Activity, permission: String): Boolean {
    return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
}

class SimpleDialog: DialogFragment() {

    companion object {
        private const val PERMISSION_KEY = "PERMISSION_KEY"
        fun show(fragmentManager: FragmentManager, permission: String) {
            val dialog = SimpleDialog().apply {
                arguments = bundleOf(PERMISSION_KEY to permission)
            }
            dialog.show(fragmentManager, "RATIONAL_DIALOG_TAG")
        }
    }

    private var clickListener: SimpleDialogListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (activity is SimpleDialogListener) {
            clickListener = activity as SimpleDialogListener
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setTitle("Permission rationale")
            .setMessage("Some rationale")
            .setPositiveButton("Ok") { dialog, wich ->
                val permission = arguments?.getString(PERMISSION_KEY).orEmpty()
                clickListener?.onPositiveClick(permission)
            }.setNegativeButton("Cancel") { dialog, wich ->
                dialog.dismiss()
            }.create()
    }

    interface SimpleDialogListener {
        fun onPositiveClick(permission: String)
    }
}