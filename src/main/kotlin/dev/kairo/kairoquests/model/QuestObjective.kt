package dev.kairo.kairoquests.model

data class QuestObjective(
    val type: QuestType,
    val target: String,
    val amount: Int
)
