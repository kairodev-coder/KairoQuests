package dev.kairo.kairoquests.service

import dev.kairo.kairoquests.hook.VaultHook
import dev.kairo.kairoquests.model.Quest
import dev.kairo.kairoquests.model.RewardType
import dev.kairo.kairoquests.util.ItemUtil
import dev.kairo.kairoquests.util.MessageService
import dev.kairo.kairoquests.util.SoundUtil
import dev.kairo.kairoquests.util.TextUtil
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player

class RewardService(
    private val messages: MessageService,
    private val vaultHook: VaultHook
) {
    fun give(player: Player, quest: Quest, placeholders: Map<String, String>) {
        quest.rewards.forEach { reward ->
            val value = messages.apply(reward.value, placeholders)
            when (reward.type) {
                RewardType.COMMAND -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), value)
                RewardType.PLAYER_COMMAND -> player.performCommand(value)
                RewardType.MESSAGE -> player.sendMessage(TextUtil.mini(value))
                RewardType.SOUND -> SoundUtil.play(player, value)
                RewardType.TITLE -> {
                    val parts = value.split("|", limit = 2)
                    player.showTitle(net.kyori.adventure.title.Title.title(TextUtil.mini(parts[0]), TextUtil.mini(parts.getOrElse(1) { "" })))
                }
                RewardType.ACTIONBAR -> player.sendActionBar(TextUtil.mini(value))
                RewardType.MONEY -> vaultHook.deposit(player, value.toDoubleOrNull() ?: 0.0)
                RewardType.EXPERIENCE -> player.giveExp(value.toIntOrNull() ?: 0)
                RewardType.ITEM -> giveItem(player, value)
            }
        }
    }

    private fun giveItem(player: Player, value: String) {
        val parts = value.split(":")
        val material = Material.matchMaterial(parts.getOrElse(0) { "STONE" }) ?: return
        val amount = parts.getOrNull(1)?.toIntOrNull() ?: 1
        val leftovers = player.inventory.addItem(ItemUtil.simple(material.name, "<white>${material.name.lowercase().replace('_', ' ')}", emptyList(), amount))
        leftovers.values.forEach { player.world.dropItemNaturally(player.location, it) }
    }
}
