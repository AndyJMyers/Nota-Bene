package com.notabene.app

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

/** Use an explicit offset through the repeated autumn hour; reject spring gaps and future events. */
internal fun repeatHistoryTime(local: LocalDateTime, zone: ZoneId, preferred: ZoneOffset?, now: Long): Long {
    val offsets = zone.rules.getValidOffsets(local)
    require(offsets.isNotEmpty()) { "That time did not occur because the clocks changed. Choose another time." }
    val offset = preferred?.takeIf { it in offsets } ?: offsets.first()
    val result = local.toInstant(offset).toEpochMilli()
    require(result <= now) { "Choose a time that has already happened." }
    return result
}
