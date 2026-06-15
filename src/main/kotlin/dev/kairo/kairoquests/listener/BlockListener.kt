package dev.kairo.kairoquests.listener

import dev.kairo.kairoquests.model.QuestType
import dev.kairo.kairoquests.service.QuestProgressService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent

class BlockListener(private val progressService: QuestProgressService) : Listener {
    @EventHandler(ignoreCancelled = true)
    fun onBreak(event: BlockBreakEvent) {
        progressService.addMatching(event.player, QuestType.BREAK_BLOCK, event.block.type.name)
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlace(event: BlockPlaceEvent) {
        progressService.addMatching(event.player, QuestType.PLACE_BLOCK, event.block.type.name)
    }
}
