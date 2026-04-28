package com.echorescue.app

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.view.WindowCompat
import com.echorescue.app.ui.EchoRescueApp
import com.echorescue.app.ui.EchoRescueViewModel
import com.echorescue.app.ui.theme.EchoRescueTheme

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<EchoRescueViewModel> {
        EchoRescueViewModel.Factory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            EchoRescueTheme(useLightTheme = state.useLightTheme) {
                val colorScheme = androidx.compose.material3.MaterialTheme.colorScheme
                SideEffect {
                    window.statusBarColor = colorScheme.background.toArgb()
                    window.navigationBarColor = colorScheme.background.toArgb()
                    WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightStatusBars = state.useLightTheme
                        isAppearanceLightNavigationBars = state.useLightTheme
                    }
                }

                var permissionRequested by remember { mutableStateOf(false) }
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { results ->
                    viewModel.updatePermissionStatus(results)
                }

                LaunchedEffect(permissionRequested) {
                    if (!permissionRequested) {
                        permissionRequested = true
                        val permissions = mutableListOf(
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_ADVERTISE,
                            Manifest.permission.RECORD_AUDIO
                        )
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        permissionLauncher.launch(permissions.toTypedArray())
                    }
                }

                LaunchedEffect(state.permissions.allGranted, state.emergencyRole) {
                    if (state.permissions.allGranted) {
                        viewModel.activateEmergencyRoleIfPossible()
                    }
                }

                EchoRescueApp(
                    state = state,
                    onSelectTab = viewModel::selectTab,
                    onSelectRescueMode = viewModel::selectRescueMode,
                    onStartVictimMode = viewModel::startVictimMode,
                    onStopVictimMode = viewModel::stopVictimMode,
                    onScanVictims = viewModel::scanAndConnect,
                    onRunMeasurement = viewModel::runMeasurement,
                    onSetCalibrationReference = viewModel::setCalibrationReference,
                    onSaveCalibration = viewModel::saveCalibrationFromLastMeasurement,
                    onRecordAnchorMeasurement = viewModel::recordCurrentMeasurementAtNextAnchor,
                    onClearAnchorMeasurements = viewModel::clearAnchorMeasurements,
                    onSolveVictimLocation = viewModel::solveVictimLocation,
                    onSetEmergencyRole = viewModel::setEmergencyRole,
                    onAskMedical = viewModel::askMedical,
                    onQuestionChange = viewModel::setMedicalQuestion,
                    onDismissLanding = viewModel::dismissLanding,
                    onToggleLightTheme = viewModel::toggleLightTheme,
                    onRetryPermissions = {
                        val permissions = mutableListOf(
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_ADVERTISE,
                            Manifest.permission.RECORD_AUDIO
                        )
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        permissionLauncher.launch(permissions.toTypedArray())
                    }
                )
            }
        }
    }
}
