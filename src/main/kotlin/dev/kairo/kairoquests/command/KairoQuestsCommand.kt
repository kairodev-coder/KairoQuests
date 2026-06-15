package dev.kairo.kairoquests.command

import dev.kairo.kairoquests.config.ConfigManager
import dev.kairo.kairoquests.hook.VaultHook
import dev.kairo.kairoquests.model.QuestCategory
import dev.kairo.kairoquests.service.QuestProgressService
import dev.kairo.kairoquests.service.QuestService
import dev.kairo.kairoquests.util.MessageService
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class KairoQuestsCommand(
    private val configManager: ConfigManager,
    private val questService: QuestService,
    private val progressService: QuestProgressService,
    private val messages: MessageService,
    private val vaultHook: VaultHook
) : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("kairoquests.admin")) {
            messages.send(sender, "no-permission")
            return true
        }
        when (args.getOrNull(0)?.lowercase()) {
            "reload" -> reload(sender)
            "reset" -> reset(sender, args)
            "giveprogress" -> progress(sender, args, true)
            "setprogress" -> progress(sender, args, false)
            "complete" -> complete(sender, args)
            "stats" -> stats(sender, args)
            "debug" -> debug(sender)
            else -> messages.send(sender, "invalid-usage", mapOf("%usage%" to "/kairoquests <reload|reset|giveprogress|setprogress|complete|stats|debug>"))
        }
        return true
    }

    private fun reload(sender: CommandSender) {
        if (!sender.hasPermission("kairoquests.admin.reload")) return messages.send(sender, "no-permission")
        runCatching {
            configManager.reload()
            questService.reload()
        }.onSuccess {
            messages.send(sender, "reload-success")
        }.onFailure {
            messages.send(sender, "reload-failed")
            Bukkit.getLogger().warning("KairoQuests reload failed: ${it.message}")
        }
    }

    private fun reset(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("kairoquests.admin.reset")) return messages.send(sender, "no-permission")
        val targetName = args.getOrNull(1) ?: return messages.send(sender, "invalid-usage", mapOf("%usage%" to "/kairoquests reset <player> [quest]"))
        val target = Bukkit.getPlayer(targetName) ?: return messages.send(sender, "player-not-found")
        val questId = args.getOrNull(2)
        if (questId != null && questService.get(questId) == null) return messages.send(sender, "quest-not-found")
        progressService.reset(target.uniqueId, questId)
        messages.send(sender, "admin-reset", mapOf("%player%" to target.name))
    }

    private fun progress(sender: CommandSender, args: Array<out String>, add: Boolean) {
        if (!sender.hasPermission("kairoquests.admin.progress")) return messages.send(sender, "no-permission")
        val targetName = args.getOrNull(1) ?: return messages.send(sender, "invalid-usage", mapOf("%usage%" to "/kairoquests ${args[0]} <player> <quest> <amount>"))
        val target = Bukkit.getPlayer(targetName) ?: return messages.send(sender, "player-not-found")
        val questId = args.getOrNull(2) ?: return messages.send(sender, "invalid-usage", mapOf("%usage%" to "/kairoquests ${args[0]} <player> <quest> <amount>"))
        val amount = args.getOrNull(3)?.toIntOrNull() ?: return messages.send(sender, "invalid-usage", mapOf("%usage%" to "/kairoquests ${args[0]} <player> <quest> <amount>"))
        if ((add && amount <= 0) || (!add && amount < 0)) return messages.send(sender, "invalid-number")
        val ok = if (add) progressService.addProgress(target.uniqueId, questId, amount) else progressService.setProgress(target.uniqueId, questId, amount)
        if (ok) messages.send(sender, "admin-progress", mapOf("%player%" to target.name)) else messages.send(sender, "quest-not-found")
    }

    private fun complete(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("kairoquests.admin.complete")) return messages.send(sender, "no-permission")
        val targetName = args.getOrNull(1) ?: return messages.send(sender, "invalid-usage", mapOf("%usage%" to "/kairoquests complete <player> <quest>"))
        val target = Bukkit.getPlayer(targetName) ?: return messages.send(sender, "player-not-found")
        val questId = args.getOrNull(2) ?: return messages.send(sender, "invalid-usage", mapOf("%usage%" to "/kairoquests complete <player> <quest>"))
        if (progressService.completeQuest(target.uniqueId, questId)) {
            messages.send(sender, "admin-complete", mapOf("%player%" to target.name))
        } else {
            messages.send(sender, "quest-not-found")
        }
    }

    private fun stats(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("kairoquests.admin.stats")) return messages.send(sender, "no-permission")
        val targetName = args.getOrNull(1) ?: return messages.send(sender, "invalid-usage", mapOf("%usage%" to "/kairoquests stats <player>"))
        val target = Bukkit.getPlayer(targetName) ?: return messages.send(sender, "player-not-found")
        val stats = progressService.stats(target.uniqueId)
        messages.send(sender, "stats", mapOf(
            "%daily_completed%" to (stats?.completedDaily ?: 0).toString(),
            "%weekly_completed%" to (stats?.completedWeekly ?: 0).toString(),
            "%total_completed%" to (stats?.completedTotal ?: 0).toString(),
            "%daily_streak%" to (stats?.currentDailyStreak ?: 0).toString()
        ))
    }

    private fun debug(sender: CommandSender) {
        val values = listOf(
            "Storage" to "SQLite",
            "Loaded quests" to questService.all().size.toString(),
            "Daily quests" to questService.byCategory(QuestCategory.DAILY).size.toString(),
            "Weekly quests" to questService.byCategory(QuestCategory.WEEKLY).size.toString(),
            "Vault" to vaultHook.enabled.toString(),
            "PlaceholderAPI" to Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI").toString()
        )
        messages.send(sender, "debug-header")
        values.forEach { (key, value) ->
            messages.send(sender, "debug-line", mapOf("%key%" to key, "%value%" to value))
        }
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        return when (args.size) {
            1 -> listOf("reload", "reset", "giveprogress", "setprogress", "complete", "stats", "debug").filter { it.startsWith(args[0], true) }
            2 -> when (args[0].lowercase()) {
                "reset", "giveprogress", "setprogress", "complete", "stats" -> Bukkit.getOnlinePlayers().map { it.name }.filter { it.startsWith(args[1], true) }
                else -> emptyList()
            }
            3 -> when (args[0].lowercase()) {
                "reset", "giveprogress", "setprogress", "complete" -> questService.all().map { it.id }.filter { it.startsWith(args[2], true) }
                else -> emptyList()
            }
            else -> emptyList()
        }
    }
}
