package dev.kairo.kairoquests.gui

import dev.kairo.kairoquests.config.GuiConfig
import dev.kairo.kairoquests.model.QuestCategory
import dev.kairo.kairoquests.service.QuestProgressService
import dev.kairo.kairoquests.service.QuestService
import dev.kairo.kairoquests.util.MessageService
import dev.kairo.kairoquests.util.SoundUtil
import dev.kairo.kairoquests.util.TextUtil
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.Inventory
import org.bukkit.plugin.java.JavaPlugin

class GuiManager(
    private val plugin: JavaPlugin,
    private val guiConfig: GuiConfig,
    private val questService: QuestService,
    private val progressService: QuestProgressService,
    private val messages: MessageService
) {
    private val builder = GuiItemBuilder(messages)
    private val paginated = PaginatedGui(guiConfig, progressService, messages, builder)
    private val mainGui = QuestMainGui(guiConfig, progressService, messages, builder)
    private val detailGui = QuestDetailGui(guiConfig, progressService, builder)

    fun openMain(player: Player) = player.openInventory(mainGui.build(player))

    fun openDaily(player: Player, page: Int = 0) = openQuestList(player, "daily", QuestCategory.DAILY, page)

    fun openWeekly(player: Player, page: Int = 0) = openQuestList(player, "weekly", QuestCategory.WEEKLY, page)

    fun openDetail(player: Player, questId: String) {
        val quest = questService.get(questId) ?: run {
            messages.send(player, "quest-not-found")
            return
        }
        player.openInventory(detailGui.build(player, quest))
    }

    fun handleClick(player: Player, holder: GuiHolder, slot: Int, click: ClickType) {
        val actions = holder.actions[slot] ?: return
        val selected = when {
            click.isShiftClick && click.isLeftClick -> actions.shiftLeft
            click.isRightClick -> actions.right
            else -> actions.left
        }
        selected.forEach { action -> runAction(player, holder, action) }
    }

    private fun runAction(player: Player, holder: GuiHolder, action: String) {
        val resolved = messages.apply(action, actionPlaceholders(player, holder))
        val parts = resolved.split(":", limit = 2)
        when (parts[0].uppercase()) {
            "OPEN_GUI" -> openNamed(player, parts.getOrElse(1) { "main" }, holder)
            "CLAIM_QUEST" -> {
                progressService.claim(player, parts.getOrElse(1) { holder.questId ?: "" })
                refresh(player, holder)
            }
            "TRACK_QUEST" -> progressService.track(player, parts.getOrElse(1) { holder.questId ?: "" })
            "UNTRACK_QUEST" -> progressService.untrack(player)
            "NEXT_PAGE" -> page(player, holder, holder.page + 1)
            "PREVIOUS_PAGE" -> page(player, holder, holder.page - 1)
            "BACK" -> openMain(player)
            "CLOSE" -> player.closeInventory()
            "REFRESH" -> refresh(player, holder)
            "RUN_COMMAND" -> player.performCommand(parts.getOrElse(1) { "" }.removePrefix("/"))
            "SEND_MESSAGE" -> player.sendMessage(TextUtil.mini(parts.getOrElse(1) { "" }))
            "PLAY_SOUND" -> SoundUtil.play(player, parts.getOrElse(1) { "" })
        }
    }

    private fun openNamed(player: Player, name: String, holder: GuiHolder) {
        when {
            name.equals("main", true) -> openMain(player)
            name.equals("daily", true) -> openDaily(player)
            name.equals("weekly", true) -> openWeekly(player)
            name.startsWith("quest_detail:", true) -> {
                val raw = name.substringAfter(":")
                val questId = questService.get(raw)?.id ?: progressService.trackedQuest(player.uniqueId) ?: holder.questId ?: raw
                openDetail(player, questId)
            }
        }
    }

    private fun page(player: Player, holder: GuiHolder, page: Int) {
        when (holder.type) {
            "daily" -> openDaily(player, page)
            "weekly" -> openWeekly(player, page)
            else -> openMain(player)
        }
    }

    private fun refresh(player: Player, holder: GuiHolder) {
        Bukkit.getScheduler().runTaskLater(
            plugin,
            Runnable {
                val inventory: Inventory = when (holder.type) {
                    "daily" -> questList(player, "daily", QuestCategory.DAILY, holder.page)
                    "weekly" -> questList(player, "weekly", QuestCategory.WEEKLY, holder.page)
                    "detail" -> holder.questId?.let { questService.get(it) }?.let { detailGui.build(player, it) } ?: mainGui.build(player)
                    else -> mainGui.build(player)
                }
                player.openInventory(inventory)
            },
            1L
        )
    }

    private fun openQuestList(player: Player, type: String, category: QuestCategory, page: Int) {
        player.openInventory(questList(player, type, category, page))
    }

    private fun questList(player: Player, type: String, category: QuestCategory, page: Int): Inventory =
        paginated.build(player, type, questService.byCategory(category), page.coerceAtLeast(0))

    private fun actionPlaceholders(player: Player, holder: GuiHolder): Map<String, String> {
        val questId = holder.questId ?: progressService.trackedQuest(player.uniqueId)
        val quest = questId?.let(questService::get)
        return if (quest != null) {
            progressService.placeholders(player, quest).plus("%tracked_quest%" to (progressService.trackedQuest(player.uniqueId) ?: ""))
        } else {
            mapOf("%player%" to player.name, "%tracked_quest%" to (progressService.trackedQuest(player.uniqueId) ?: ""))
        }
    }
}
