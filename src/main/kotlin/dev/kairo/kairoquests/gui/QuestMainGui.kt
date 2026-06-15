package dev.kairo.kairoquests.gui

import dev.kairo.kairoquests.config.GuiConfig
import dev.kairo.kairoquests.model.PlayerQuestStats
import dev.kairo.kairoquests.service.QuestProgressService
import dev.kairo.kairoquests.util.MessageService
import dev.kairo.kairoquests.util.TextUtil
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

class QuestMainGui(
    private val guiConfig: GuiConfig,
    private val progressService: QuestProgressService,
    private val messages: MessageService,
    private val builder: GuiItemBuilder
) {
    fun build(player: Player): Inventory {
        val holder = GuiHolder("main")
        val inventory = Bukkit.createInventory(holder, guiConfig.size("main"), TextUtil.mini(guiConfig.title("main")))
        holder.attach(inventory)
        fillBorder(inventory)
        val stats = progressService.stats(player.uniqueId) ?: PlayerQuestStats(player.uniqueId, player.name, 0, 0, 0, 0, 0, null)
        val trackedId = progressService.trackedQuest(player.uniqueId)
        val placeholders = mapOf(
            "%player%" to player.name,
            "%daily_completed%" to stats.completedDaily.toString(),
            "%weekly_completed%" to stats.completedWeekly.toString(),
            "%total_completed%" to stats.completedTotal.toString(),
            "%daily_streak%" to stats.currentDailyStreak.toString(),
            "%best_daily_streak%" to stats.bestDailyStreak.toString(),
            "%tracked_quest%" to (trackedId ?: "None")
        )
        guiConfig.items("main").forEach { item ->
            if (item.permission != null && !player.hasPermission(item.permission)) return@forEach
            val stack = builder.fromConfig(item, placeholders)
            val slots = item.slots.ifEmpty { listOf(item.slot) }
            slots.filter { it in 0 until inventory.size }.forEach { slot ->
                inventory.setItem(slot, stack)
                holder.actions[slot] = GuiSlotActions(item.leftClickActions, item.rightClickActions, item.shiftLeftClickActions)
            }
        }
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
