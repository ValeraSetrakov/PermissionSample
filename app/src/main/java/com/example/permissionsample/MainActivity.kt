package com.example.permissionsample

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.activity_main.*


private const val CAMERA_PERMISSION_REQUEST_CODE = 111

class MainActivity : AppCompatActivity(), SimpleDialog.SimpleDialogListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        request_permission_btn.setOnClickListener {
            requestPermissionByState(Manifest.permission.CAMERA, CAMERA_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            val permission = permissions.first()
            val permissionIsGranted = grantResults.all { it == PERMISSION_GRANTED }
            if (permissionIsGranted) {
                toast("$permission is granted")
                // do something if permission is granted
            } else {
                if (isPermissionDenied(this, permission)) {
                    toast("$permission is denied")
                } else {
                    // do something if permission is denied forever
                    toast("$permission is denied forever")
                    openAppSettingScreen()
                }
            }
        }
    }

    override fun onPositiveClick(permission: String) {
        requestPermission(permission, CAMERA_PERMISSION_REQUEST_CODE)
    }

    /**
     * Запрос разрешения с учетом его состояния [PermissionState]
     * @param permission - запрашиваемое разрешение
     * @param permissionCode - код результата запрашиваемого разрешения
     */
    private fun requestPermissionByState(permission: String, permissionCode: Int) {
        when (getStateOfPermission(this, permission)) {
            PermissionState.GRANTED -> {
                toast("$permission is granted")
                // do something if permission is granted
            }
            PermissionState.DENIED -> {
                showPermissionRationale(permission)
            }
            else -> {
                requestPermission(permission, permissionCode)
            }
        }
    }

    private fun requestPermission(permission: String, permissionCode: Int) {
        ActivityCompat.requestPermissions(this, arrayOf(permission), permissionCode)
    }

    private fun showPermissionRationale(permission: String) {
        SimpleDialog.show(supportFragmentManager, permission)
    }

    private fun openAppSettingScreen() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
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
     * Пользователь отклонил разрешение
     */
    DENIED,

    /**
     * Невозможно определить состояние
     */
    UNDEFINED
}

/**
 * Определяет состояние разрешение
 * @param permission - проверяемое разрешение
 *
 * @return состояние разрешения
 */
private fun getStateOfPermission(
    activity: Activity,
    permission: String
): PermissionState {
    return when {
        isPermissionGranted(activity, permission) -> PermissionState.GRANTED
        isPermissionDenied(activity, permission) -> PermissionState.DENIED
        else -> PermissionState.UNDEFINED
    }
}

/**
 * Отказано ли в выдаче разрешения
 * @param permission - проверяемое разрешение
 *
 * @return true, если разрешение было запрошено, но не выдано, false, если разрешение было запрошено
 * и выдано, либо, если разрешение было запрошено, и отклонено навсегда
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
 * Нужно ли объяснить пользователю необходимость выдачи разрешения
 * @param permission - проверяемое разрешение
 */
private fun isShouldShowRequestPermissionRationale(
    activity: Activity,
    permission: String
): Boolean {
    return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
}

class SimpleDialog : DialogFragment() {

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