package com.notabene.app

import org.junit.Assert.assertEquals
import org.junit.Test

class NotaStyleTest {
    @Test
    fun nextStyleVisitsEveryStyleAndWraps() {
        var style = NotaStyle.RETRO_FUTURIST
        val visited = mutableSetOf<NotaStyle>()
        repeat(NotaStyle.entries.size) {
            visited += style
            style = style.next()
        }
        assertEquals(NotaStyle.entries.toSet(), visited)
        assertEquals(NotaStyle.RETRO_FUTURIST, style)
    }

    @Test
    fun corruptStoredStyleFallsBackSafely() {
        assertEquals(NotaStyle.RETRO_FUTURIST, NotaStyle.fromStored("NOT_A_STYLE"))
        assertEquals(NotaStyle.ART_NOUVEAU, NotaStyle.fromStored("ART_NOUVEAU"))
    }
}
