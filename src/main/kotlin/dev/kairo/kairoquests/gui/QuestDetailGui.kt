package dev.kairo.kairoquests.gui

import dev.kairo.kairoquests.config.GuiConfig
import dev.kairo.kairoquests.model.Quest
import dev.kairo.kairoquests.model.QuestStatus
import dev.kairo.kairoquests.service.QuestProgressService
import dev.kairo.kairoquests.util.ItemUtil
import dev.kairo.kairoquests.util.TextUtil
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

class QuestDetailGui(
    private val guiConfig: GuiConfig,
    private val progressService: QuestProgressService,
    private val builder: GuiItemBuilder
) {
    fun build(player: Player, quest: Quest): Inventory {
        val holder = GuiHolder("detail", questId = quest.id)
        val inventory = Bukkit.createInventory(holder, guiConfig.size("detail"), TextUtil.mini(guiConfig.title("detail")))
        holder.attach(inventory)
        fillBorder(inventory)
        val progress = progressService.progress(player, quest)
        val placeholders = progressService.placeholders(player, quest, progress)

        inventory.setItem(
            13,
            ItemUtil.simple(
                quest.guiMaterial,
                quest.displayName,
                listOf(
                    "<gray>${quest.description}",
                    "<gray>Type: <yellow>%quest_type%",
                    "<gray>Progress: <green>%quest_progress%</green><dark_gray>/</dark_gray><yellow>%quest_required%</yellow>",
                    "<gray>Status: <yellow>%quest_status%",
                    "<gray>Resets in: <yellow>%quest_time_left%"
                ).map { line -> placeholders.entries.fold(line) { current, entry -> current.replace(entry.key, entry.value) } },
                customModelData = quest.customModelData
            )
        )
        inventory.setItem(29, ItemUtil.simple("COMPASS", "<aqua>Track Quest", listOf("<gray>Show this quest in placeholders.")))
        holder.actions[29] = GuiSlotActions(left = listOf("TRACK_QUEST:${quest.id}"))
        if (progress.status == QuestStatus.COMPLETED) {
            inventory.setItem(31, ItemUtil.simple("LIME_DYE", "<green>Claim Reward", listOf("<gray>Collect this quest reward.")))
            holder.actions[31] = GuiSlotActions(left = listOf("CLAIM_QUEST:${quest.id}"))
        } else {
            inventory.setItem(31, ItemUtil.simple("CLOCK", "<yellow>In Progress", listOf("<gray>${progress.progress}/${quest.objective.amount} complete.")))
        }
        inventory.setItem(33, ItemUtil.simple("BARRIER", "<red>Back", emptyList()))
        holder.actions[33] = GuiSlotActions(left = listOf("BACK"))
        return inventory
    }

    private fun fillBorder(inventory: Inventory) {
        val filler = builder.filler(guiConfig.borderMaterial(), guiConfig.fillerName())
        for (slot in 0 until inventory.size) {
            val row = slot / 9
            val column = slot % 9
            if (row == 0 || row == (inventory.size / 9) - 1 || column == 0 || column == 8) {
                inventory.setItem(slot, filler)
            }
        }
    }
}
