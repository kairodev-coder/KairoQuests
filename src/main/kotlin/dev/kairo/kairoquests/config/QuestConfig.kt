package dev.kairo.kairoquests.config

import dev.kairo.kairoquests.model.Quest
import dev.kairo.kairoquests.model.QuestCategory
import dev.kairo.kairoquests.model.QuestObjective
import dev.kairo.kairoquests.model.QuestResetType
import dev.kairo.kairoquests.model.QuestReward
import dev.kairo.kairoquests.model.QuestType
import dev.kairo.kairoquests.model.RewardType
import org.bukkit.plugin.java.JavaPlugin

class QuestConfig(plugin: JavaPlugin) : FileConfig(plugin, "quests.yml") {
    private var loadedQuests: List<Quest> = emptyList()

    override fun reload() {
        super.reload()
        loadedQuests = loadQuests()
    }

    fun quests(): List<Quest> = loadedQuests

    private fun loadQuests(): List<Quest> {
        val section = config.getConfigurationSection("quests") ?: return emptyList()
        return section.getKeys(false).mapNotNull { id ->
            val path = "quests.$id"
            if (!config.getBoolean("$path.enabled", true)) return@mapNotNull null
            val type = enumValue<QuestType>(config.getString("$path.type"), QuestType.CUSTOM)
            val category = enumValue<QuestCategory>(config.getString("$path.category"), QuestCategory.DAILY)
            val resetType = enumValue<QuestResetType>(config.getString("$path.reset-type"), QuestResetType.NONE)
            Quest(
                id = id,
                displayName = config.getString("$path.display-name", id) ?: id,
                description = config.getString("$path.description", "") ?: "",
                category = category,
                objective = QuestObjective(
                    type = type,
                    target = config.getString("$path.target", "*") ?: "*",
                    amount = config.getInt("$path.amount", 1).coerceAtLeast(1)
                ),
                resetType = resetType,
                resetIntervalSeconds = config.getLong("$path.reset-interval", 0L),
                permissionRequired = config.getBoolean("$path.permission-required", false),
                permission = config.getString("$path.permission", "") ?: "",
                autoStart = config.getBoolean("$path.auto-start", true),
                autoClaim = config.getBoolean("$path.auto-claim", false),
                rewards = config.getMapList("$path.rewards").mapNotNull { reward ->
                    val rewardType = enumValue<RewardType>(reward["type"]?.toString(), RewardType.MESSAGE)
                    val value = reward["value"]?.toString() ?: return@mapNotNull null
                    QuestReward(rewardType, value)
                },
                guiMaterial = config.getString("$path.gui-material", "BOOK") ?: "BOOK",
                customModelData = config.getInt("$path.custom-model-data", 0).takeIf { it > 0 },
                lore = config.getStringList("$path.lore"),
                completionMessage = config.getString("$path.completion-message", "") ?: "",
                broadcastCompletion = config.getBoolean("$path.broadcast-completion", false),
                startActions = config.getStringList("$path.start-actions"),
                completeActions = config.getStringList("$path.complete-actions"),
                claimActions = config.getStringList("$path.claim-actions")
            )
        }
    }

    private inline fun <reified T : Enum<T>> enumValue(value: String?, fallback: T): T {
        return runCatching { enumValueOf<T>((value ?: fallback.name).uppercase()) }.getOrDefault(fallback)
    }
}
