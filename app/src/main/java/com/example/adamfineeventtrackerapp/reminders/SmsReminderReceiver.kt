package com.example.adamfineeventtrackerapp.reminders

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.ContextCompat

// Sends the reminder text
class SmsReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val eventName = intent.getStringExtra(EXTRA_EVENT_NAME) ?: "Event"

        val canSend = ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) ==
            PackageManager.PERMISSION_GRANTED
        if (!canSend) return

        try {
            val smsManager = context.getSystemService(SmsManager::class.java)
            smsManager.sendTextMessage(REMINDER_PHONE_NUMBER, null, "Reminder: $eventName is starting!", null, null)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send SMS reminder for \"$eventName\"", e)
        }
    }

    companion object {
        const val EXTRA_EVENT_NAME = "eventName"
        private const val TAG = "SmsReminderReceiver"

        // Emulator phone number
        private const val REMINDER_PHONE_NUMBER = "5554"
    }
}
