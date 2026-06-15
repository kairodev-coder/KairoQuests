package dev.kairo.kairoquests.command

import dev.kairo.kairoquests.gui.GuiManager
import dev.kairo.kairoquests.model.QuestStatus
import dev.kairo.kairoquests.service.QuestProgressService
import dev.kairo.kairoquests.service.QuestService
import dev.kairo.kairoquests.util.MessageService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class QuestCommand(
    private val guiManager: GuiManager,
    private val questService: QuestService,
    private val progressService: QuestProgressService,
    private val messages: MessageService
) : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val player = sender as? Player ?: run {
            messages.send(sender, "player-only")
            return true
        }
        when (label.lowercase()) {
            "daily" -> {
                if (!check(player, "kairoquests.command.daily")) return true
                guiManager.openDaily(player)
            }
            "weekly" -> {
                if (!check(player, "kairoquests.command.weekly")) return true
                guiManager.openWeekly(player)
            }
            else -> handleQuest(player, args)
        }
        return true
    }

    private fun handleQuest(player: Player, args: Array<out String>) {
        if (args.isEmpty()) {
            if (!check(player, "kairoquests.command.quests")) return
            guiManager.openMain(player)
            return
        }
        when (args[0].lowercase()) {
            "progress" -> {
                if (!check(player, "kairoquests.command.progress")) return
                val active = progressService.allProgress(player.uniqueId).filter { it.status == QuestStatus.ACTIVE || it.status == QuestStatus.COMPLETED }
                if (active.isEmpty()) {
                    messages.send(player, "progress-updated", mapOf("%quest_display%" to "Quests", "%quest_progress%" to "0", "%quest_required%" to "0"))
                    return
                }
                active.take(5).forEach { progress ->
                    val quest = questService.get(progress.questId) ?: return@forEach
                    messages.send(player, "progress-updated", progressService.placeholders(player, quest, progress))
                }
            }
            "claim" -> {
                if (!check(player, "kairoquests.command.claim")) return
                val questId = args.getOrNull(1) ?: return messages.send(player, "invalid-usage", mapOf("%usage%" to "/quest claim <quest>"))
                progressService.claim(player, questId)
            }
            "track" -> {
                if (!check(player, "kairoquests.command.track")) return
                val questId = args.getOrNull(1) ?: return messages.send(player, "invalid-usage", mapOf("%usage%" to "/quest track <quest>"))
                if (!progressService.track(player, questId)) messages.send(player, "quest-not-found")
            }
            "untrack" -> {
                if (!check(player, "kairoquests.command.track")) return
                progressService.untrack(player)
            }
            else -> messages.send(player, "invalid-usage", mapOf("%usage%" to "/quest [progress|claim|track|untrack]"))
        }
    }

    private fun check(player: Player, permission: String): Boolean {
        if (player.hasPermission(permission)) return true
        messages.send(player, "no-permission")
        return false
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        if (alias.equals("daily", true) || alias.equals("weekly", true)) return emptyList()
        return when (args.size) {
            1 -> listOf("progress", "claim", "track", "untrack").filter { it.startsWith(args[0], true) }
            2 -> if (args[0].equals("claim", true) || args[0].equals("track", true)) {
                questService.all().map { it.id }.filter { it.startsWith(args[1], true) }
            } else emptyList()
            else -> emptyList()
        }
    }
}
