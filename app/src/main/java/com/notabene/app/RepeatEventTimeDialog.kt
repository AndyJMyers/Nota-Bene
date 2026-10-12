package com.notabene.app

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
internal fun RepeatEventTimeDialog(
    initialTime: Long,
    isNew: Boolean,
    busy: Boolean,
    onDismiss: () -> Unit,
    onSave: (Long) -> Unit
) {
    val context = LocalContext.current
    val zone = ZoneId.systemDefault()
    val initial = Instant.ofEpochMilli(initialTime).atZone(zone)
    var dateText by rememberSaveable(initialTime) { mutableStateOf(initial.toLocalDate().toString()) }
    var timeText by rememberSaveable(initialTime) { mutableStateOf(initial.toLocalTime().withSecond(0).withNano(0).toString()) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val date = LocalDate.parse(dateText)
    val time = LocalTime.parse(timeText)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "Add past event" else "Correct recorded time") },
        text = {
            Column {
                Text("When did it happen? Times use this phone’s local time zone.")
                TextButton(enabled = !busy, onClick = {
                    DatePickerDialog(context, { _, year, month, day ->
                        dateText = LocalDate.of(year, month + 1, day).toString()
                        error = null
                    }, date.year, date.monthValue - 1, date.dayOfMonth).apply {
                        datePicker.maxDate = System.currentTimeMillis()
                    }.show()
                }) { Text("Date: ${date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))}") }
                TextButton(enabled = !busy, onClick = {
                    TimePickerDialog(context, { _, hour, minute ->
                        timeText = LocalTime.of(hour, minute).toString()
                        error = null
                    }, time.hour, time.minute, android.text.format.DateFormat.is24HourFormat(context)).show()
                }) { Text("Time: ${time.format(DateTimeFormatter.ofPattern("HH:mm"))}") }
                error?.let { Text(it) }
            }
        },
        confirmButton = { TextButton(enabled = !busy, onClick = {
            runCatching { repeatHistoryTime(date.atTime(time), zone, initial.offset, System.currentTimeMillis()) }
                .onSuccess(onSave)
                .onFailure { error = it.message }
        }) { Text("SAVE") } },
        dismissButton = { TextButton(enabled = !busy, onClick = onDismiss) { Text("CANCEL") } }
    )
}
