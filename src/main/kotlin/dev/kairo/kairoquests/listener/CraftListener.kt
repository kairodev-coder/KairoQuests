package dev.kairo.kairoquests.listener

import dev.kairo.kairoquests.model.QuestType
import dev.kairo.kairoquests.service.QuestProgressService
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.CraftingInventory

class CraftListener(private val progressService: QuestProgressService) : Listener {
    @EventHandler(ignoreCancelled = true)
    fun onCraft(event: CraftItemEvent) {
        val player = event.whoClicked as? Player ?: return
        val result = event.currentItem ?: return
        val amount = if (event.click == ClickType.SHIFT_LEFT || event.click == ClickType.SHIFT_RIGHT) {
            result.amount * maxCrafts(event.inventory)
        } else {
            result.amount
        }
        progressService.addMatching(player, QuestType.CRAFT_ITEM, result.type.name, amount.coerceAtLeast(1))
    }

    private fun maxCrafts(inventory: CraftingInventory): Int {
        return inventory.matrix
            .filterNotNull()
            .minOfOrNull { it.amount }
            ?.coerceAtLeast(1)
            ?: 1
    }
}
