package app.echoirx

import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import app.echoirx.data.permission.PermissionsManager
import app.echoirx.presentation.screens.MainScreen
import app.echoirx.presentation.theme.EchoirTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var permissionsManager: PermissionsManager

    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        checkAndRequestAllFilesAccess()
    }

    private val allFilesAccessLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request permissions
        if (!permissionsManager.arePermissionsGranted()) {
            permissionsLauncher.launch(permissionsManager.requiredPermissions)
        } else {
            checkAndRequestAllFilesAccess()
        }

        setContent {
            EchoirTheme {
                MainScreen()
            }
        }
    }

    private fun checkAndRequestAllFilesAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            permissionsManager.getAllFilesAccessIntent()?.let { intent ->
                allFilesAccessLauncher.launch(intent)
            }
        }
    }
}