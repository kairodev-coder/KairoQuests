package dev.kairo.kairoquests.database

import java.util.UUID

class QuestRepository(private val database: DatabaseManager) {
    fun trackedQuest(playerId: UUID): String? {
        database.connection().use { connection ->
            connection.prepareStatement("SELECT quest_id FROM tracked_quests WHERE player_uuid = ?").use { statement ->
                statement.setString(1, playerId.toString())
                statement.executeQuery().use { result ->
                    return if (result.next()) result.getString("quest_id") else null
                }
            }
        }
    }

    fun setTrackedQuest(playerId: UUID, questId: String) {
        database.connection().use { connection ->
            connection.prepareStatement(
                """
                INSERT INTO tracked_quests(player_uuid, quest_id)
                VALUES(?, ?)
                ON CONFLICT(player_uuid) DO UPDATE SET quest_id = excluded.quest_id
                """.trimIndent()
            ).use { statement ->
                statement.setString(1, playerId.toString())
                statement.setString(2, questId)
                statement.executeUpdate()
            }
        }
    }

    fun clearTrackedQuest(playerId: UUID) {
        database.connection().use { connection ->
            connection.prepareStatement("DELETE FROM tracked_quests WHERE player_uuid = ?").use { statement ->
                statement.setString(1, playerId.toString())
                statement.executeUpdate()
            }
        }
    }
}
