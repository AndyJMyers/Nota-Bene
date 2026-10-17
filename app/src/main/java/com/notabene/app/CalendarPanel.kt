package com.notabene.app

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
internal fun CalendarPanel(collection: Collection, accent: Color, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val dao = remember { NotaBeneDatabase.get(context).collectionDao() }
    val entries by remember(collection.id) { dao.observeEntries(collection.id) }.collectAsState(emptyList())
    val scope = rememberCoroutineScope()
    var editorOpen by rememberSaveable(collection.id) { mutableStateOf(false) }
    var editingId by rememberSaveable(collection.id) { mutableStateOf<Long?>(null) }
    var deletingId by rememberSaveable(collection.id) { mutableStateOf<Long?>(null) }
    var showPast by rememberSaveable(collection.id) { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var pulse by remember { mutableIntStateOf(0) }
    var now by remember { mutableStateOf(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)) }
    LaunchedEffect(collection.id) {
        while (true) {
            now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)
            delay(15_000)
        }
    }
    val upcoming = entries.mapNotNull { entry ->
        val start = runCatching { LocalDateTime.parse(entry.calendarAt) }.getOrNull() ?: return@mapNotNull null
        val repeat = runCatching { CalendarRepeat.valueOf(entry.calendarRepeat) }.getOrDefault(CalendarRepeat.NONE)
        val next = nextCalendarOccurrence(start, repeat, now)
        if (next == null && !showPast) null else Triple(entry, next ?: start, next == null)
    }.sortedWith(compareBy<Triple<CollectionEntry, LocalDateTime, Boolean>> { it.third }.thenBy { it.second })
    fun save(action: suspend () -> Unit, onSuccess: () -> Unit) {
        if (busy) return
        busy = true
        scope.launch {
            try { action(); onSuccess() }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { Toast.makeText(context, "Could not save the change. Please try again.", Toast.LENGTH_LONG).show() }
            finally { busy = false }
        }
    }
    Column(modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.fillMaxWidth()) {
            Button(onClick = { editingId = null; editorOpen = true }, modifier = Modifier.fillMaxWidth()) { Text("NEW EVENT") }
            CompletionFlourish(pulse, accent, Modifier.matchParentSize())
        }
        Text("Local calendar · no sharing or alerts", color = MaterialTheme.colorScheme.onBackground)
        TextButton(onClick = { showPast = !showPast }) { Text(if (showPast) "HIDE PAST EVENTS" else "SHOW PAST EVENTS") }
        if (upcoming.isEmpty()) {
            Text(if (entries.isEmpty()) "Something to look forward to. Tap NEW EVENT to start." else "No upcoming events. Past events are kept below SHOW PAST EVENTS.", color = MaterialTheme.colorScheme.onBackground)
        }
        upcoming.forEach { (entry, date, past) ->
            key(entry.id) {
                NotaCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(entry.text, style = MaterialTheme.typography.titleMedium)
                        Text(date.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)))
                        if (past) Text("Past event")
                        if (entry.calendarRepeat != "NONE") Text("Repeats ${entry.calendarRepeat.lowercase()} · next occurrence shown")
                        if (entry.detail.isNotBlank()) Text(entry.detail)
                        Row {
                            TextButton(onClick = { editingId = entry.id; editorOpen = true }, enabled = !busy) { Text("EDIT") }
                            TextButton(onClick = { deletingId = entry.id }, enabled = !busy) { Text("DELETE") }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
    if (editorOpen) {
        val original = entries.firstOrNull { it.id == editingId }
        // A restored dialog waits for Room to deliver its original event.
        if (editingId == null || original != null) CalendarEventDialog(original, busy,
            onDismiss = { if (!busy) editorOpen = false },
            onSave = { title, detail, start, repeat ->
                save({
                    val updated = (original ?: CollectionEntry(collectionId = collection.id, text = title)).copy(
                        text = title, detail = detail, calendarAt = start.toString(), calendarRepeat = repeat.name)
                    if (original == null) dao.insertEntry(updated) else dao.updateEntry(updated)
                }, { editorOpen = false; pulse++ })
            })
    }
    deletingId?.let { id ->
        AlertDialog(onDismissRequest = { if (!busy) deletingId = null },
            title = { Text("Delete event?") },
            text = { Text("This removes the whole event, including its future repeats. Other events are kept.") },
            confirmButton = { TextButton(enabled = !busy, onClick = {
                save({ dao.deleteEntryAndEvents(id) }, { deletingId = null })
            }) { Text("DELETE") } },
            dismissButton = { TextButton(enabled = !busy, onClick = { deletingId = null }) { Text("CANCEL") } })
    }
}

