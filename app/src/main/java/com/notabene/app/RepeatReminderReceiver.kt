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
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

private const val RepeatReminderChannel = "repeat_reminders"
private const val RepeatReminderRequest = 7402
private const val RepeatReminderInterval = 15 * 60 * 1000L

/** A modest periodic check for the small number of user-selected repeat reminders. */
object RepeatReminderScheduler {
    fun prepare(context: Context) {
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(
                RepeatReminderChannel,
                "Repeat reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Optional reminders for repeat items you choose"
            }
        )
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            RepeatReminderRequest,
            Intent(context, RepeatReminderReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + 60_000L,
            RepeatReminderInterval,
            pendingIntent
        )
    }

    fun cancelNotification(context: Context, entryId: Long) {
        context.getSystemService(NotificationManager::class.java).cancel(notificationId(entryId))
    }

    private fun notificationId(entryId: Long): Int = 20_000 + (entryId % 1_000_000).toInt()
    internal fun idFor(entryId: Long): Int = notificationId(entryId)
}

class RepeatReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                checkDueRepeats(context.applicationContext)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun checkDueRepeats(context: Context) {
        if (Build.VERSION.SDK_INT >= 33 &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val dao = NotaBeneDatabase.get(context).collectionDao()
        val collections = dao.observeCollections().first().filter { it.kind == CollectionKind.REPEAT.name }
        val now = System.currentTimeMillis()
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        collections.forEach { collection ->
            dao.observeEntries(collection.id).first()
                .filter { it.notifyWhenDue && it.intervalDays > 0 }
                .forEach { entry ->
                    val dueAt = dueAt(entry) ?: return@forEach
                    if (now < dueAt) return@forEach
                    val dueKey = "${entry.id}:${dueAt / 86_400_000L}"
                    val preferences = context.getSharedPreferences("repeat_reminders", Context.MODE_PRIVATE)
                    if (preferences.getBoolean(dueKey, false)) return@forEach

                    val openApp = PendingIntent.getActivity(
                        context,
                        RepeatReminderScheduler.idFor(entry.id),
                        Intent(context, MainActivity::class.java),
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    val message = "${entry.text} is due. Open Nota Bene when you are ready."
                    val notification = NotificationCompat.Builder(context, RepeatReminderChannel)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("Nota Bene reminder")
                        .setContentText(message)
                        .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                        .setContentIntent(openApp)
                        .setAutoCancel(true)
                        .build()
                    notificationManager.notify(RepeatReminderScheduler.idFor(entry.id), notification)
                    preferences.edit().putBoolean(dueKey, true).apply()
                }
        }
    }

    private fun dueAt(entry: CollectionEntry): Long? {
        val time = runCatching { LocalTime.parse(entry.notifyAt) }.getOrNull() ?: return null
        val zone = ZoneId.systemDefault()
        val anchor = entry.lastCompletedAt ?: entry.createdAt
        val dueDate = Instant.ofEpochMilli(anchor).atZone(zone).toLocalDate().plusDays(entry.intervalDays.toLong())
        return LocalDateTime.of(dueDate, time).atZone(zone).toInstant().toEpochMilli()
    }
}

class RepeatReminderBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            RepeatReminderScheduler.prepare(context.applicationContext)
        }
    }
}
