package com.notabene.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.LocalTime

class CoreRulesTest {
    @Test
    fun `zero entries is the white band`() {
        assertEquals(ConsumptionBand.NONE, consumptionBand(0, 2))
    }

    @Test
    fun `entries through usual count are green`() {
        assertEquals(ConsumptionBand.WITHIN_USUAL, consumptionBand(1, 2))
        assertEquals(ConsumptionBand.WITHIN_USUAL, consumptionBand(2, 2))
    }

    @Test
    fun `one above usual is amber`() {
        assertEquals(ConsumptionBand.ONE_OVER, consumptionBand(3, 2))
    }

    @Test
    fun `further above usual is red`() {
        assertEquals(ConsumptionBand.FURTHER_OVER, consumptionBand(4, 2))
        assertEquals(ConsumptionBand.FURTHER_OVER, consumptionBand(12, 2))
    }

    @Test
    fun `invalid counts are rejected rather than silently coloured`() {
        assertThrows(IllegalArgumentException::class.java) { consumptionBand(-1, 1) }
        assertThrows(IllegalArgumentException::class.java) { consumptionBand(1, 0) }
    }

    @Test
    fun `stock decreases for every log and never becomes negative`() {
        assertEquals(10, remainingDoses(10, 0))
        assertEquals(8, remainingDoses(10, 2))
        assertEquals(0, remainingDoses(2, 7))
    }

    @Test
    fun `receipt parser chooses first merchant line and largest amount`() {
        val (merchant, amount) = parseCapture("Corner Shop\nSubtotal £12.30\nTOTAL £14.75")
        assertEquals("Corner Shop", merchant)
        assertEquals("£14.75", amount)
    }

    @Test
    fun `receipt parser accepts GBP and comma decimal`() {
        val (merchant, amount) = parseCapture("LE PETIT MARCHE\nGBP 8,40")
        assertEquals("LE PETIT MARCHE", merchant)
        assertEquals("GBP 8,40", amount)
    }

    @Test
    fun `receipt parser remains useful when no amount exists`() {
        val (merchant, amount) = parseCapture("The Old Bakery\nThank you")
        assertEquals("The Old Bakery", merchant)
        assertEquals("", amount)
    }

    @Test
    fun `dose time accepts compact valid times`() {
        assertEquals(LocalTime.of(8, 5), parseDoseTime("8:05"))
        assertEquals(LocalTime.of(23, 59), parseDoseTime(" 23:59 "))
    }

    @Test
    fun `dose time rejects invalid input`() {
        assertNull(parseDoseTime("24:00"))
        assertNull(parseDoseTime("breakfast"))
        assertNull(parseDoseTime(""))
    }
}
