package com.example.adamfineeventtrackerapp.ui.settings

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * SMS notification settings and sign out. UI only; state lives in [SettingsViewModel].
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onSignOut: () -> Unit
) {
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
        viewModel::onPermissionResult
    )

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("SMS Notifications", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                    .padding(15.dp)
            ) {
                Text(
                    if (viewModel.smsEnabled) "Status: ENABLED\n\nYou will get texts for upcoming events!"
                    else "Status: DISABLED\n\nYou won't get any texts unless you allow SMS."
                )
            }
            Spacer(Modifier.height(30.dp))

            if (viewModel.smsEnabled) {
                Button(onClick = {}, enabled = false) {
                    Text("ALREADY ALLOWED")
                }
            } else {
                Button(onClick = { permissionLauncher.launch(Manifest.permission.SEND_SMS) }) {
                    Text("ALLOW SMS UPDATES")
                }
            }

            TextButton(onClick = onBack) {
                Text("Back to Event List")
            }
            Spacer(Modifier.height(10.dp))

            Button(
                onClick = onSignOut,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.7f))
            ) {
                Text("SIGN OUT", color = Color.White)
            }
        }
    }
}
