package dev.kairo.kairoquests.model

import java.util.UUID

data class PlayerQuestStats(
    val playerId: UUID,
    var playerName: String,
    var completedDaily: Int,
    var completedWeekly: Int,
    var completedTotal: Int,
    var currentDailyStreak: Int,
    var bestDailyStreak: Int,
    var lastDailyCompletion: Long?
)
