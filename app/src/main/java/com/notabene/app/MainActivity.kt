package com.notabene.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.statusBarsPadding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.text.DateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Date
import java.util.Locale

private val Ink = Color(0xFF090812)
private val Glass = Color(0xFF28212B)
private val Purple = Color(0xFF321052)
private val Blue = Color(0xFF164B89)
private val Fusion = Color(0xFFF2C94C)
private val Crimson = Color(0xFF9D174D)

private enum class Tab(val shortLabel: String, val title: String, val prompt: String) {
    PAYMENTS("PAY", "Payments", "Capture, check, then keep"),
    MEDICINE("MED", "Medicine", "Record medicine or prescription"),
    HEALTH("BODY", "Health log", "Log a symptom or measurement"),
    TASKS("TASK", "To do & dependencies", "Add a task or dependency"),
    RESEARCH("ASK", "Further research", "Add a question to investigate")
}

private enum class Effect { STARS, SNOW }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NotaBeneApp() }
    }
}

@Composable
private fun NotaBeneApp() {
    var selected by remember { mutableStateOf(Tab.PAYMENTS) }
    var mood by remember { mutableFloatStateOf(.42f) }
    var effect by remember { mutableStateOf(Effect.STARS) }
    val accent = moodColour(mood)

    MaterialTheme(colorScheme = darkColorScheme(primary = accent, surface = Glass, background = Ink)) {
        Box(Modifier.fillMaxSize().background(Ink)) {
            CalmBackground(effect, accent)
            Column(
                Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 18.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Header(
                    mood = mood,
                    accent = accent,
                    effect = effect,
                    onMoodChange = { mood = it },
                    onCycleEffect = { effect = if (effect == Effect.STARS) Effect.SNOW else Effect.STARS }
                )
                InstrumentTabs(selected, accent) { selected = it }
                AnimatedContent(
                    targetState = selected,
                    transitionSpec = { fadeIn(tween(450)) togetherWith fadeOut(tween(1100)) },
                    label = "tab title"
                ) { tab ->
                    Text(tab.title.uppercase(), color = accent, fontSize = 23.sp, fontWeight = FontWeight.Light, letterSpacing = 3.sp)
                }
                when (selected) {
                    Tab.PAYMENTS -> PaymentPanel(accent, Modifier.weight(1f))
                    Tab.MEDICINE -> MedicationPanel(accent, Modifier.weight(1f))
                    Tab.HEALTH -> BodyPanel(accent, Modifier.weight(1f))
                    Tab.TASKS -> TaskPanel(accent, Modifier.weight(1f))
                    Tab.RESEARCH -> AskPanel(accent, Modifier.weight(1f))
                    else -> PlaceholderPanel(selected, accent, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun Header(
    mood: Float,
    accent: Color,
    effect: Effect,
    onMoodChange: (Float) -> Unit,
    onCycleEffect: () -> Unit
) {
    Row(Modifier.fillMaxWidth().height(52.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            Text("NOTA BENE", color = Color(0xFFE9E0D2), fontSize = 26.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
            Text("PERSONAL OPERATIONS LOG", color = Color(0xFF8F8790), fontSize = 9.sp, letterSpacing = 2.sp)
        }
        Row(Modifier.weight(1f).height(52.dp), verticalAlignment = Alignment.CenterVertically) {
            Slider(
                value = mood,
                onValueChange = onMoodChange,
                modifier = Modifier.weight(1f).height(48.dp),
                colors = SliderDefaults.colors(thumbColor = accent, activeTrackColor = accent)
            )
            TextButton(onClick = onCycleEffect, contentPadding = ButtonDefaults.TextButtonContentPadding) {
                Text(if (effect == Effect.STARS) "✦ SKY" else "❄ SNOW", color = Color(0xFFC8BDC8), fontSize = 9.sp)
            }
        }
    }
}

@Composable
private fun InstrumentTabs(selected: Tab, accent: Color, onSelect: (Tab) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Tab.entries.forEach { tab ->
            val active = tab == selected
            val glow by animateFloatAsState(if (active) 1f else .14f, tween(420), label = "filament glow")
            Box(
                Modifier.weight(1f).height(50.dp)
                    .background(Brush.verticalGradient(listOf(lerp(Ink, accent, glow * .55f), Glass, Ink)), RoundedCornerShape(7.dp))
                    .border(2.dp, lerp(Color(0xFF39313B), accent, glow), RoundedCornerShape(7.dp))
                    .clickable { onSelect(tab) },
                contentAlignment = Alignment.Center
            ) {
                Text(tab.shortLabel, color = lerp(Color(0xFF6A626C), Color(0xFFFFE8A8), glow), fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
private fun PaymentPanel(accent: Color, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val dao = remember { NotaBeneDatabase.get(context).paymentDao() }
    val payments by dao.observeAll().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var merchant by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("manual") }
    var status by remember { mutableStateOf("Ready for manual, voice or receipt capture") }
    var busy by remember { mutableStateOf(false) }

    fun absorb(text: String, captureSource: String) {
        val suggestion = parseCapture(text)
        if (merchant.isBlank()) merchant = suggestion.first
        if (amount.isBlank()) amount = suggestion.second
        note = listOf(note, text.trim()).filter { it.isNotBlank() }.joinToString("\n")
        source = captureSource
    }

    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val words = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull().orEmpty()
            if (words.isNotBlank()) {
                absorb(words, "voice")
                status = "Speech captured — check the fields before saving"
            } else status = "No speech was returned"
        } else status = "Listening cancelled"
    }

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        busy = true
        status = "Reading receipt text on this device…"
        runCatching { InputImage.fromFilePath(context, uri) }
            .onSuccess { image ->
                TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS).process(image)
                    .addOnSuccessListener { result ->
                        absorb(result.text, "receipt")
                        status = if (result.text.isBlank()) "No readable text found — try a clearer image" else "Receipt read — check the suggestions before saving"
                    }
                    .addOnFailureListener { status = "Could not read that image: ${it.localizedMessage ?: "unknown error"}" }
                    .addOnCompleteListener { busy = false }
            }
            .onFailure {
                busy = false
                status = "Could not open that image"
            }
    }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xE61B1820)), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("CAPTURE / REVIEW", color = accent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PaymentField("Merchant", merchant, { merchant = it }, Modifier.weight(1.35f), accent)
                    PaymentField("Amount", amount, { amount = it }, Modifier.weight(.8f), accent)
                }
                PaymentField("Notes / captured text", note, { note = it }, Modifier.fillMaxWidth(), accent, minLines = 2)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            scope.launch {
                                dao.insert(PaymentRecord(merchant = merchant.trim(), amount = amount.trim(), note = note.trim(), capturedFrom = source))
                                merchant = ""; amount = ""; note = ""; source = "manual"
                                status = "Payment saved locally"
                            }
                        },
                        enabled = merchant.isNotBlank() || amount.isNotBlank() || note.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = accent)
                    ) { Text("KEEP", color = Ink, fontWeight = FontWeight.Black) }
                    OutlinedButton(onClick = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Describe the payment")
                        }
                        runCatching { speechLauncher.launch(intent) }.onFailure { status = "No speech recognition service is available" }
                    }) { Text("LISTEN") }
                    OutlinedButton(onClick = { imageLauncher.launch("image/*") }, enabled = !busy) { Text(if (busy) "READING" else "RECEIPT") }
                }
                Text(status, color = Color(0xFFA79DA8), fontSize = 11.sp)
            }
        }
        Text("KEPT PAYMENTS  ${payments.size}", color = Color(0xFF8F8790), fontSize = 10.sp, letterSpacing = 2.sp)
        if (payments.isEmpty()) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("Nothing kept yet", color = Color(0xFF6F6771), fontSize = 13.sp)
            }
        } else {
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                items(payments, key = { it.id }) { payment ->
                    PaymentRow(payment, accent) { scope.launch { dao.delete(payment.id) } }
                }
            }
        }
    }
}

