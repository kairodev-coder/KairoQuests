package dev.kairo.kairoquests.model

data class QuestReward(
    val type: RewardType,
    val value: String
)

enum class RewardType {
    COMMAND,
    PLAYER_COMMAND,
    MESSAGE,
    SOUND,
    TITLE,
    ACTIONBAR,
    MONEY,
    EXPERIENCE,
    ITEM
}
