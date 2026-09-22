package com.notabene.app

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Small collection symbols, drawn in the active mood's materials and colours. */
@Composable
internal fun EmptyCollectionArtwork(kind: CollectionKind, accent: Color) {
    val spec = LocalNotaStyle.current.spec
    Canvas(Modifier.fillMaxWidth().height(112.dp)) {
        val u = size.height / 112f
        val cx = size.width / 2f
        val cy = size.height / 2f
        val frame = spec.frame.copy(alpha = .84f)
        val light = spec.glow.copy(alpha = .88f)
        val signal = spec.secondary.copy(alpha = .8f)
        val plate = Offset(cx - 66f * u, cy - 45f * u)
        val plateSize = Size(132f * u, 90f * u)

        drawCircle(accent, 53f * u, Offset(cx, cy), alpha = .07f)
        drawRoundRect(spec.surface.copy(alpha = .78f), plate, plateSize, CornerRadius(13f * u))
        drawRoundRect(frame, plate, plateSize, CornerRadius(13f * u), style = Stroke(1.5f * u))
        drawLine(frame, Offset(cx - 47f * u, cy + 31f * u), Offset(cx + 47f * u, cy + 31f * u), 1f * u)
        listOf(-1f, 1f).forEach { side ->
            drawCircle(signal, 2.3f * u, Offset(cx + side * 55f * u, cy - 34f * u))
        }

        when (kind) {
            CollectionKind.TODO -> {
                repeat(3) { row ->
                    val y = cy - 24f * u + row * 20f * u
                    drawRoundRect(frame, Offset(cx - 42f * u, y - 4f * u), Size(9f * u, 9f * u), CornerRadius(2f * u), style = Stroke(1.8f * u))
                    drawLine(light, Offset(cx - 25f * u, y), Offset(cx + (if (row == 2) 18f else 36f) * u, y), 2f * u)
                    if (row == 0) {
                        drawLine(signal, Offset(cx - 42f * u, y), Offset(cx - 38f * u, y + 4f * u), 2.3f * u)
                        drawLine(signal, Offset(cx - 38f * u, y + 4f * u), Offset(cx - 31f * u, y - 6f * u), 2.3f * u)
                    }
                }
            }
            CollectionKind.LOG -> {
                drawCircle(frame, 26f * u, Offset(cx, cy - 4f * u), style = Stroke(1.8f * u))
                val inkLine = Path().apply {
                    moveTo(cx - 28f * u, cy + 13f * u)
                    cubicTo(cx - 7f * u, cy - 13f * u, cx + 4f * u, cy + 14f * u, cx + 28f * u, cy - 12f * u)
                }
                drawPath(inkLine, light, style = Stroke(2.4f * u))
                drawCircle(signal, 3.5f * u, Offset(cx + 28f * u, cy - 12f * u))
                repeat(3) { dot -> drawCircle(frame, 1.8f * u, Offset(cx - 12f * u + dot * 12f * u, cy + 19f * u)) }
            }
            CollectionKind.RECORD -> {
                drawRoundRect(frame, Offset(cx - 34f * u, cy - 29f * u), Size(66f * u, 53f * u), CornerRadius(3f * u), style = Stroke(1.5f * u))
                drawRoundRect(light, Offset(cx - 27f * u, cy - 24f * u), Size(66f * u, 53f * u), CornerRadius(3f * u), style = Stroke(2f * u))
                drawCircle(signal, 5f * u, Offset(cx + 19f * u, cy - 13f * u))
                val landscape = Path().apply {
                    moveTo(cx - 19f * u, cy + 17f * u)
                    lineTo(cx - 5f * u, cy + 1f * u)
                    lineTo(cx + 4f * u, cy + 10f * u)
                    lineTo(cx + 15f * u, cy - 1f * u)
                    lineTo(cx + 31f * u, cy + 17f * u)
                }
                drawPath(landscape, light, style = Stroke(2f * u))
            }
            CollectionKind.REPEAT -> {
                drawArc(frame, 25f, 295f, false, Offset(cx - 30f * u, cy - 30f * u), Size(60f * u, 60f * u), style = Stroke(2f * u))
                drawArc(light, 205f, 255f, false, Offset(cx - 22f * u, cy - 22f * u), Size(44f * u, 44f * u), style = Stroke(2.5f * u))
                drawCircle(signal, 6f * u, Offset(cx + 17f * u, cy - 19f * u))
                drawCircle(light, 3f * u, Offset(cx, cy))
                drawLine(light, Offset(cx + 26f * u, cy - 16f * u), Offset(cx + 36f * u, cy - 17f * u), 2f * u)
                drawLine(light, Offset(cx + 26f * u, cy - 16f * u), Offset(cx + 30f * u, cy - 7f * u), 2f * u)
            }
        }
    }
}

/** A short, non-blocking seal after a save or completion has actually been stored. */
@Composable
internal fun CompletionFlourish(pulse: Int, accent: Color, modifier: Modifier = Modifier) {
    val spec = LocalNotaStyle.current.spec
    val progress = remember { Animatable(1f) }
    LaunchedEffect(pulse) {
        if (pulse > 0) {
            progress.snapTo(0f)
            progress.animateTo(1f, tween(950))
        }
    }
    if (pulse == 0 || progress.value >= 1f) return

    Canvas(modifier) {
        val p = progress.value
        val opacity = (p / .14f).coerceAtMost(1f) * (1f - p)
        val centre = Offset(size.width - 36.dp.toPx(), size.height / 2f)
        val radius = 72.dp.toPx() * (.13f + p * .25f)
        drawCircle(spec.glow, radius, centre, alpha = opacity * .75f, style = Stroke(2.4f))
        drawCircle(accent, radius * .55f, centre, alpha = opacity * .22f)
        drawCircle(spec.secondary, 4f, centre, alpha = opacity)
        repeat(8) { ray ->
            val angle = ray * PI.toFloat() / 4f
            val inner = radius + 5f
            val outer = inner + (1f - p) * 12f
            drawLine(
                if (ray % 2 == 0) spec.glow else spec.secondary,
                Offset(centre.x + cos(angle) * inner, centre.y + sin(angle) * inner),
                Offset(centre.x + cos(angle) * outer, centre.y + sin(angle) * outer),
                2f,
                alpha = opacity * .85f
            )
        }
    }
}
