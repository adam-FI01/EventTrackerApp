package com.example.adamfineeventtrackerapp.ui.settings

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel

// Tracks if SMS is allowed
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    var smsEnabled by mutableStateOf(hasSmsPermission())
        private set

    fun onPermissionResult(granted: Boolean) {
        smsEnabled = granted
    }

    private fun hasSmsPermission(): Boolean =
        ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.SEND_SMS) ==
            PackageManager.PERMISSION_GRANTED
}
