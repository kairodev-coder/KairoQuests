package dev.kairo.kairoquests.listener

import dev.kairo.kairoquests.model.QuestType
import dev.kairo.kairoquests.service.QuestProgressService
import org.bukkit.entity.Item
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerFishEvent

class FishingListener(private val progressService: QuestProgressService) : Listener {
    @EventHandler(ignoreCancelled = true)
    fun onFish(event: PlayerFishEvent) {
        if (event.state != PlayerFishEvent.State.CAUGHT_FISH) return
        val material = (event.caught as? Item)?.itemStack?.type?.name ?: "*"
        progressService.addMatching(event.player, QuestType.FISH_ITEM, material)
    }
}
