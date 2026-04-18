package com.bbdyno.hyroxsim.feature.settings

import androidx.lifecycle.ViewModel
import com.bbdyno.hyroxsim.sync.garmin.GarminBridge
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val bridge: GarminBridge,
) : ViewModel() {
    val connectedDeviceName: String? get() = bridge.connectedDeviceName
    fun requestDeviceSelection() = bridge.requestDeviceSelection()
}
