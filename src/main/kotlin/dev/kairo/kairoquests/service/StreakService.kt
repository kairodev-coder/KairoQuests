package dev.kairo.kairoquests.service

import dev.kairo.kairoquests.model.PlayerQuestStats
import dev.kairo.kairoquests.util.TimeUtil
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class StreakService {
    fun markDailyCompletion(stats: PlayerQuestStats) {
        val today = LocalDate.now()
        val last = stats.lastDailyCompletion?.let { Instant.ofEpochSecond(it).atZone(ZoneId.systemDefault()).toLocalDate() }
        stats.currentDailyStreak = when {
            last?.isEqual(today) == true -> stats.currentDailyStreak
            last?.isEqual(today.minusDays(1)) == true -> stats.currentDailyStreak + 1
            else -> 1
        }
        stats.bestDailyStreak = maxOf(stats.bestDailyStreak, stats.currentDailyStreak)
        stats.lastDailyCompletion = TimeUtil.nowSeconds()
    }
}
