package dev.kairo.kairoquests.database

class MigrationManager(private val database: DatabaseManager) {
    fun migrate() {
        database.connection().use { connection ->
            connection.createStatement().use { statement ->
                statement.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS kairoquests_schema (
                        version INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                statement.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS player_quest_progress (
                        player_uuid TEXT NOT NULL,
                        player_name TEXT NOT NULL,
                        quest_id TEXT NOT NULL,
                        progress INTEGER NOT NULL,
                        status TEXT NOT NULL,
                        started_at INTEGER NOT NULL,
                        completed_at INTEGER,
                        claimed_at INTEGER,
                        reset_at INTEGER,
                        PRIMARY KEY (player_uuid, quest_id)
                    )
                    """.trimIndent()
                )
                statement.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS player_quest_stats (
                        player_uuid TEXT PRIMARY KEY,
                        player_name TEXT NOT NULL,
                        completed_daily INTEGER NOT NULL DEFAULT 0,
                        completed_weekly INTEGER NOT NULL DEFAULT 0,
                        completed_total INTEGER NOT NULL DEFAULT 0,
                        current_daily_streak INTEGER NOT NULL DEFAULT 0,
                        best_daily_streak INTEGER NOT NULL DEFAULT 0,
                        last_daily_completion INTEGER
                    )
                    """.trimIndent()
                )
                statement.executeUpdate(
                    """
                    CREATE TABLE IF NOT EXISTS tracked_quests (
                        player_uuid TEXT PRIMARY KEY,
                        quest_id TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_kairoquests_player_uuid ON player_quest_progress(player_uuid)")
                statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_kairoquests_quest_id ON player_quest_progress(quest_id)")
                statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_kairoquests_status ON player_quest_progress(status)")
                statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_kairoquests_reset_at ON player_quest_progress(reset_at)")
                val count = statement.executeQuery("SELECT COUNT(*) FROM kairoquests_schema").use { result ->
                    if (result.next()) result.getInt(1) else 0
                }
                if (count == 0) {
                    statement.executeUpdate("INSERT INTO kairoquests_schema(version) VALUES(1)")
                }
            }
        }
    }
}
