package dev.kairo.kairoquests.hook

import dev.kairo.kairoquests.KairoQuestsPlugin
import dev.kairo.kairoquests.util.NumberUtil
import dev.kairo.kairoquests.util.TextUtil
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer

class PlaceholderApiHook(private val plugin: KairoQuestsPlugin) : PlaceholderExpansion() {
    override fun getIdentifier(): String = "kairoquests"

    override fun getAuthor(): String = "Kairo"

    override fun getVersion(): String = plugin.pluginMeta.version

    override fun persist(): Boolean = true

    override fun onRequest(player: OfflinePlayer?, params: String): String {
        val id = player?.uniqueId ?: return ""
        val stats = plugin.progressService.stats(id)
        return when (params.lowercase()) {
            "daily_completed" -> (stats?.completedDaily ?: 0).toString()
            "weekly_completed" -> (stats?.completedWeekly ?: 0).toString()
            "total_completed" -> (stats?.completedTotal ?: 0).toString()
            "daily_streak" -> (stats?.currentDailyStreak ?: 0).toString()
            "best_daily_streak" -> (stats?.bestDailyStreak ?: 0).toString()
            "tracked" -> trackedName(id)
            "tracked_progress" -> trackedProgress(id)
            "tracked_percent" -> trackedPercent(id)
            else -> ""
        }
    }

    private fun trackedName(playerId: java.util.UUID): String {
        val questId = plugin.progressService.trackedQuest(playerId) ?: return "None"
        return plugin.questService.get(questId)?.displayName?.let(TextUtil::stripMini) ?: questId
    }

    private fun trackedProgress(playerId: java.util.UUID): String {
        val questId = plugin.progressService.trackedQuest(playerId) ?: return "0/0"
        val quest = plugin.questService.get(questId) ?: return "0/0"
        return "${plugin.progressService.getProgressValue(playerId, questId)}/${quest.objective.amount}"
    }

    private fun trackedPercent(playerId: java.util.UUID): String {
        val questId = plugin.progressService.trackedQuest(playerId) ?: return "0%"
        val quest = plugin.questService.get(questId) ?: return "0%"
        return NumberUtil.percent(plugin.progressService.getProgressValue(playerId, questId), quest.objective.amount)
    }
}
