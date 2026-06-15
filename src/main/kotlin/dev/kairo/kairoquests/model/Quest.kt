package dev.kairo.kairoquests.model

data class Quest(
    val id: String,
    val displayName: String,
    val description: String,
    val category: QuestCategory,
    val objective: QuestObjective,
    val resetType: QuestResetType,
    val resetIntervalSeconds: Long,
    val permissionRequired: Boolean,
    val permission: String,
    val autoStart: Boolean,
    val autoClaim: Boolean,
    val rewards: List<QuestReward>,
    val guiMaterial: String,
    val customModelData: Int?,
    val lore: List<String>,
    val completionMessage: String,
    val broadcastCompletion: Boolean,
    val startActions: List<String>,
    val completeActions: List<String>,
    val claimActions: List<String>
) {
    fun canUse(hasPermission: (String) -> Boolean): Boolean {
        return !permissionRequired || permission.isBlank() || hasPermission(permission)
    }
}