@Composable
private fun CalendarEventDialog(original: CollectionEntry?, busy: Boolean, onDismiss: () -> Unit,
    onSave: (String, String, LocalDateTime, CalendarRepeat) -> Unit) {
    val context = LocalContext.current
    val initial = remember(original?.id) {
        original?.calendarAt?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() }
            ?: LocalDateTime.now().plusHours(1).truncatedTo(ChronoUnit.MINUTES)
    }
    var title by rememberSaveable(original?.id) { mutableStateOf(original?.text.orEmpty()) }
    var detail by rememberSaveable(original?.id) { mutableStateOf(original?.detail.orEmpty()) }
    var date by rememberSaveable(original?.id) { mutableStateOf(initial.toLocalDate().toString()) }
    var time by rememberSaveable(original?.id) { mutableStateOf(initial.toLocalTime().toString()) }
    var repeatName by rememberSaveable(original?.id) { mutableStateOf(original?.calendarRepeat ?: "NONE") }
    var repeatMenu by remember { mutableStateOf(false) }
    val repeat = CalendarRepeat.valueOf(repeatName)
    AlertDialog(onDismissRequest = onDismiss,
        title = { Text(if (original == null) "NEW EVENT" else "EDIT EVENT") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(title, { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth(), enabled = !busy)
                OutlinedButton(enabled = !busy, onClick = {
                    val current = LocalDate.parse(date)
                    DatePickerDialog(context, { _, y, m, d -> date = LocalDate.of(y, m + 1, d).toString() }, current.year, current.monthValue - 1, current.dayOfMonth).show()
                }, modifier = Modifier.fillMaxWidth()) { Text("Date: " + LocalDate.parse(date).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))) }
                OutlinedButton(enabled = !busy, onClick = {
                    val current = LocalTime.parse(time)
                    TimePickerDialog(context, { _, h, m -> time = LocalTime.of(h, m).toString() }, current.hour, current.minute, true).show()
                }, modifier = Modifier.fillMaxWidth()) { Text("Time: $time") }
                Box {
                    OutlinedButton(enabled = !busy, onClick = { repeatMenu = true }) { Text("Repeat: ${repeat.label}") }
                    DropdownMenu(expanded = repeatMenu, onDismissRequest = { repeatMenu = false }) {
                        CalendarRepeat.entries.forEach { option -> DropdownMenuItem(text = { Text(option.label) }, onClick = { repeatName = option.name; repeatMenu = false }) }
                    }
                }
                OutlinedTextField(detail, { detail = it }, label = { Text("Details (optional)") }, modifier = Modifier.fillMaxWidth(), enabled = !busy, minLines = 2)
                Text("Dates and times stay local. Monthly/yearly repeats use the last valid day when needed. Editing changes the whole series.")
            }
        },
        confirmButton = { TextButton(enabled = !busy && title.isNotBlank(), onClick = { onSave(title.trim(), detail.trim(), LocalDateTime.of(LocalDate.parse(date), LocalTime.parse(time)), repeat) }) { Text("SAVE") } },
        dismissButton = { TextButton(enabled = !busy, onClick = onDismiss) { Text("CANCEL") } })
}
