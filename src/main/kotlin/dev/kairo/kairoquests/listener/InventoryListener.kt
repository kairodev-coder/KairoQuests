package dev.kairo.kairoquests.listener

import dev.kairo.kairoquests.gui.GuiHolder
import dev.kairo.kairoquests.gui.GuiManager
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class InventoryListener(private val guiManager: GuiManager) : Listener {
    @EventHandler(ignoreCancelled = true)
    fun onClick(event: InventoryClickEvent) {
        val holder = event.inventory.holder as? GuiHolder ?: return
        event.isCancelled = true
        val player = event.whoClicked as? Player ?: return
        guiManager.handleClick(player, holder, event.rawSlot, event.click)
    }
}
