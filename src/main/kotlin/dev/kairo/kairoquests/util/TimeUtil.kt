package dev.kairo.kairoquests.util

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.time.DayOfWeek

object TimeUtil {
    fun nowSeconds(): Long = Instant.now().epochSecond

    fun formatDuration(seconds: Long): String {
        val duration = Duration.ofSeconds(seconds.coerceAtLeast(0))
        val days = duration.toDays()
        val hours = duration.toHoursPart()
        val minutes = duration.toMinutesPart()
        return when {
            days > 0 -> "${days}d ${hours}h"
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "${duration.toSecondsPart()}s"
        }
    }

    fun nextDailyReset(time: String, zone: ZoneId = ZoneId.systemDefault()): Long {
        val resetTime = runCatching { LocalTime.parse(time) }.getOrDefault(LocalTime.MIDNIGHT)
        var reset = LocalDateTime.of(LocalDate.now(zone), resetTime)
        if (!reset.atZone(zone).toInstant().isAfter(Instant.now())) {
            reset = reset.plusDays(1)
        }
        return reset.atZone(zone).toEpochSecond()
    }

    fun nextWeeklyReset(day: String, time: String, zone: ZoneId = ZoneId.systemDefault()): Long {
        val resetDay = runCatching { DayOfWeek.valueOf(day.uppercase()) }.getOrDefault(DayOfWeek.MONDAY)
        val resetTime = runCatching { LocalTime.parse(time) }.getOrDefault(LocalTime.MIDNIGHT)
        var reset = LocalDate.now(zone).with(TemporalAdjusters.nextOrSame(resetDay)).atTime(resetTime)
        if (!reset.atZone(zone).toInstant().isAfter(Instant.now())) {
            reset = reset.plusWeeks(1)
        }
        return reset.atZone(zone).toEpochSecond()
    }
}
