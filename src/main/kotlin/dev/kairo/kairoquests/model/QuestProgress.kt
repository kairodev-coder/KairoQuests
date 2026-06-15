package dev.kairo.kairoquests.model

import java.util.UUID

data class QuestProgress(
    val playerId: UUID,
    var playerName: String,
    val questId: String,
    var progress: Int,
    var status: QuestStatus,
    var startedAt: Long,
    var completedAt: Long?,
    var claimedAt: Long?,
    var resetAt: Long?
)
