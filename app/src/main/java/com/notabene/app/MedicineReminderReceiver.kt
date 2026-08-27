package com.notabene.app

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

private const val ReminderChannel = "meds_reminders"
private const val ReminderRequest = 7401
private const val ReminderInterval = 15 * 60 * 1000L

object MedicineReminderScheduler {
    fun prepare(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                ReminderChannel,
                "MEDS reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for user-entered medicine schedules"
            }
        )
        schedule(context)
    }

    fun schedule(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val intent = Intent(context, MedicineReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ReminderRequest,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + 60_000L,
            ReminderInterval,
            pendingIntent
        )
    }

    fun cancelNotification(context: Context, medicationId: Long) {
        context.getSystemService(NotificationManager::class.java)
            .cancel(notificationId(medicationId))
    }

    fun notificationsEnabled(context: Context): Boolean =
        context.getSystemService(NotificationManager::class.java).areNotificationsEnabled()

    private fun notificationId(medicationId: Long): Int =
        10_000 + (medicationId % 1_000_000).toInt()

    internal fun idFor(medicationId: Long): Int = notificationId(medicationId)
}

class MedicineReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                checkSchedules(context.applicationContext)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun checkSchedules(context: Context) {
        if (Build.VERSION.SDK_INT >= 33 &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val dao = NotaBeneDatabase.get(context).medicationDao()
        val medications = dao.observeMedications().first().filter { it.active }
        val logs = dao.observeDoseLogs().first()
        val today = LocalDate.now()
        val todayText = today.toString()
        val now = System.currentTimeMillis()
        val evening = LocalTime.of(18, 0)
        val preferences = context.getSharedPreferences("meds_reminders", Context.MODE_PRIVATE)
        val notificationManager = context.getSystemService(NotificationManager::class.java)

        medications.forEach { medication ->
            val taken = logs.any { it.medicationId == medication.id && it.doseDate == todayText }
            if (taken) {
                notificationManager.cancel(MedicineReminderScheduler.idFor(medication.id))
                return@forEach
            }

            val doseTime = runCatching { LocalTime.parse(medication.doseTime) }.getOrNull() ?: return@forEach
            val scheduled = today.atTime(doseTime).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            if (now < scheduled) return@forEach

            val stage = if (!LocalTime.now().isBefore(evening)) "evening" else "due"
            val key = "${medication.id}:$todayText:$stage"
            if (preferences.getBoolean(key, false)) return@forEach

            val openApp = PendingIntent.getActivity(
                context,
                MedicineReminderScheduler.idFor(medication.id),
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val message = if (stage == "evening") {
                "No dose has been recorded today for ${medication.name}."
            } else {
                "${medication.name} ${medication.dosage} was due at ${medication.doseTime}."
            }
            val notification = NotificationCompat.Builder(context, ReminderChannel)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(if (stage == "evening") "MEDS still needs attention" else "MEDS reminder")
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(openApp)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
            notificationManager.notify(MedicineReminderScheduler.idFor(medication.id), notification)
            preferences.edit().putBoolean(key, true).apply()
        }
    }
}

class MedicineReminderBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            MedicineReminderScheduler.prepare(context.applicationContext)
        }
    }
}
