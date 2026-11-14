package com.example.adamfineeventtrackerapp.reminders

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Owns all AlarmManager / PendingIntent logic for event reminders.
 * Each event uses its database id as the PendingIntent request code, so
 * re-scheduling an edited event replaces its old alarm.
 */
class ReminderScheduler(context: Context) {

    enum class Result { SCHEDULED, NO_PERMISSION, INVALID_DATE }

    private val appContext = context.applicationContext
    private val alarmManager = appContext.getSystemService(AlarmManager::class.java)

    fun schedule(eventId: Int, eventName: String, date: String, time: String): Result {
        if (!hasSmsPermission()) return Result.NO_PERMISSION

        val triggerAt = parseDateTime(date, time) ?: return Result.INVALID_DATE
        val pendingIntent = buildPendingIntent(eventId, eventName)

        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
        return Result.SCHEDULED
    }

    fun cancel(eventId: Int) {
        alarmManager.cancel(buildPendingIntent(eventId, eventName = null))
    }

    private fun hasSmsPermission(): Boolean =
        ContextCompat.checkSelfPermission(appContext, Manifest.permission.SEND_SMS) ==
            PackageManager.PERMISSION_GRANTED

    private fun parseDateTime(date: String, time: String): Long? = try {
        SimpleDateFormat(DATE_TIME_PATTERN, Locale.getDefault()).parse("$date $time")?.time
    } catch (_: ParseException) {
        null
    }

    private fun buildPendingIntent(eventId: Int, eventName: String?): PendingIntent {
        val intent = Intent(appContext, SmsReminderReceiver::class.java).apply {
            if (eventName != null) putExtra(SmsReminderReceiver.EXTRA_EVENT_NAME, eventName)
        }
        return PendingIntent.getBroadcast(
            appContext,
            eventId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    companion object {
        const val DATE_TIME_PATTERN = "MM/dd/yyyy HH:mm"
    }
}
