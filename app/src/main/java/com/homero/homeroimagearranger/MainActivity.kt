package com.homero.homeroimagearranger

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.homero.homeroimagearranger.screen.MainScreen
import com.homero.homeroimagearranger.ui.theme.HomeroImageArrangerTheme
import java.io.File
import java.util.jar.Manifest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HomeroImageArrangerTheme {
                /* Create a surface container using the 'background' color from the theme */
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    /* Check for permissions inside the application */
                    if (checkPermission()){
                        Log.d(TAG, "onCreate: Permission already granted, create folder")
                        createFolder()
                    }
                    else{
                        Log.d(TAG, "onCreate: Permission was not granted, request")
                        requestPermission()
                    }
                    /* Lock the screen rotation into portrait mode */
                    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                    /* Load the main screen of the application */
                    MainScreen()
                }
            }
        }
    }


    @Composable
    fun LockScreenOrientation(orientation: Int) {
        /* This function allows the user to lock the screen orientation */
        val context = LocalContext.current
        DisposableEffect(orientation) {
            val activity = context.findActivity() ?: return@DisposableEffect onDispose {}
            val originalOrientation = activity.requestedOrientation
            activity.requestedOrientation = orientation
            onDispose {
                activity.requestedOrientation = originalOrientation
            }
        }
    }

    private fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

    /* Permissions */

    private companion object{
        /* Stores the permission request constants */
        private const val STORAGE_PERMISSION_CODE = 100
        private const val TAG = "PERMISSION_TAG"
    }

    private fun createFolder(){
        /* Creates a folder in the assigned directory */
        val directory = "Documents"
        val folderName = "HomeroImageArranger"
        val file = File("${Environment.getExternalStorageDirectory()}/$directory/$folderName")
        val folderCreated = file.mkdir()
        if (folderCreated) {
            toast("Folder Created: ${file.absolutePath}")
        } else {
            toast("Folder not created....")
        }
    }

    private fun requestPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            /* Requests for permission if the application has none */
            try {
                Log.d(TAG, "requestPermission: try")
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                val uri = Uri.fromParts("package", this.packageName, null)
                intent.data = uri
                storageActivityResultLauncher.launch(intent)
            }
            catch (e: Exception){
                Log.e(TAG, "requestPermission: ", e)
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                storageActivityResultLauncher.launch(intent)
            }
        }

    }

    private val storageActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        /* Executes the result of the request permission function */
        Log.d(TAG, "storageActivityResultLauncher: ")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            /* Android is 11(R) or above */
            if (Environment.isExternalStorageManager()){
                /* Manage External Storage Permission is granted */
                Log.d(TAG, "storageActivityResultLauncher: Manage External Storage Permission is granted")
                createFolder()
            }
            else{
                /* Manage External Storage Permission is denied.... */
                Log.d(TAG, "storageActivityResultLauncher: Manage External Storage Permission is denied....")
                toast("Manage External Storage Permission is denied....")
            }
        }

    }

    private fun checkPermission(): Boolean {
        /* If Android is 11(R) or above */
        return Environment.isExternalStorageManager()
    }


    /* Function deprecated consider replacing it */
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE){
            if (grantResults.isNotEmpty()){
                /* Check if permissions are granted or not */
                val write = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val read = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (write && read){
                    /* External Storage Permission granted */
                    Log.d(TAG, "onRequestPermissionsResult: External Storage Permission granted")
                    createFolder()
                }
                else{
                    /* External Storage Permission denied... */
                    Log.d(TAG, "onRequestPermissionsResult: External Storage Permission denied...")
                    toast("External Storage Permission denied...")
                }
            }
        }
    }

    private fun toast(message: String){
        /* Creates a quick toast */
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}







