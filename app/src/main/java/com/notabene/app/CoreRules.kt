package com.notabene.app

import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

internal enum class ConsumptionBand {
    NONE,
    WITHIN_USUAL,
    ONE_OVER,
    FURTHER_OVER
}

internal fun consumptionBand(loggedToday: Int, usualPerDay: Int): ConsumptionBand {
    require(loggedToday >= 0)
    require(usualPerDay > 0)
    return when {
        loggedToday == 0 -> ConsumptionBand.NONE
        loggedToday <= usualPerDay -> ConsumptionBand.WITHIN_USUAL
        loggedToday == usualPerDay + 1 -> ConsumptionBand.ONE_OVER
        else -> ConsumptionBand.FURTHER_OVER
    }
}

internal fun remainingDoses(startingDoses: Int, loggedDoses: Int): Int {
    require(startingDoses >= 0)
    require(loggedDoses >= 0)
    return (startingDoses - loggedDoses).coerceAtLeast(0)
}

internal enum class ReminderStage(val keyPart: String) {
    DUE("due"),
    EVENING("evening")
}

internal fun reminderStage(
    scheduledTime: LocalTime,
    currentTime: LocalTime,
    doseRecordedToday: Boolean,
    dueAlreadyNotified: Boolean,
    eveningAlreadyNotified: Boolean,
    eveningStarts: LocalTime = LocalTime.of(18, 0)
): ReminderStage? {
    if (doseRecordedToday || currentTime < scheduledTime) return null
    val stage = if (currentTime < eveningStarts) ReminderStage.DUE else ReminderStage.EVENING
    return when (stage) {
        ReminderStage.DUE -> stage.takeUnless { dueAlreadyNotified }
        ReminderStage.EVENING -> stage.takeUnless { eveningAlreadyNotified }
    }
}

internal fun parseCapture(text: String): Pair<String, String> {
    val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
    val amountPattern = Regex("(?:£|GBP\\s*)?([0-9]+[.,][0-9]{2})(?![0-9])", RegexOption.IGNORE_CASE)
    val amounts = amountPattern.findAll(text).mapNotNull { match ->
        match.groupValues.getOrNull(1)?.replace(',', '.')?.toDoubleOrNull()?.let { it to match.value.trim() }
    }.toList()
    val amount = amounts.maxByOrNull { it.first }?.second.orEmpty()
    val merchant = lines.firstOrNull { line -> !amountPattern.containsMatchIn(line) && line.any(Char::isLetter) }?.take(60).orEmpty()
    return merchant to amount
}

internal fun parseDoseTime(value: String): LocalTime? = try {
    LocalTime.parse(value.trim(), DateTimeFormatter.ofPattern("H:mm").withResolverStyle(ResolverStyle.STRICT))
} catch (_: DateTimeParseException) {
    null
}
