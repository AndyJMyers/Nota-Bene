package com.notabene.app

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val Ink = Color(0xFF090812)
private val Glass = Color(0xFF28212B)
private val Purple = Color(0xFF321052)
private val Blue = Color(0xFF164B89)
private val Fusion = Color(0xFFF2C94C)
private val Crimson = Color(0xFF9D174D)

private enum class Tab(val shortLabel: String, val title: String, val prompt: String) {
    PAYMENTS("SPEND", "Spending", "Capture, check, then keep"),
    MEDICINE("MEDS", "Medicines", "Record medicine or prescription"),
    HEALTH("SOMA", "Soma", "Log a symptom or measurement"),
    TASKS("TASK", "To do & dependencies", "Add a task or dependency"),
    RESEARCH("ASK", "Further research", "Add a question to investigate")
}

private enum class Effect(val label: String) {
    STARS("✦ SKY"),
    SNOW("❄ SNOW"),
    OIL("◉ OIL"),
    WAVES("≋ WAVES");

    fun next(): Effect = entries[(ordinal + 1) % entries.size]
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MedicineReminderScheduler.prepare(applicationContext)
        setContent { NotaBeneApp() }
    }
}

@Composable
private fun NotaBeneApp() {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val backgroundInteraction = remember { MutableInteractionSource() }
    val scope = rememberCoroutineScope()
    val database = remember { NotaBeneDatabase.get(context) }
    var selected by rememberSaveable { mutableStateOf(Tab.PAYMENTS) }
    var mood by rememberSaveable { mutableFloatStateOf(.42f) }
    var effect by rememberSaveable { mutableStateOf(Effect.STARS) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var remindersGranted by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < 33 ||
                context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        )
    }
    val accent = moodColour(mood)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> remindersGranted = granted }
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri ->
        if (uri != null) scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { exportWorkbook(database, it) }
                        ?: error("Could not open the selected file")
                }
                Toast.makeText(context, "Nota Bene records exported", Toast.LENGTH_SHORT).show()
            } catch (_: Exception) {
                Toast.makeText(context, "Export failed", Toast.LENGTH_LONG).show()
            }
        }
    }

    MaterialTheme(colorScheme = darkColorScheme(primary = accent, surface = Glass, background = Ink)) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Ink)
                .clickable(
                    interactionSource = backgroundInteraction,
                    indication = null
                ) {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
        ) {
            CalmBackground(effect, accent, mood)
            Column(
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Header(
                    mood = mood,
                    accent = accent,
                    effect = effect,
                    onMoodChange = { mood = it },
                    onCycleEffect = { effect = effect.next() },
                    onSettings = {
                        remindersGranted = MedicineReminderScheduler.notificationsEnabled(context)
                        showSettings = true
                    }
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
            if (showSettings) {
                SettingsDialog(
                    accent = accent,
                    remindersGranted = remindersGranted,
                    onDismiss = { showSettings = false },
                    onRequestReminders = {
                        if (Build.VERSION.SDK_INT >= 33) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            remindersGranted = true
                        }
                    },
                    onExport = {
                        showSettings = false
                        exportLauncher.launch("nota-bene-${LocalDate.now()}.xlsx")
                    },
                    onErase = {
                        scope.launch {
                            withContext(Dispatchers.IO) { database.clearAllTables() }
                            MedicineReminderScheduler.eraseReminderData(context)
                            Toast.makeText(context, "All Nota Bene records erased", Toast.LENGTH_LONG).show()
                            showSettings = false
                        }
                    }
                )
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
    onCycleEffect: () -> Unit,
    onSettings: () -> Unit
) {
    Row(Modifier.fillMaxWidth().height(52.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Image(
            painter = painterResource(id = R.drawable.nb_fountain_icon),
            contentDescription = "Nota Bene",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(46.dp)
                .height(46.dp)
                .clip(RoundedCornerShape(11.dp))
                .border(1.dp, Color(0x99F2C94C), RoundedCornerShape(11.dp))
        )
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
                Text(effect.label, color = Color(0xFFC8BDC8), fontSize = 9.sp)
            }
            TextButton(onClick = onSettings, modifier = Modifier.width(30.dp).semantics { contentDescription = "Settings" }, contentPadding = ButtonDefaults.TextButtonContentPadding) {
                Text("*", color = Color(0xFFE9E0D2), fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SettingsDialog(
    accent: Color,
    remindersGranted: Boolean,
    onDismiss: () -> Unit,
    onRequestReminders: () -> Unit,
    onExport: () -> Unit,
    onErase: () -> Unit
) {
    var confirmErase by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF17131B),
        title = {
            Text("SETTINGS", color = accent, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        },
        text = {
            Column(
                Modifier.heightIn(max = 520.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("MEDS REMINDERS", color = accent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                Text(
                    "Nota Bene checks local schedules after a dose is due and again in the early evening if it is still unrecorded. Android may delay or suppress notifications because of battery, permission or device settings. Reminders are a helpful aid, not a substitute for paying attention to your prescription or medical advice.",
                    color = Color(0xFFC7BDC7),
                    fontSize = 12.sp
                )
                Button(
                    onClick = onRequestReminders,
                    enabled = !remindersGranted,
                    colors = ButtonDefaults.buttonColors(containerColor = accent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (remindersGranted) "REMINDERS ON" else "ENABLE REMINDERS", color = Ink, fontWeight = FontWeight.Black)
                }
                HorizontalDivider(color = Color(0xFF4B424D))
                Text("DATA", color = accent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                OutlinedButton(onClick = onExport, modifier = Modifier.fillMaxWidth()) {
                    Text("EXPORT XLSX")
                }
                if (confirmErase) {
                    Text("Erase every SPEND, MEDS, SOMA, TASK and ASK record on this device? Exported copies are not affected.", color = Color(0xFFE2B5C2), fontSize = 12.sp)
                    Button(
                        onClick = onErase,
                        colors = ButtonDefaults.buttonColors(containerColor = Crimson),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("CONFIRM ERASE ALL", fontWeight = FontWeight.Black) }
                    TextButton(onClick = { confirmErase = false }, modifier = Modifier.fillMaxWidth()) { Text("CANCEL") }
                } else {
                    TextButton(onClick = { confirmErase = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("ERASE ALL LOCAL DATA", color = Color(0xFFC98A9D))
                    }
                }
                HorizontalDivider(color = Color(0xFF4B424D))
                Text("PRIVACY & SAFETY", color = accent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                Text(
                    "YOUR RECORDS\nSPEND, MEDS, SOMA, TASK and ASK records stay in Nota Bene's local database. There is no Nota Bene account or server, and the developer cannot see them. Android cloud backup and device-transfer backup are disabled.",
                    color = Color(0xFFC7BDC7),
                    fontSize = 12.sp
                )
                Text(
                    "EXPORT\nRecords leave Nota Bene only when you deliberately export an XLSX workbook and choose where to save it. A cloud destination then applies its own privacy terms. Protect exports as sensitive data and delete them separately.",
                    color = Color(0xFFC7BDC7),
                    fontSize = 12.sp
                )
                Text(
                    "RECEIPTS & SPEECH\nReceipt images and recognised text are processed on-device and the image is not retained. Google's bundled ML Kit may send limited app, device, performance and installation diagnostics—not the receipt image or recognised text. Speech is handled by the recognition service installed on your phone; its provider may process audio under its own policy. Nota Bene keeps only text you accept.",
                    color = Color(0xFFC7BDC7),
                    fontSize = 12.sp
                )
                Text(
                    "REMINDERS & LOCK SCREEN\nMedicine checks run locally. Notification details are marked private; a generic message is shown until the phone is unlocked, subject to your Android lock-screen settings. Use a device screen lock.",
                    color = Color(0xFFC7BDC7),
                    fontSize = 12.sp
                )
                Text(
                    "DELETION\nErase all local data removes every record and reminder-state marker in Nota Bene. Previously exported copies must be deleted where you saved them.",
                    color = Color(0xFFC7BDC7),
                    fontSize = 12.sp
                )
                Text(
                    "MEDICAL LIMITS\nNota Bene is a personal recording and organisation tool, not a medical device. It does not diagnose, treat, cure or prevent any medical condition and does not give dose advice. Do not use it as your only essential reminder. Seek professional advice for medical questions and use emergency services when necessary.",
                    color = Color(0xFFE0D5DF),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "PUBLISHER\nDeveloped and published by Andy J Myers. Project and support: github.com/AndyJMyers/Nota-Bene",
                    color = Color(0xFFC7BDC7),
                    fontSize = 12.sp
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("DONE", color = accent) } }
    )
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

    Column(modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
            Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                Text("Nothing kept yet", color = Color(0xFF6F6771), fontSize = 13.sp)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                payments.forEach { payment ->
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

    Column(modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
            Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                Text(if (items.isEmpty()) "Nothing waiting to be investigated" else "All completed items are hidden", color = Color(0xFF6F6771), fontSize = 13.sp)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                visibleItems.forEach { item ->
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

    Column(modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
            Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                Text(if (tasks.isEmpty()) "Nothing waiting to be done" else "All completed tasks are hidden", color = Color(0xFF6F6771), fontSize = 13.sp)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                visibleTasks.forEach { task ->
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

    Column(modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xE61B1820)), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("NEW SOMA RECORD", color = accent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
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
        Text("SOMA HISTORY  ${observations.size}", color = Color(0xFF8F8790), fontSize = 10.sp, letterSpacing = 2.sp)
        if (observations.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                Text("No observations recorded", color = Color(0xFF6F6771), fontSize = 13.sp)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                observations.forEach { item ->
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

    Column(modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(9.dp)) {
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
            Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                Text("No medication schedules to show", color = Color(0xFF6F6771), fontSize = 13.sp)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                visibleMedications.forEach { medication ->
                    val medicationLogs = logs.filter { it.medicationId == medication.id }
                    MedicationRow(
                        medication = medication,
                        logs = medicationLogs,
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
                                MedicineReminderScheduler.cancelNotification(context, medication.id)
                            }
                        },
                        onToggleActive = {
                            scope.launch { dao.setActive(medication.id, !medication.active) }
                        },
                        onSetStock = { amountOnHand ->
                            scope.launch {
                                dao.setStartingDoses(medication.id, amountOnHand + medicationLogs.size)
                            }
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
    onToggleActive: () -> Unit,
    onSetStock: (Int) -> Unit
) {
    val today = LocalDate.now().toString()
    val todayLog = logs.firstOrNull { it.doseDate == today }
    val remaining = (medication.startingDoses - logs.size).coerceAtLeast(0)
    val scheduled = scheduledForToday(medication.doseTime)
    val clockFormat = remember { DateTimeFormatter.ofPattern("HH:mm") }
    var takenTime by remember(medication.id) { mutableStateOf(LocalTime.now().format(clockFormat)) }
    var expanded by remember(medication.id) { mutableStateOf(false) }
    var stockOnHand by remember(medication.id, remaining) { mutableStateOf(remaining.toString()) }
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
            Row(
                Modifier.fillMaxWidth().clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(medication.name, color = if (medication.active) Color(0xFFE6DEE6) else Color(0xFF777078), fontWeight = FontWeight.SemiBold)
                    Text("${medication.dosage}  ·  DAILY ${medication.doseTime}", color = Color(0xFF9A919B), fontSize = 10.sp)
                }
                Text(stateText, color = stateColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(if (expanded) "  ▲" else "  ▼", color = accent, fontSize = 9.sp)
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
                if (expanded) {
                    HorizontalDivider(color = Color(0xFF4B424D))
                    Text("STOCK", color = accent, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        PaymentField(
                            "Amount on hand",
                            stockOnHand,
                            { stockOnHand = it.filter(Char::isDigit) },
                            Modifier.weight(1f),
                            accent
                        )
                        Button(
                            onClick = { onSetStock(stockOnHand.toInt()) },
                            enabled = stockOnHand.toIntOrNull() != null,
                            colors = ButtonDefaults.buttonColors(containerColor = accent)
                        ) { Text("SET STOCK", color = Ink, fontWeight = FontWeight.Black) }
                    }
                    HorizontalDivider(color = Color(0xFF4B424D))
                    Text("DOSE HISTORY", color = accent, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                    if (logs.isEmpty()) {
                        Text("No doses recorded", color = Color(0xFF817881), fontSize = 11.sp)
                    } else {
                        logs.sortedByDescending { it.takenAt }.forEach { log ->
                            val taken = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(log.takenAt))
                            val planned = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(log.scheduledFor))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(taken, color = Color(0xFFD4CCD4), fontSize = 11.sp)
                                Text("due $planned", color = Color(0xFF8F8790), fontSize = 10.sp)
                            }
                        }
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
private fun CalmBackground(effect: Effect, accent: Color, mood: Float) {
    val motion = rememberInfiniteTransition(label = "calm background")
    val energy = when {
        mood < .33f -> .55f - (mood / .33f) * .2f
        else -> .35f + ((mood - .33f) / .67f) * .8f
    }
    val baseDuration = when (effect) {
        Effect.STARS -> 20_000
        Effect.SNOW -> 21_000
        Effect.OIL -> 30_000
        Effect.WAVES -> 24_000
    }
    val duration = (baseDuration / (.65f + energy * .55f)).toInt()
    val phase by motion.animateFloat(0f, 1f, infiniteRepeatable(tween(duration), RepeatMode.Restart), label = "atmosphere")
    val atmosphereAlpha = (.62f + energy * .18f).coerceAtMost(.88f)
    val animationSilver = Color(0xFFD4D9E1)
    val animationGold = Color(0xFFE6B94A)
    val animationRed = Color(0xFFC5214F)
    val animationPalette = listOf(animationSilver, animationGold, animationRed)
    Canvas(Modifier.fillMaxSize().alpha(atmosphereAlpha)) {
        when (effect) {
            Effect.STARS -> {
                repeat(74) { index ->
                    val x = ((index * 83) % 101) / 100f * size.width
                    val y = ((index * 47) % 103) / 102f * size.height
                    val pulse = .26f + ((phase + index * .17f) % 1f) * (.38f + energy * .22f)
                    drawCircle(animationPalette[index % 3], 1.2f + index % 4, Offset(x, y), alpha = pulse.coerceAtMost(.92f))
                }

                val novaPulse = (.5f + .5f * sin(phase * 2f * PI.toFloat()))
                val nova = Offset(size.width * .78f, size.height * .22f)
                drawCircle(animationRed, 12f + novaPulse * (22f + energy * 14f), nova, alpha = .12f + novaPulse * energy * .18f)
                drawCircle(animationGold, 4f + novaPulse * energy * 4f, nova, alpha = .66f + energy * .16f)
                repeat(8) { ray ->
                    val angle = ray * PI.toFloat() / 4f
                    val length = 17f + novaPulse * 27f
                    drawLine(animationSilver, nova, Offset(nova.x + cos(angle) * length, nova.y + sin(angle) * length), 1.8f, alpha = .18f + novaPulse * energy * .26f)
                }

                val comet = (phase * 2.2f) % 1f
                if (comet < .2f) {
                    val p = comet / .2f
                    val head = Offset(size.width * (.08f + p * .7f), size.height * (.2f + p * .2f))
                    drawLine(animationSilver, Offset(head.x - 105f, head.y - 48f), head, 3f, alpha = (.44f + energy * .3f) * (1f - p))
                    drawCircle(animationGold, 4.2f, head, alpha = (.62f + energy * .24f) * (1f - p))
                }
                val secondComet = ((phase + .57f) * 1.7f) % 1f
                if (secondComet < .14f) {
                    val p = secondComet / .14f
                    val head = Offset(size.width * (.35f + p * .45f), size.height * (.66f + p * .12f))
                    drawLine(animationRed, Offset(head.x - 82f, head.y - 31f), head, 2.5f, alpha = (.38f + energy * .28f) * (1f - p))
                }
            }

            Effect.SNOW -> repeat(48) { index ->
                val baseX = ((index * 83) % 101) / 100f * size.width
                val baseY = ((index * 47 + 19) % 103) / 102f * size.height
                val current = phase * 2f * PI.toFloat()
                val broadDrift = sin(current * (.55f + index % 4 * .08f) + index * .73f) * (18f + index % 6 * 5f)
                val gust = sin(current * (1.7f + index % 3 * .12f) + index * 1.31f) * (5f + energy * 9f)
                val x = (baseX + broadDrift + gust + size.width) % size.width
                val eddy = sin(current * 2.1f + index * .91f) * (34f + index % 5 * 8f) * (.55f + energy * .25f)
                val y = (baseY + phase * size.height + eddy + size.height) % size.height
                drawCircle(animationPalette[index % 3], 2.1f + index % 5, Offset(x, y), alpha = (.42f + (index % 4) * .07f) * (.72f + energy * .28f))
            }

            Effect.OIL -> {
                val colours = listOf(animationRed, animationGold, animationSilver, animationRed, animationGold, animationSilver)
                repeat(6) { index ->
                    val angle = phase * 2f * PI.toFloat() + index * 1.37f
                    val x = size.width * (.5f + .34f * sin(angle * (.45f + index * .05f)))
                    val y = size.height * (.5f + .38f * cos(angle * (.32f + index * .04f)))
                    val radius = size.minDimension * (.29f + index * .045f)
                    drawCircle(colours[index], radius * (.86f + energy * .18f), Offset(x, y), alpha = (.12f + index * .018f) * (.78f + energy * .34f))
                    drawCircle(lerp(colours[index], animationSilver, .28f), radius * .58f, Offset(x + radius * .2f, y - radius * .12f), alpha = .075f + energy * .035f)
                }
            }

            Effect.WAVES -> {
                val turn = phase * 2f * PI.toFloat()
                val seaBlue = lerp(Color(0xFF330815), animationRed, .42f)
                val greenRoom = lerp(animationRed, animationGold, .48f)
                val foam = animationSilver
                val horizon = size.height * .38f

                // The whole sea rolls sideways, so even the deep water is visibly travelling.
                repeat(3) { band ->
                    val swell = Path()
                    val top = horizon + size.height * band * .13f
                    val lift = (18f + band * 12f) * (.72f + energy * .32f)
                    val travel = phase * size.width * (.34f + band * .08f)
                    swell.moveTo(-size.width * .35f, top)
                    repeat(5) { crest ->
                        val left = -size.width * .35f + crest * size.width * .34f + travel % (size.width * .34f)
                        swell.cubicTo(left + size.width * .08f, top - lift, left + size.width * .17f, top - lift, left + size.width * .22f, top)
                        swell.cubicTo(left + size.width * .27f, top + lift * .6f, left + size.width * .31f, top + lift * .35f, left + size.width * .34f, top)
                    }
                    swell.lineTo(size.width, size.height)
                    swell.lineTo(0f, size.height)
                    swell.close()
                    drawPath(swell, lerp(seaBlue, animationGold, band * .12f), alpha = .31f + band * .065f)
                }

                // Two breakers move through a full life: swell, steepen, barrel, collapse, wash.
                repeat(2) { wave ->
                    val cycle = (phase + wave * .53f) % 1f
                    val life = sin(cycle * PI.toFloat()).coerceAtLeast(0f)
                    val barrel = if (cycle in .24f.. .72f) sin((cycle - .24f) / .48f * PI.toFloat()) else 0f
                    val collapse = ((cycle - .62f) / .38f).coerceIn(0f, 1f)
                    val crestX = size.width * (-.18f + cycle * 1.35f)
                    val waterline = size.height * (.67f + wave * .055f)
                    val rise = size.height * (.09f + .17f * barrel) * life
                    val shoulder = size.width * (.18f + .08f * life)

                    val body = Path().apply {
                        moveTo(crestX - shoulder * 1.8f, waterline)
                        cubicTo(crestX - shoulder, waterline - rise * .18f, crestX - shoulder * .58f, waterline - rise * .82f, crestX, waterline - rise)
                        cubicTo(crestX + shoulder * (.38f + barrel * .18f), waterline - rise * .96f, crestX + shoulder * (.55f + barrel * .3f), waterline - rise * .40f, crestX + shoulder, waterline)
                        lineTo(crestX + shoulder, waterline + size.height * .09f)
                        lineTo(crestX - shoulder * 1.8f, waterline + size.height * .09f)
                        close()
                    }
                    drawPath(body, lerp(seaBlue, greenRoom, .45f + barrel * .35f), alpha = .48f + life * .19f)

                    // The opening is a separate dark translucent hollow, not a closed emblem.
                    if (barrel > .12f) {
                        val hollow = Path().apply {
                            moveTo(crestX - shoulder * .18f, waterline - rise * .66f)
                            cubicTo(crestX + shoulder * .08f, waterline - rise * .88f, crestX + shoulder * .46f, waterline - rise * .70f, crestX + shoulder * .48f, waterline - rise * .34f)
                            cubicTo(crestX + shoulder * .29f, waterline - rise * .52f, crestX + shoulder * .08f, waterline - rise * .42f, crestX - shoulder * .18f, waterline - rise * .66f)
                            close()
                        }
                        drawPath(hollow, Color(0xFF24040E), alpha = barrel * .68f)
                    }

                    val lip = Path().apply {
                        moveTo(crestX - shoulder * .54f, waterline - rise * .72f)
                        cubicTo(crestX - shoulder * .15f, waterline - rise * 1.12f, crestX + shoulder * .38f, waterline - rise * (1.06f - collapse * .24f), crestX + shoulder * (.52f + collapse * .55f), waterline - rise * (.42f - collapse * .18f))
                    }
                    drawPath(lip, animationGold, alpha = .48f + life * .4f, style = Stroke(width = 3.4f + barrel * 3.4f))

                    repeat(7) { finger ->
                        val rootX = crestX - shoulder * .15f + finger * shoulder * .105f
                        val rootY = waterline - rise * (1f - finger * .035f)
                        val sprayThrow = barrel * (12f + finger % 3 * 7f) + collapse * (22f + finger * 3f)
                        val tip = Offset(rootX + sprayThrow, rootY - barrel * (10f + finger % 4 * 5f) + sin(turn * 1.4f + finger) * 4f)
                        drawLine(if (finger % 3 == 0) animationRed else foam, Offset(rootX, rootY), tip, 1.8f + finger % 3 * .7f, alpha = life * (.46f + energy * .2f))
                    }

                    // After the lip falls, white water runs ahead and broadens along the shore.
                    if (collapse > 0f) {
                        val whiteWater = Path().apply {
                            val front = crestX + shoulder * (.45f + collapse * 1.8f)
                            moveTo(crestX - shoulder * .25f, waterline)
                            cubicTo(crestX + shoulder * .35f, waterline - 18f * (1f - collapse), front - shoulder * .3f, waterline + 14f, front, waterline + 5f)
                        }
                        drawPath(whiteWater, animationSilver, alpha = collapse * .72f, style = Stroke(width = 4.5f + collapse * 6f))
                    }

                    repeat(10) { fleck ->
                        val sprayLife = (barrel + collapse * .7f).coerceAtMost(1f)
                        val x = crestX + (fleck - 3) * 9f + collapse * fleck * 8f
                        val y = waterline - rise - (fleck % 5) * 8f + sin(turn * 1.3f + fleck) * 7f
                        drawCircle(animationPalette[fleck % 3], 1.5f + fleck % 3 * .8f, Offset(x, y), alpha = sprayLife * .58f)
                    }
                }

                // A tiny square-rigger, heeled into the weather on the hostile horizon.
                val shipX = size.width * (.17f + sin(turn * .33f) * .012f)
                val shipY = horizon - 8f + sin(turn * .85f) * 9f
                val heel = 7f + sin(turn * .85f) * 4f
                val hull = Path().apply {
                    moveTo(shipX - 27f, shipY)
                    lineTo(shipX + 24f, shipY + heel)
                    lineTo(shipX + 15f, shipY + 16f + heel)
                    lineTo(shipX - 19f, shipY + 12f)
                    close()
                }
                drawPath(hull, Color(0xFF26040D), alpha = .9f)
                repeat(2) { mast ->
                    val mx = shipX - 9f + mast * 21f
                    val deckY = shipY + 5f + mast * 3f
                    val topY = deckY - 45f + mast * 8f
                    drawLine(animationGold, Offset(mx, deckY), Offset(mx + heel, topY), 1.8f, alpha = .82f)
                    val sail = Path().apply {
                        moveTo(mx + heel - 1f, topY + 5f)
                        lineTo(mx + heel + 16f, topY + 25f)
                        lineTo(mx + 4f, topY + 27f)
                        close()
                    }
                    drawPath(sail, lerp(animationSilver, animationGold, .34f), alpha = .72f)
                }

                // Broken shore foam in the foreground keeps the water visibly in motion.
                repeat(3) { line ->
                    val wash = Path()
                    val y = size.height * (.78f + line * .075f) + sin(turn + line) * (7f + energy * 3f)
                    wash.moveTo(-10f, y)
                    wash.cubicTo(size.width * .22f, y - 18f, size.width * .36f, y + 17f, size.width * .55f, y - 5f)
                    wash.cubicTo(size.width * .73f, y - 22f, size.width * .88f, y + 13f, size.width + 10f, y - 8f)
                    drawPath(
                        wash,
                        animationPalette[line],
                        alpha = .38f + line * .08f,
                        style = Stroke(width = 2.8f + line * .9f)
                    )
                }
            }
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
