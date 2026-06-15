package dev.kairo.kairoquests.listener

import dev.kairo.kairoquests.model.QuestType
import dev.kairo.kairoquests.service.QuestProgressService
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent

class EntityListener(private val progressService: QuestProgressService) : Listener {
    @EventHandler(ignoreCancelled = true)
    fun onDeath(event: EntityDeathEvent) {
        val killer = event.entity.killer ?: return
        if (event.entity is Player) {
            progressService.addMatching(killer, QuestType.KILL_PLAYER, "PLAYER")
        } else {
            progressService.addMatching(killer, QuestType.KILL_MOB, event.entityType.name)
        }
    }
}
