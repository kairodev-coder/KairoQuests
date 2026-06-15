package dev.kairo.kairoquests.service

import dev.kairo.kairoquests.config.ConfigManager
import dev.kairo.kairoquests.model.Quest
import dev.kairo.kairoquests.model.QuestResetType
import dev.kairo.kairoquests.util.TimeUtil

class ResetService(private val configManager: ConfigManager) {
    fun nextReset(quest: Quest): Long? {
        return when (quest.resetType) {
            QuestResetType.NONE -> null
            QuestResetType.DAILY -> TimeUtil.nextDailyReset(configManager.main.config.getString("quests.daily-reset-time", "00:00") ?: "00:00")
            QuestResetType.WEEKLY -> TimeUtil.nextWeeklyReset(
                configManager.main.config.getString("quests.weekly-reset-day", "MONDAY") ?: "MONDAY",
                configManager.main.config.getString("quests.weekly-reset-time", "00:00") ?: "00:00"
            )
            QuestResetType.CUSTOM_INTERVAL -> TimeUtil.nowSeconds() + quest.resetIntervalSeconds.coerceAtLeast(60)
        }
    }

    fun expired(resetAt: Long?): Boolean = resetAt != null && resetAt <= TimeUtil.nowSeconds()
}
