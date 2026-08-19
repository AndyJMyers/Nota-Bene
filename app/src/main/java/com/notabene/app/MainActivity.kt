package com.notabene.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

private val Ink = Color(0xFF090812)
private val Glass = Color(0xFF28212B)
private val Purple = Color(0xFF321052)
private val Blue = Color(0xFF164B89)
private val Fusion = Color(0xFFF2C94C)
private val Crimson = Color(0xFF9D174D)

private enum class Tab(val shortLabel: String, val title: String, val prompt: String) {
    PAYMENTS("PAY", "Payments", "Record a payment"),
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
                Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Header(effect) { effect = if (effect == Effect.STARS) Effect.SNOW else Effect.STARS }
                InstrumentTabs(selected, accent) { selected = it }
                AnimatedContent(
                    targetState = selected,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tab title"
                ) { tab ->
                    Text(
                        tab.title.uppercase(),
                        color = accent,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 4.sp
                    )
                }
                CapturePanel(selected, accent)
                Spacer(Modifier.weight(1f))
                MoodControl(mood, accent) { mood = it }
            }
        }
    }
}

@Composable
private fun Header(effect: Effect, onCycle: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("NOTA BENE", color = Color(0xFFE9E0D2), fontSize = 28.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
            Text("PERSONAL OPERATIONS LOG", color = Color(0xFF8F8790), fontSize = 10.sp, letterSpacing = 2.sp)
        }
        TextButton(onClick = onCycle) {
            Text(if (effect == Effect.STARS) "✦ SKY" else "❄ SNOW", color = Color(0xFFC8BDC8), fontSize = 12.sp)
        }
    }
}

@Composable
private fun InstrumentTabs(selected: Tab, accent: Color, onSelect: (Tab) -> Unit) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Tab.entries.forEach { tab ->
            val active = tab == selected
            val glow by animateFloatAsState(if (active) 1f else .18f, label = "filament glow")
            Box(
                Modifier
                    .width(72.dp).height(48.dp)
                    .background(
                        Brush.verticalGradient(listOf(lerp(Ink, accent, glow * .55f), Glass, Ink)),
                        RoundedCornerShape(7.dp)
                    )
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
private fun CapturePanel(tab: Tab, accent: Color) {
    var note by remember(tab) { mutableStateOf("") }
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xD91B1820)), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(tab.prompt, color = Color(0xFFCFC5CE), fontSize = 14.sp)
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                placeholder = { Text("Type or speak naturally…") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accent,
                    cursorColor = accent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = accent)) { Text("SAVE", color = Ink, fontWeight = FontWeight.Black) }
                OutlinedButton(onClick = {}) { Text("VOICE") }
                if (tab == Tab.PAYMENTS) OutlinedButton(onClick = {}) { Text("PHOTO") }
            }
            AnimatedVisibility(note.isEmpty()) {
                Text("Prototype controls — storage and capture follow in the next milestone.", color = Color(0xFF827A84), fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun MoodControl(value: Float, accent: Color, onChange: (Float) -> Unit) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("MOOD / ATMOSPHERE", color = Color(0xFF8F8790), fontSize = 10.sp, letterSpacing = 2.sp)
            Text("${(value * 100).roundToInt()}", color = accent, fontSize = 11.sp)
        }
        Slider(value = value, onValueChange = onChange, colors = SliderDefaults.colors(thumbColor = accent, activeTrackColor = accent))
    }
}

@Composable
private fun CalmBackground(effect: Effect, accent: Color) {
    Canvas(Modifier.fillMaxSize().alpha(.42f)) {
        val count = if (effect == Effect.STARS) 42 else 30
        repeat(count) { index ->
            val x = ((index * 83) % 101) / 100f * size.width
            val y = ((index * 47 + if (effect == Effect.SNOW) 19 else 0) % 103) / 102f * size.height
            val radius = if (effect == Effect.STARS) 0.8f + index % 3 else 1.8f + index % 4
            drawCircle(if (effect == Effect.STARS) accent else Color.White, radius, Offset(x, y), alpha = .15f + (index % 5) * .06f)
        }
    }
}

private fun moodColour(value: Float): Color = when {
    value < .33f -> lerp(Purple, Blue, value / .33f)
    value < .67f -> lerp(Blue, Fusion, (value - .33f) / .34f)
    else -> lerp(Fusion, Crimson, (value - .67f) / .33f)
}

