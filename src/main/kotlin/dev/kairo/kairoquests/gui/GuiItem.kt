package dev.kairo.kairoquests.gui

data class GuiItem(
    val material: String,
    val displayName: String,
    val lore: List<String>,
    val slot: Int,
    val slots: List<Int>,
    val amount: Int,
    val customModelData: Int?,
    val enchanted: Boolean,
    val hideFlags: Boolean,
    val skullOwner: String?,
    val leftClickActions: List<String>,
    val rightClickActions: List<String>,
    val shiftLeftClickActions: List<String>,
    val permission: String?
)
