package dev.kairo.kairoquests.service

import dev.kairo.kairoquests.config.QuestConfig
import dev.kairo.kairoquests.model.Quest
import dev.kairo.kairoquests.model.QuestCategory
import dev.kairo.kairoquests.model.QuestType

class QuestService(private val questConfig: QuestConfig) {
    private var quests: List<Quest> = emptyList()
    private var questsById: Map<String, Quest> = emptyMap()
    private var questsByType: Map<QuestType, List<Quest>> = emptyMap()

    fun reload() {
        quests = questConfig.quests().sortedBy { it.id }
        questsById = quests.associateBy { it.id }
        questsByType = quests.groupBy { it.objective.type }
    }

    fun all(): List<Quest> = quests

    fun get(id: String): Quest? = questsById[id]

    fun byCategory(category: QuestCategory): List<Quest> = quests.filter { it.category == category }

    fun matching(type: QuestType, target: String): List<Quest> {
        val normalized = target.uppercase()
        return questsByType[type].orEmpty().filter {
            it.objective.target == "*" || it.objective.target.uppercase() == normalized
        }
    }
}