@Composable
private fun PaymentField(label: String, value: String, onChange: (String) -> Unit, modifier: Modifier, accent: Color, minLines: Int = 1) {
    OutlinedTextField(
        value = value, onValueChange = onChange, modifier = modifier, minLines = minLines,
        label = { Text(label) },
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accent, cursorColor = accent, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
    )
}

@Composable
private fun PaymentRow(payment: PaymentRecord, accent: Color, onDelete: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xD91B1820)), shape = RoundedCornerShape(9.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(payment.merchant.ifBlank { "Unlabelled payment" }, color = Color(0xFFE6DEE6), fontWeight = FontWeight.SemiBold)
                Text(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(payment.createdAt)) + "  ·  " + payment.capturedFrom.uppercase(), color = Color(0xFF847C86), fontSize = 10.sp)
                if (payment.note.isNotBlank()) Text(payment.note.replace('\n', ' '), color = Color(0xFFAFA5AF), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (payment.amount.isNotBlank()) Text(payment.amount, color = accent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = onDelete) { Text("×", color = Color(0xFF817781), fontSize = 20.sp) }
        }
    }
}

@Composable
private fun AskPanel(accent: Color, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val dao = remember { NotaBeneDatabase.get(context).askDao() }
    val items by dao.observeAll().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var draft by remember { mutableStateOf("") }
    var hideCompleted by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Type or speak something to investigate") }
    val visibleItems = remember(items, hideCompleted) { if (hideCompleted) items.filterNot { it.done } else items }

    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val words = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull().orEmpty()
            if (words.isNotBlank()) {
                draft = words
                status = "Speech captured — edit it or keep it"
            } else status = "No speech was returned"
        } else status = "Listening cancelled"
    }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xE61B1820)), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("NEW QUESTION / TASK", color = accent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                PaymentField("What needs looking into?", draft, { draft = it }, Modifier.fillMaxWidth(), accent, minLines = 2)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            scope.launch {
                                dao.insert(AskItem(text = draft.trim()))
                                draft = ""
                                status = "ASK item saved locally"
                            }
                        },
                        enabled = draft.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = accent)
                    ) { Text("KEEP", color = Ink, fontWeight = FontWeight.Black) }
                    OutlinedButton(onClick = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "What needs investigating?")
                        }
                        runCatching { speechLauncher.launch(intent) }.onFailure { status = "No speech recognition service is available" }
                    }) { Text("LISTEN") }
                }
                Text(status, color = Color(0xFFA79DA8), fontSize = 11.sp)
            }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("ASK ITEMS  ${items.count { !it.done }} OPEN", color = Color(0xFF8F8790), fontSize = 10.sp, letterSpacing = 2.sp, modifier = Modifier.weight(1f))
            TextButton(onClick = { hideCompleted = !hideCompleted }) {
                Text(if (hideCompleted) "SHOW COMPLETED" else "HIDE COMPLETED", color = accent, fontSize = 10.sp)
            }
        }
        if (visibleItems.isEmpty()) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text(if (items.isEmpty()) "Nothing waiting to be investigated" else "All completed items are hidden", color = Color(0xFF6F6771), fontSize = 13.sp)
            }
        } else {
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                items(visibleItems, key = { it.id }) { item ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xD91B1820)), shape = RoundedCornerShape(9.dp)) {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(checked = item.done, onCheckedChange = { done -> scope.launch { dao.setDone(item.id, done) } })
                            Text(
                                item.text,
                                color = if (item.done) Color(0xFF777078) else Color(0xFFE6DEE6),
                                fontSize = 14.sp,
                                textDecoration = if (item.done) TextDecoration.LineThrough else TextDecoration.None,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskPanel(accent: Color, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val dao = remember { NotaBeneDatabase.get(context).taskDao() }
    val tasks by dao.observeAll().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var draft by remember { mutableStateOf("") }
    var waitingOn by remember { mutableStateOf("") }
    var hideCompleted by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Type or speak a task") }
    val visibleTasks = remember(tasks, hideCompleted) { if (hideCompleted) tasks.filterNot { it.done } else tasks }

    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val words = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull().orEmpty()
            if (words.isNotBlank()) {
                draft = words
                status = "Speech captured — edit it or keep it"
            } else status = "No speech was returned"
        } else status = "Listening cancelled"
    }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xE61B1820)), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("NEW TASK", color = accent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                PaymentField("What needs doing?", draft, { draft = it }, Modifier.fillMaxWidth(), accent, minLines = 2)
                PaymentField("Waiting on… (optional)", waitingOn, { waitingOn = it }, Modifier.fillMaxWidth(), accent)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            scope.launch {
                                dao.insert(TaskItem(text = draft.trim(), waitingOn = waitingOn.trim()))
                                draft = ""; waitingOn = ""
                                status = "Task saved locally"
                            }
                        },
                        enabled = draft.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = accent)
                    ) { Text("KEEP", color = Ink, fontWeight = FontWeight.Black) }
                    OutlinedButton(onClick = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "What needs doing?")
                        }
                        runCatching { speechLauncher.launch(intent) }.onFailure { status = "No speech recognition service is available" }
                    }) { Text("LISTEN") }
                }
                Text(status, color = Color(0xFFA79DA8), fontSize = 11.sp)
            }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("TASKS  ${tasks.count { !it.done }} OPEN", color = Color(0xFF8F8790), fontSize = 10.sp, letterSpacing = 2.sp, modifier = Modifier.weight(1f))
            TextButton(onClick = { hideCompleted = !hideCompleted }) {
                Text(if (hideCompleted) "SHOW COMPLETED" else "HIDE COMPLETED", color = accent, fontSize = 10.sp)
            }
        }
        if (visibleTasks.isEmpty()) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text(if (tasks.isEmpty()) "Nothing waiting to be done" else "All completed tasks are hidden", color = Color(0xFF6F6771), fontSize = 13.sp)
            }
        } else {
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                items(visibleTasks, key = { it.id }) { task ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xD91B1820)), shape = RoundedCornerShape(9.dp)) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = task.done, onCheckedChange = { done -> scope.launch { dao.setDone(task.id, done) } })
                            Column(Modifier.weight(1f)) {
                                Text(
                                    task.text,
                                    color = if (task.done) Color(0xFF777078) else Color(0xFFE6DEE6),
                                    fontSize = 14.sp,
                                    textDecoration = if (task.done) TextDecoration.LineThrough else TextDecoration.None
                                )
                                if (task.waitingOn.isNotBlank()) {
                                    Text("WAITING ON  ${task.waitingOn}", color = if (task.done) Color(0xFF655F66) else accent, fontSize = 10.sp, letterSpacing = 1.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BodyPanel(accent: Color, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val dao = remember { NotaBeneDatabase.get(context).bodyDao() }
    val observations by dao.observeAll().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var draft by remember { mutableStateOf("") }
    var measurement by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Type or speak an observation") }

    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val words = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull().orEmpty()
            if (words.isNotBlank()) {
                draft = words
                status = "Speech captured — edit it or keep it"
            } else status = "No speech was returned"
        } else status = "Listening cancelled"
    }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xE61B1820)), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("NEW BODY RECORD", color = accent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                PaymentField("Symptom or observation", draft, { draft = it }, Modifier.fillMaxWidth(), accent, minLines = 2)
                PaymentField("Measurement (optional)", measurement, { measurement = it }, Modifier.fillMaxWidth(), accent)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            scope.launch {
                                dao.insert(BodyItem(observation = draft.trim(), measurement = measurement.trim()))
                                draft = ""; measurement = ""
                                status = "Body record saved locally"
                            }
                        },
                        enabled = draft.isNotBlank() || measurement.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = accent)
                    ) { Text("KEEP", color = Ink, fontWeight = FontWeight.Black) }
                    OutlinedButton(onClick = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Describe the observation")
                        }
                        runCatching { speechLauncher.launch(intent) }.onFailure { status = "No speech recognition service is available" }
                    }) { Text("LISTEN") }
                }
                Text(status, color = Color(0xFFA79DA8), fontSize = 11.sp)
            }
        }
        Text("BODY HISTORY  ${observations.size}", color = Color(0xFF8F8790), fontSize = 10.sp, letterSpacing = 2.sp)
        if (observations.isEmpty()) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("No observations recorded", color = Color(0xFF6F6771), fontSize = 13.sp)
            }
        } else {
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                items(observations, key = { it.id }) { item ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xD91B1820)), shape = RoundedCornerShape(9.dp)) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                if (item.observation.isNotBlank()) Text(item.observation, color = Color(0xFFE6DEE6), fontSize = 14.sp)
                                if (item.measurement.isNotBlank()) Text(item.measurement, color = accent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(item.createdAt)), color = Color(0xFF847C86), fontSize = 10.sp)
                            }
                            TextButton(onClick = { scope.launch { dao.delete(item.id) } }) { Text("×", color = Color(0xFF817781), fontSize = 20.sp) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MedicationPanel(accent: Color, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val dao = remember { NotaBeneDatabase.get(context).medicationDao() }
    val medications by dao.observeMedications().collectAsState(initial = emptyList())
    val logs by dao.observeDoseLogs().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var doseTime by remember { mutableStateOf("08:00") }
    var startingDoses by remember { mutableStateOf("") }
    var reorderAt by remember { mutableStateOf("7") }
    var showHalted by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("One schedule represents one daily dose") }
    var now by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(60_000)
        }
    }

    val visibleMedications = remember(medications, showHalted) {
        if (showHalted) medications else medications.filter { it.active }
    }
    val validTime = parseDoseTime(doseTime)
    val validStock = startingDoses.toIntOrNull()?.takeIf { it >= 0 }
    val validReorder = reorderAt.toIntOrNull()?.takeIf { it >= 0 }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xE61B1820)), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text("NEW MEDICATION SCHEDULE", color = accent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    PaymentField("Medication", name, { name = it }, Modifier.weight(1.25f), accent)
                    PaymentField("Dosage", dosage, { dosage = it }, Modifier.weight(.9f), accent)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    PaymentField("Daily time", doseTime, { doseTime = it }, Modifier.weight(1f), accent)
                    PaymentField("Doses left", startingDoses, { startingDoses = it.filter(Char::isDigit) }, Modifier.weight(1f), accent)
                    PaymentField("Reorder at", reorderAt, { reorderAt = it.filter(Char::isDigit) }, Modifier.weight(1f), accent)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            scope.launch {
                                dao.insertMedication(
                                    Medication(
                                        name = name.trim(), dosage = dosage.trim(),
                                        doseTime = validTime!!.format(DateTimeFormatter.ofPattern("HH:mm")),
                                        startingDoses = validStock!!, reorderAt = validReorder!!
                                    )
                                )
                                name = ""; dosage = ""; startingDoses = ""; status = "Medication schedule saved locally"
                            }
                        },
                        enabled = name.isNotBlank() && dosage.isNotBlank() && validTime != null && validStock != null && validReorder != null,
                        colors = ButtonDefaults.buttonColors(containerColor = accent)
                    ) { Text("ADD", color = Ink, fontWeight = FontWeight.Black) }
                    Text(status, color = Color(0xFFA79DA8), fontSize = 10.sp, modifier = Modifier.weight(1f))
                }
            }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("MEDICINES", color = Color(0xFF8F8790), fontSize = 10.sp, letterSpacing = 2.sp, modifier = Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = !showHalted, onClick = { showHalted = false })
                Text("ACTIVE", color = if (!showHalted) accent else Color(0xFF777078), fontSize = 9.sp)
                RadioButton(selected = showHalted, onClick = { showHalted = true })
                Text("WITH HALTED", color = if (showHalted) accent else Color(0xFF777078), fontSize = 9.sp)
            }
        }
        if (visibleMedications.isEmpty()) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("No medication schedules to show", color = Color(0xFF6F6771), fontSize = 13.sp)
            }
        } else {
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                items(visibleMedications, key = { it.id }) { medication ->
                    MedicationRow(
                        medication = medication,
                        logs = logs.filter { it.medicationId == medication.id },
                        now = now,
                        accent = accent,
                        onTaken = { takenAt ->
                            scope.launch {
                                val scheduled = scheduledForToday(medication.doseTime)
                                dao.insertDoseLog(
                                    DoseLog(
                                        medicationId = medication.id,
                                        doseDate = LocalDate.now().toString(),
                                        scheduledFor = scheduled,
                                        takenAt = takenAt
                                    )
                                )
                            }
                        },
                        onToggleActive = {
                            scope.launch { dao.setActive(medication.id, !medication.active) }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MedicationRow(
    medication: Medication,
    logs: List<DoseLog>,
    now: Long,
    accent: Color,
    onTaken: (Long) -> Unit,
    onToggleActive: () -> Unit
) {
    val today = LocalDate.now().toString()
    val todayLog = logs.firstOrNull { it.doseDate == today }
    val remaining = (medication.startingDoses - logs.size).coerceAtLeast(0)
    val scheduled = scheduledForToday(medication.doseTime)
    val clockFormat = remember { DateTimeFormatter.ofPattern("HH:mm") }
    var takenTime by remember(medication.id) { mutableStateOf(LocalTime.now().format(clockFormat)) }
    val parsedTakenTime = parseDoseTime(takenTime)
    val recordedTime = todayLog?.takenAt?.let {
        DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(it))
    }
    val stateText = when {
        !medication.active -> "HALTED"
        todayLog != null && todayLog.takenAt <= scheduled + 60 * 60 * 1000 -> "TAKEN $recordedTime"
        todayLog != null -> "TAKEN LATE $recordedTime"
        now > scheduled -> "OVERDUE"
        else -> "DUE ${medication.doseTime}"
    }
    val stateColor = when {
        !medication.active -> Color(0xFF777078)
        todayLog != null -> Color(0xFF73B58A)
        now > scheduled -> Crimson
        else -> accent
    }
    val reorder = remaining <= medication.reorderAt

    Card(colors = CardDefaults.cardColors(containerColor = Color(0xD91B1820)), shape = RoundedCornerShape(9.dp)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(medication.name, color = if (medication.active) Color(0xFFE6DEE6) else Color(0xFF777078), fontWeight = FontWeight.SemiBold)
                    Text("${medication.dosage}  ·  DAILY ${medication.doseTime}", color = Color(0xFF9A919B), fontSize = 10.sp)
                }
                Text(stateText, color = stateColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("$remaining DOSES LEFT", color = if (reorder) Crimson else Color(0xFFC5BBC5), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                if (reorder && medication.active) Text("APPLY FOR MORE", color = Crimson, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (medication.active && todayLog == null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        PaymentField("Taken at", takenTime, { takenTime = it }, Modifier.weight(1f), accent)
                        Button(
                            onClick = {
                                val takenAt = LocalDate.now().atTime(parsedTakenTime!!).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                                onTaken(takenAt)
                            },
                            enabled = parsedTakenTime != null && remaining > 0,
                            colors = ButtonDefaults.buttonColors(containerColor = accent)
                        ) { Text("LOG TAKEN", color = Ink, fontWeight = FontWeight.Black) }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onToggleActive) {
                        Text(if (medication.active) "HALT" else "RESTART")
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaceholderPanel(tab: Tab, accent: Color, modifier: Modifier = Modifier) {
    Card(modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xD91B1820)), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(tab.prompt, color = Color(0xFFCFC5CE), fontSize = 15.sp)
            Spacer(Modifier.height(9.dp))
            HorizontalDivider(Modifier.width(44.dp), color = accent)
            Spacer(Modifier.height(9.dp))
            Text("Scheduled for a later working circuit", color = Color(0xFF756D77), fontSize = 11.sp)
        }
    }
}

@Composable
private fun CalmBackground(effect: Effect, accent: Color) {
    val motion = rememberInfiniteTransition(label = "calm background")
    val phase by motion.animateFloat(0f, 1f, infiniteRepeatable(tween(if (effect == Effect.STARS) 7000 else 11000), RepeatMode.Restart), label = "atmosphere")
    Canvas(Modifier.fillMaxSize().alpha(.42f)) {
        val count = if (effect == Effect.STARS) 42 else 30
        repeat(count) { index ->
            val baseX = ((index * 83) % 101) / 100f * size.width
            val baseY = ((index * 47 + if (effect == Effect.SNOW) 19 else 0) % 103) / 102f * size.height
            val x = if (effect == Effect.SNOW) (baseX + phase * (12 + index % 7)) % size.width else baseX
            val y = if (effect == Effect.SNOW) (baseY + phase * size.height) % size.height else baseY
            val pulse = if (effect == Effect.STARS) .12f + ((phase + index * .17f) % 1f) * .32f else .3f
            drawCircle(if (effect == Effect.STARS) accent else Color.White, if (effect == Effect.STARS) 0.8f + index % 3 else 1.8f + index % 4, Offset(x, y), alpha = pulse)
        }
    }
}

private fun parseCapture(text: String): Pair<String, String> {
    val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
    val amountPattern = Regex("(?:£|GBP\\s*)?([0-9]+[.,][0-9]{2})(?![0-9])", RegexOption.IGNORE_CASE)
    val amounts = amountPattern.findAll(text).mapNotNull { match ->
        match.groupValues.getOrNull(1)?.replace(',', '.')?.toDoubleOrNull()?.let { it to match.value.trim() }
    }.toList()
    val amount = amounts.maxByOrNull { it.first }?.second.orEmpty()
    val merchant = lines.firstOrNull { line -> !amountPattern.containsMatchIn(line) && line.any(Char::isLetter) }?.take(60).orEmpty()
    return merchant to amount
}

private fun parseDoseTime(value: String): LocalTime? = try {
    LocalTime.parse(value.trim(), DateTimeFormatter.ofPattern("H:mm"))
} catch (_: DateTimeParseException) {
    null
}

private fun scheduledForToday(value: String): Long {
    val time = parseDoseTime(value) ?: LocalTime.MIDNIGHT
    return LocalDate.now().atTime(time).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

private fun moodColour(value: Float): Color = when {
    value < .33f -> lerp(Purple, Blue, value / .33f)
    value < .67f -> lerp(Blue, Fusion, (value - .33f) / .34f)
    else -> lerp(Fusion, Crimson, (value - .67f) / .33f)
}
