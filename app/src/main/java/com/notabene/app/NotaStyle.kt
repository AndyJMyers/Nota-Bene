package com.notabene.app

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.runtime.staticCompositionLocalOf

internal enum class NotaStyle(val displayName: String) {
    RETRO_FUTURIST("RETRO FUTURIST"),
    STEAMPUNK("STEAMPUNK"),
    ECCLESIASTIC("ECCLESIASTIC"),
    COSMIC_FUNK("COSMIC FUNK"),
    ORBITAL_DECO("ORBITAL DECO"),
    ART_NOUVEAU("ART NOUVEAU"),
    WILLIAM_MORRIS("WILLIAM MORRIS");

    fun next(): NotaStyle = entries[(ordinal + 1) % entries.size]

    companion object {
        fun fromStored(value: String?): NotaStyle = entries.firstOrNull { it.name == value } ?: RETRO_FUTURIST
    }
}

internal data class NotaStyleSpec(
    val ink: Color,
    val surface: Color,
    val panel: Color,
    val text: Color,
    val muted: Color,
    val panelText: Color,
    val panelMuted: Color,
    val frame: Color,
    val glow: Color,
    val secondary: Color,
    val titleFamily: FontFamily,
    val corner: Int,
    val border: Int
)

internal val NotaStyle.spec: NotaStyleSpec
    get() = when (this) {
        NotaStyle.RETRO_FUTURIST -> NotaStyleSpec(
            ink = Color(0xFF090812), surface = Color(0xFF17131B), panel = Color(0xFF211B24),
            text = Color(0xFFE9E0E8), muted = Color(0xFFA79DA8), panelText = Color(0xFFE9E0E8), panelMuted = Color(0xFFA79DA8), frame = Color(0xFF514653),
            glow = Color(0xFFF2C94C), secondary = Color(0xFF9D174D), titleFamily = FontFamily.SansSerif, corner = 8, border = 2
        )
        NotaStyle.STEAMPUNK -> NotaStyleSpec(
            ink = Color(0xFF100A06), surface = Color(0xFF24170E), panel = Color(0xFFF0DEC0),
            text = Color(0xFFF1DFC0), muted = Color(0xFFB8A17E), panelText = Color(0xFF21140C), panelMuted = Color(0xFF6C5840), frame = Color(0xFF8A632E),
            glow = Color(0xFFE8A735), secondary = Color(0xFF7C2E20), titleFamily = FontFamily.Serif, corner = 3, border = 3
        )
        NotaStyle.ECCLESIASTIC -> NotaStyleSpec(
            ink = Color(0xFF040817), surface = Color(0xFF0B1731), panel = Color(0xFFF1E5C9),
            text = Color(0xFFF3E6C7), muted = Color(0xFFAEB7CF), panelText = Color(0xFF08162D), panelMuted = Color(0xFF596379), frame = Color(0xFF9A7938),
            glow = Color(0xFFE8C459), secondary = Color(0xFF9E1734), titleFamily = FontFamily.Serif, corner = 4, border = 2
        )
        NotaStyle.COSMIC_FUNK -> NotaStyleSpec(
            ink = Color(0xFF010104), surface = Color(0xFF0A0A10), panel = Color(0xFF120A0D),
            text = Color(0xFFFFEAC2), muted = Color(0xFFC7BBA8), panelText = Color(0xFFFFEAC2), panelMuted = Color(0xFFC7BBA8), frame = Color(0xFFB8B4B8),
            glow = Color(0xFFFFB000), secondary = Color(0xFFE51B48), titleFamily = FontFamily.SansSerif, corner = 18, border = 3
        )
        NotaStyle.ORBITAL_DECO -> NotaStyleSpec(
            ink = Color(0xFF040404), surface = Color(0xFF11100F), panel = Color(0xFFF0E5CF),
            text = Color(0xFFF2E7D4), muted = Color(0xFFBAB0A2), panelText = Color(0xFF11100F), panelMuted = Color(0xFF5B554C), frame = Color(0xFFAA8B5A),
            glow = Color(0xFFDDBB73), secondary = Color(0xFF8E1526), titleFamily = FontFamily.SansSerif, corner = 2, border = 2
        )
        NotaStyle.ART_NOUVEAU -> NotaStyleSpec(
            ink = Color(0xFF061B1A), surface = Color(0xFF0C2C29), panel = Color(0xFFF1E4C6),
            text = Color(0xFFF4E8CD), muted = Color(0xFFB8C2AE), panelText = Color(0xFF082725), panelMuted = Color(0xFF536861), frame = Color(0xFFB5893E),
            glow = Color(0xFFE4BC68), secondary = Color(0xFF8F2532), titleFamily = FontFamily.Serif, corner = 20, border = 2
        )
        NotaStyle.WILLIAM_MORRIS -> NotaStyleSpec(
            ink = Color(0xFF071315), surface = Color(0xFF142622), panel = Color(0xFFEDE0BC),
            text = Color(0xFFF3E7C8), muted = Color(0xFFB9B397), panelText = Color(0xFF122522), panelMuted = Color(0xFF5B6657), frame = Color(0xFF9A7735),
            glow = Color(0xFFD0A646), secondary = Color(0xFF8C342B), titleFamily = FontFamily.Serif, corner = 4, border = 2
        )
    }

internal val LocalNotaStyle = staticCompositionLocalOf { NotaStyle.RETRO_FUTURIST }
