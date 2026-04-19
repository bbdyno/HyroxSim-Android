package com.bbdyno.hyroxsim.feature.settings

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.health.connect.client.PermissionController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bbdyno.hyroxsim.core.sensors.HeartRateSource
import kotlinx.coroutines.launch

@Composable
fun SettingsRoute(
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    vm: SettingsViewModel = hiltViewModel(),
) {
    Column(
        modifier = modifier
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.settings_back), tint = Color.White)
                }
            }
            Text(
                text = stringResource(R.string.settings_title),
                color = Color(0xFFFFD700),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = if (onBack == null) Modifier.padding(start = 4.dp, top = 8.dp) else Modifier,
            )
        }
        HorizontalDivider(color = Color(0xFF222222))

        GarminCard(vm = vm)
        SensorPermissionsCard()

        Surface(
            color = Color(0xFF0C0C0C),
            contentColor = Color.White,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.settings_app_version_title), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    stringResource(R.string.settings_app_version_value),
                    color = Color(0xFF888888),
                    fontSize = 12.sp,
                )
            }
        }
    }
}

@Composable
private fun GarminCard(vm: SettingsViewModel) {
    val disconnectedLabel = stringResource(R.string.settings_garmin_status_disconnected)
    val launchedLabel = stringResource(R.string.settings_garmin_status_launched)
    var statusText by remember { mutableStateOf(disconnectedLabel) }
    Surface(
        color = Color(0xFF0C0C0C),
        contentColor = Color.White,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(stringResource(R.string.settings_garmin_title), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(
                stringResource(R.string.settings_garmin_instructions),
                color = Color(0xFFAAAAAA),
                fontSize = 12.sp,
            )
            Text(
                text = stringResource(R.string.settings_garmin_status_prefix, vm.connectedDeviceName ?: statusText),
                color = Color(0xFF888888),
                fontSize = 12.sp,
            )
            Button(
                onClick = {
                    vm.requestDeviceSelection()
                    statusText = launchedLabel
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFD700),
                    contentColor = Color.Black,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.settings_garmin_pick_device), fontWeight = FontWeight.Bold) }
        }
    }
}

/**
 * Runtime permission management for GPS + Health Connect HR. Android's
 * permission model splits them: location is a classic runtime permission
 * (dangerous level), Health Connect uses a dedicated contract.
 */
@Composable
private fun SensorPermissionsCard() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val hrSource = remember { HeartRateSource(context) }
    val lifecycleOwner = LocalLifecycleOwner.current

    var locationGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hrGranted by remember { mutableStateOf(false) }
    var hrAvailable by remember { mutableStateOf(true) }

    // Refresh HC status when the screen becomes visible (user might have
    // granted permissions in a different app).
    LaunchedEffect(lifecycleOwner.lifecycle) {
        hrAvailable = hrSource.isAvailable()
        if (hrAvailable) {
            hrGranted = hrSource.hasPermissions()
        }
    }

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> locationGranted = granted }

    val hcLauncher = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        hrGranted = granted.containsAll(HeartRateSource.REQUIRED_PERMISSIONS)
    }

    Surface(
        color = Color(0xFF0C0C0C),
        contentColor = Color.White,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(stringResource(R.string.settings_sensor_title), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)

            PermissionRow(
                label = stringResource(R.string.settings_permission_location_label),
                description = stringResource(R.string.settings_permission_location_desc),
                granted = locationGranted,
                actionLabel = if (locationGranted) stringResource(R.string.settings_permission_granted) else stringResource(R.string.settings_permission_allow),
                enabled = !locationGranted,
                onClick = { locationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
            )

            PermissionRow(
                label = stringResource(R.string.settings_permission_hr_label),
                description = if (hrAvailable) {
                    stringResource(R.string.settings_permission_hr_desc)
                } else {
                    stringResource(R.string.settings_permission_hr_missing)
                },
                granted = hrGranted,
                actionLabel = when {
                    !hrAvailable -> stringResource(R.string.settings_permission_unsupported)
                    hrGranted -> stringResource(R.string.settings_permission_granted)
                    else -> stringResource(R.string.settings_permission_allow)
                },
                enabled = hrAvailable && !hrGranted,
                onClick = {
                    scope.launch {
                        hcLauncher.launch(HeartRateSource.REQUIRED_PERMISSIONS)
                    }
                },
            )
        }
    }
}

@Composable
private fun PermissionRow(
    label: String,
    description: String,
    granted: Boolean,
    actionLabel: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(
                    color = if (granted) Color(0xFF4CD964) else Color(0xFF666666),
                    shape = CircleShape,
                ),
        )
        Spacer(Modifier.size(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(description, color = Color(0xFF888888), fontSize = 11.sp)
        }
        Button(
            onClick = onClick,
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFD700),
                contentColor = Color.Black,
                disabledContainerColor = Color(0xFF222222),
                disabledContentColor = Color(0xFF888888),
            ),
        ) { Text(actionLabel, fontSize = 12.sp) }
    }
}
