package dev.kairo.kairoquests.database

import dev.kairo.kairoquests.model.PlayerQuestStats
import dev.kairo.kairoquests.model.QuestProgress
import dev.kairo.kairoquests.model.QuestStatus
import java.util.UUID

class PlayerQuestRepository(private val database: DatabaseManager) {
    fun loadProgress(playerId: UUID): List<QuestProgress> {
        database.connection().use { connection ->
            connection.prepareStatement("SELECT * FROM player_quest_progress WHERE player_uuid = ?").use { statement ->
                statement.setString(1, playerId.toString())
                statement.executeQuery().use { result ->
                    val progress = mutableListOf<QuestProgress>()
                    while (result.next()) {
                        progress += QuestProgress(
                            playerId = UUID.fromString(result.getString("player_uuid")),
                            playerName = result.getString("player_name"),
                            questId = result.getString("quest_id"),
                            progress = result.getInt("progress"),
                            status = QuestStatus.valueOf(result.getString("status")),
                            startedAt = result.getLong("started_at"),
                            completedAt = result.getLongOrNull("completed_at"),
                            claimedAt = result.getLongOrNull("claimed_at"),
                            resetAt = result.getLongOrNull("reset_at")
                        )
                    }
                    return progress
                }
            }
        }
    }

    fun saveProgress(progress: QuestProgress) {
        database.connection().use { connection ->
            connection.prepareStatement(
                """
                INSERT INTO player_quest_progress(
                    player_uuid, player_name, quest_id, progress, status, started_at, completed_at, claimed_at, reset_at
                ) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(player_uuid, quest_id) DO UPDATE SET
                    player_name = excluded.player_name,
                    progress = excluded.progress,
                    status = excluded.status,
                    started_at = excluded.started_at,
                    completed_at = excluded.completed_at,
                    claimed_at = excluded.claimed_at,
                    reset_at = excluded.reset_at
                """.trimIndent()
            ).use { statement ->
                statement.setString(1, progress.playerId.toString())
                statement.setString(2, progress.playerName)
                statement.setString(3, progress.questId)
                statement.setInt(4, progress.progress)
                statement.setString(5, progress.status.name)
                statement.setLong(6, progress.startedAt)
                statement.setNullableLong(7, progress.completedAt)
                statement.setNullableLong(8, progress.claimedAt)
                statement.setNullableLong(9, progress.resetAt)
                statement.executeUpdate()
            }
        }
    }

    fun deleteProgress(playerId: UUID, questId: String? = null) {
        database.connection().use { connection ->
            val sql = if (questId == null) {
                "DELETE FROM player_quest_progress WHERE player_uuid = ?"
            } else {
                "DELETE FROM player_quest_progress WHERE player_uuid = ? AND quest_id = ?"
            }
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, playerId.toString())
                questId?.let { statement.setString(2, it) }
                statement.executeUpdate()
            }
        }
    }

    fun loadStats(playerId: UUID, playerName: String): PlayerQuestStats {
        database.connection().use { connection ->
            connection.prepareStatement("SELECT * FROM player_quest_stats WHERE player_uuid = ?").use { statement ->
                statement.setString(1, playerId.toString())
                statement.executeQuery().use { result ->
                    if (result.next()) {
                        return PlayerQuestStats(
                            playerId = playerId,
                            playerName = result.getString("player_name"),
                            completedDaily = result.getInt("completed_daily"),
                            completedWeekly = result.getInt("completed_weekly"),
                            completedTotal = result.getInt("completed_total"),
                            currentDailyStreak = result.getInt("current_daily_streak"),
                            bestDailyStreak = result.getInt("best_daily_streak"),
                            lastDailyCompletion = result.getLongOrNull("last_daily_completion")
                        )
                    }
                }
            }
        }
        return PlayerQuestStats(playerId, playerName, 0, 0, 0, 0, 0, null)
    }

    fun saveStats(stats: PlayerQuestStats) {
        database.connection().use { connection ->
            connection.prepareStatement(
                """
                INSERT INTO player_quest_stats(
                    player_uuid, player_name, completed_daily, completed_weekly, completed_total,
                    current_daily_streak, best_daily_streak, last_daily_completion
                ) VALUES(?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(player_uuid) DO UPDATE SET
                    player_name = excluded.player_name,
                    completed_daily = excluded.completed_daily,
                    completed_weekly = excluded.completed_weekly,
                    completed_total = excluded.completed_total,
                    current_daily_streak = excluded.current_daily_streak,
                    best_daily_streak = excluded.best_daily_streak,
                    last_daily_completion = excluded.last_daily_completion
                """.trimIndent()
            ).use { statement ->
                statement.setString(1, stats.playerId.toString())
                statement.setString(2, stats.playerName)
                statement.setInt(3, stats.completedDaily)
                statement.setInt(4, stats.completedWeekly)
                statement.setInt(5, stats.completedTotal)
                statement.setInt(6, stats.currentDailyStreak)
                statement.setInt(7, stats.bestDailyStreak)
                statement.setNullableLong(8, stats.lastDailyCompletion)
                statement.executeUpdate()
            }
        }
    }

    private fun java.sql.ResultSet.getLongOrNull(column: String): Long? {
        val value = getLong(column)
        return if (wasNull()) null else value
    }

    private fun java.sql.PreparedStatement.setNullableLong(index: Int, value: Long?) {
        if (value == null) setNull(index, java.sql.Types.INTEGER) else setLong(index, value)
    }
}
