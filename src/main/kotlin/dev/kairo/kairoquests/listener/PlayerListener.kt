package dev.kairo.kairoquests.listener

import dev.kairo.kairoquests.model.QuestType
import dev.kairo.kairoquests.service.QuestProgressService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.enchantment.EnchantItemEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class PlayerListener(private val progressService: QuestProgressService) : Listener {
    private val lastMove = ConcurrentHashMap<UUID, String>()

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        progressService.load(event.player)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        progressService.unload(event.player)
        lastMove.remove(event.player.uniqueId)
    }

    @EventHandler(ignoreCancelled = true)
    fun onMove(event: PlayerMoveEvent) {
        val from = event.from
        val to = event.to
        if (from.blockX == to.blockX && from.blockY == to.blockY && from.blockZ == to.blockZ) return
        val key = "${to.world.uid}:${to.blockX}:${to.blockY}:${to.blockZ}"
        if (lastMove.put(event.player.uniqueId, key) != key) {
            progressService.addMatching(event.player, QuestType.WALK_DISTANCE, "BLOCKS")
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onConsume(event: PlayerItemConsumeEvent) {
        progressService.addMatching(event.player, QuestType.CONSUME_ITEM, event.item.type.name)
    }

    @EventHandler(ignoreCancelled = true)
    fun onEnchant(event: EnchantItemEvent) {
        progressService.addMatching(event.enchanter, QuestType.ENCHANT_ITEM, event.item.type.name)
    }

    @EventHandler(ignoreCancelled = true)
    fun onCommand(event: PlayerCommandPreprocessEvent) {
        val command = event.message.removePrefix("/").substringBefore(" ").uppercase()
        progressService.addMatching(event.player, QuestType.COMMAND, command)
    }
}
