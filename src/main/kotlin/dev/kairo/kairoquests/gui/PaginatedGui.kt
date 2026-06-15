package dev.kairo.kairoquests.gui

import dev.kairo.kairoquests.config.GuiConfig
import dev.kairo.kairoquests.model.Quest
import dev.kairo.kairoquests.service.QuestProgressService
import dev.kairo.kairoquests.util.ItemUtil
import dev.kairo.kairoquests.util.MessageService
import dev.kairo.kairoquests.util.TextUtil
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

class PaginatedGui(
    private val guiConfig: GuiConfig,
    private val progressService: QuestProgressService,
    private val messages: MessageService,
    private val builder: GuiItemBuilder
) {
    fun build(player: Player, type: String, quests: List<Quest>, page: Int): Inventory {
        val holder = GuiHolder(type, page)
        val inventory = Bukkit.createInventory(holder, guiConfig.size(type), TextUtil.mini(guiConfig.title(type)))
        holder.attach(inventory)
        border(inventory)

        val slots = guiConfig.questSlots().filter { it in 0 until inventory.size }
        val from = page * slots.size
        quests.drop(from).take(slots.size).forEachIndexed { index, quest ->
            val progress = progressService.progress(player, quest)
            val placeholders = progressService.placeholders(player, quest, progress)
            val lore = quest.lore.ifEmpty {
                listOf("<gray>${quest.description}", "<gray>Progress: <green>%quest_progress%</green><dark_gray>/</dark_gray><yellow>%quest_required%</yellow>", "<gray>Status: <yellow>%quest_status%")
            }.map { messages.apply(it, placeholders) }
            val item = ItemUtil.simple(quest.guiMaterial, quest.displayName, lore, customModelData = quest.customModelData)
            val slot = slots[index]
            inventory.setItem(slot, item)
            holder.actions[slot] = GuiSlotActions(
                left = listOf("OPEN_GUI:quest_detail:${quest.id}"),
                right = listOf("TRACK_QUEST:${quest.id}"),
                shiftLeft = listOf("CLAIM_QUEST:${quest.id}")
            )
        }

        if (page > 0) {
            inventory.setItem(45, ItemUtil.simple("ARROW", "<yellow>Previous Page", emptyList()))
            holder.actions[45] = GuiSlotActions(left = listOf("PREVIOUS_PAGE"))
        }
        if (from + slots.size < quests.size) {
            inventory.setItem(53, ItemUtil.simple("ARROW", "<yellow>Next Page", emptyList()))
            holder.actions[53] = GuiSlotActions(left = listOf("NEXT_PAGE"))
        }
        inventory.setItem(49, ItemUtil.simple("BARRIER", "<red>Back", emptyList()))
        holder.actions[49] = GuiSlotActions(left = listOf("BACK"))
        return inventory
    }

    private fun border(inventory: Inventory) {
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
