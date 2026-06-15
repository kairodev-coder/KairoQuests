package dev.kairo.kairoquests.service

import dev.kairo.kairoquests.database.PlayerQuestRepository
import dev.kairo.kairoquests.database.QuestRepository
import dev.kairo.kairoquests.model.PlayerQuestStats
import dev.kairo.kairoquests.model.Quest
import dev.kairo.kairoquests.model.QuestCategory
import dev.kairo.kairoquests.model.QuestProgress
import dev.kairo.kairoquests.model.QuestStatus
import dev.kairo.kairoquests.model.QuestType
import dev.kairo.kairoquests.util.MessageService
import dev.kairo.kairoquests.util.NumberUtil
import dev.kairo.kairoquests.util.SchedulerUtil
import dev.kairo.kairoquests.util.SoundUtil
import dev.kairo.kairoquests.util.TextUtil
import dev.kairo.kairoquests.util.TimeUtil
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class QuestProgressService(
    private val questService: QuestService,
    private val playerRepository: PlayerQuestRepository,
    private val questRepository: QuestRepository,
    private val resetService: ResetService,
    private val streakService: StreakService,
    private val rewardService: RewardService,
    private val messages: MessageService,
    private val scheduler: SchedulerUtil
) {
    private val progressCache = ConcurrentHashMap<UUID, MutableMap<String, QuestProgress>>()
    private val statsCache = ConcurrentHashMap<UUID, PlayerQuestStats>()
    private val trackedCache = ConcurrentHashMap<UUID, String>()
    private val dirty = ConcurrentHashMap<String, QuestProgress>()

    fun load(player: Player) {
        scheduler.async {
            val progress = playerRepository.loadProgress(player.uniqueId).associateBy { it.questId }.toMutableMap()
            val stats = playerRepository.loadStats(player.uniqueId, player.name)
            val tracked = questRepository.trackedQuest(player.uniqueId)
            scheduler.sync {
                progressCache[player.uniqueId] = progress
                statsCache[player.uniqueId] = stats
                if (tracked != null) trackedCache[player.uniqueId] = tracked
                ensureAutoStarted(player)
                addMatching(player, QuestType.LOGIN, "LOGIN")
            }
        }
    }

    fun unload(player: Player) {
        save(player.uniqueId)
        progressCache.remove(player.uniqueId)
        statsCache.remove(player.uniqueId)
        trackedCache.remove(player.uniqueId)
    }

    fun ensureAutoStarted(player: Player) {
        questService.all()
            .filter { it.autoStart && it.canUse(player::hasPermission) }
            .forEach { progress(player, it) }
    }

    fun progress(player: Player, quest: Quest): QuestProgress {
        val playerProgress = progressCache.computeIfAbsent(player.uniqueId) { ConcurrentHashMap() }
        val existing = playerProgress[quest.id]
        if (existing != null && !resetService.expired(existing.resetAt)) return existing
        val fresh = QuestProgress(
            playerId = player.uniqueId,
            playerName = player.name,
            questId = quest.id,
            progress = 0,
            status = QuestStatus.ACTIVE,
            startedAt = TimeUtil.nowSeconds(),
            completedAt = null,
            claimedAt = null,
            resetAt = resetService.nextReset(quest)
        )
        playerProgress[quest.id] = fresh
        markDirty(fresh)
        quest.startActions.forEach { runSimpleAction(player, it) }
        return fresh
    }

    fun addProgress(playerId: UUID, questId: String, amount: Int): Boolean {
        if (amount <= 0) return false
        val player = Bukkit.getPlayer(playerId) ?: return false
        val quest = questService.get(questId) ?: return false
        addProgress(player, quest, amount)
        return true
    }

    fun addProgress(player: Player, quest: Quest, amount: Int) {
        if (amount <= 0) return
        if (!quest.canUse(player::hasPermission)) return
        val progress = progress(player, quest)
        if (progress.status == QuestStatus.CLAIMED || progress.status == QuestStatus.COMPLETED) return
        progress.progress = (progress.progress + amount).coerceAtMost(quest.objective.amount)
        progress.playerName = player.name
        markDirty(progress)
        if (progress.progress >= quest.objective.amount) {
            complete(player, quest, progress)
        }
    }

    fun addMatching(player: Player, type: QuestType, target: String, amount: Int = 1) {
        if (amount <= 0) return
        questService.matching(type, target).forEach { addProgress(player, it, amount) }
    }

    fun setProgress(playerId: UUID, questId: String, amount: Int): Boolean {
        val player = Bukkit.getPlayer(playerId) ?: return false
        val quest = questService.get(questId) ?: return false
        val progress = progress(player, quest)
        val wasFinished = progress.status == QuestStatus.COMPLETED || progress.status == QuestStatus.CLAIMED
        progress.progress = amount.coerceIn(0, quest.objective.amount)
        if (progress.progress >= quest.objective.amount) {
            if (!wasFinished) {
                complete(player, quest, progress)
                return true
            }
            progress.status = QuestStatus.COMPLETED
            progress.completedAt = progress.completedAt ?: TimeUtil.nowSeconds()
        } else {
            progress.status = QuestStatus.ACTIVE
            progress.completedAt = null
            progress.claimedAt = null
        }
        markDirty(progress)
        return true
    }

    fun completeQuest(playerId: UUID, questId: String): Boolean {
        val player = Bukkit.getPlayer(playerId) ?: return false
        val quest = questService.get(questId) ?: return false
        complete(player, quest, progress(player, quest))
        return true
    }

    private fun complete(player: Player, quest: Quest, progress: QuestProgress) {
        if (progress.status == QuestStatus.COMPLETED || progress.status == QuestStatus.CLAIMED) return
        progress.progress = quest.objective.amount
        progress.status = QuestStatus.COMPLETED
        progress.completedAt = TimeUtil.nowSeconds()
        markDirty(progress)
        updateStats(player, quest)
        quest.completeActions.forEach { runSimpleAction(player, it) }
        if (quest.completionMessage.isNotBlank()) {
            player.sendMessage(TextUtil.mini(messages.apply(quest.completionMessage, placeholders(player, quest, progress))))
        } else {
            messages.send(player, "quest-completed", placeholders(player, quest, progress))
        }
        if (quest.broadcastCompletion) {
            Bukkit.broadcast(TextUtil.mini(messages.apply(quest.completionMessage.ifBlank { "%player% completed %quest_display%." }, placeholders(player, quest, progress))))
        }
        if (quest.autoClaim) {
            claim(player, quest.id)
        }
    }

    fun claim(player: Player, questId: String): Boolean {
        val quest = questService.get(questId) ?: run {
            messages.send(player, "quest-not-found")
            return false
        }
        val progress = progress(player, quest)
        if (progress.status == QuestStatus.CLAIMED) {
            messages.send(player, "quest-already-claimed")
            return false
        }
        if (progress.status != QuestStatus.COMPLETED) {
            messages.send(player, "quest-not-completed")
            return false
        }
        rewardService.give(player, quest, placeholders(player, quest, progress))
        quest.claimActions.forEach { runSimpleAction(player, it) }
        progress.status = QuestStatus.CLAIMED
        progress.claimedAt = TimeUtil.nowSeconds()
        markDirty(progress)
        messages.send(player, "quest-claimed", placeholders(player, quest, progress))
        return true
    }

    fun reset(playerId: UUID, questId: String? = null) {
        if (questId == null) {
            progressCache[playerId]?.clear()
        } else {
            progressCache[playerId]?.remove(questId)
        }
        scheduler.async { playerRepository.deleteProgress(playerId, questId) }
    }

    fun save(playerId: UUID) {
        val progress = progressCache[playerId]?.values?.toList().orEmpty()
        val stats = statsCache[playerId]
        scheduler.async {
            progress.forEach(playerRepository::saveProgress)
            if (stats != null) playerRepository.saveStats(stats)
        }
    }

    fun saveDirty() {
        val snapshot = dirty.values.toList()
        dirty.clear()
        val stats = statsCache.values.toList()
        scheduler.async {
            snapshot.forEach(playerRepository::saveProgress)
            stats.forEach(playerRepository::saveStats)
        }
    }

    fun saveAllBlocking() {
        progressCache.values.flatMap { it.values }.forEach(playerRepository::saveProgress)
        statsCache.values.forEach(playerRepository::saveStats)
    }

    fun getProgress(playerId: UUID, questId: String): QuestProgress? = progressCache[playerId]?.get(questId)

    fun getProgressValue(playerId: UUID, questId: String): Int = getProgress(playerId, questId)?.progress ?: 0

    fun allProgress(playerId: UUID): List<QuestProgress> = progressCache[playerId]?.values?.toList().orEmpty()

    fun stats(playerId: UUID): PlayerQuestStats? = statsCache[playerId]

    fun completedCount(playerId: UUID): Int = statsCache[playerId]?.completedTotal ?: 0

    fun track(player: Player, questId: String): Boolean {
        val quest = questService.get(questId) ?: return false
        progress(player, quest)
        trackedCache[player.uniqueId] = questId
        scheduler.async { questRepository.setTrackedQuest(player.uniqueId, questId) }
        messages.send(player, "quest-tracked", placeholders(player, quest, progress(player, quest)))
        return true
    }

    fun untrack(player: Player) {
        trackedCache.remove(player.uniqueId)
        scheduler.async { questRepository.clearTrackedQuest(player.uniqueId) }
        messages.send(player, "quest-untracked")
    }

    fun trackedQuest(playerId: UUID): String? = trackedCache[playerId]

    fun placeholders(player: Player, quest: Quest, progress: QuestProgress = progress(player, quest)): Map<String, String> {
        val stats = statsCache[player.uniqueId]
        val tracked = trackedQuest(player.uniqueId)?.let { questService.get(it)?.displayName } ?: "None"
        return mapOf(
            "%player%" to player.name,
            "%quest_id%" to quest.id,
            "%quest_display%" to quest.displayName,
            "%quest_description%" to quest.description,
            "%quest_type%" to quest.objective.type.name,
            "%quest_category%" to quest.category.name,
            "%quest_status%" to progress.status.name,
            "%quest_progress%" to NumberUtil.whole(progress.progress),
            "%quest_required%" to NumberUtil.whole(quest.objective.amount),
            "%quest_progress_bar%" to progressBar(progress.progress, quest.objective.amount),
            "%quest_progress_percent%" to NumberUtil.percent(progress.progress, quest.objective.amount),
            "%quest_reset_time%" to (progress.resetAt?.toString() ?: "Never"),
            "%quest_time_left%" to (progress.resetAt?.let { TimeUtil.formatDuration(it - TimeUtil.nowSeconds()) } ?: "Never"),
            "%daily_completed%" to NumberUtil.whole(stats?.completedDaily ?: 0),
            "%weekly_completed%" to NumberUtil.whole(stats?.completedWeekly ?: 0),
            "%total_completed%" to NumberUtil.whole(stats?.completedTotal ?: 0),
            "%daily_streak%" to NumberUtil.whole(stats?.currentDailyStreak ?: 0),
            "%best_daily_streak%" to NumberUtil.whole(stats?.bestDailyStreak ?: 0),
            "%tracked_quest%" to tracked
        )
    }

    private fun updateStats(player: Player, quest: Quest) {
        val stats = statsCache.computeIfAbsent(player.uniqueId) { PlayerQuestStats(player.uniqueId, player.name, 0, 0, 0, 0, 0, null) }
        stats.playerName = player.name
        stats.completedTotal += 1
        when (quest.category) {
            QuestCategory.DAILY -> {
                stats.completedDaily += 1
                streakService.markDailyCompletion(stats)
            }
            QuestCategory.WEEKLY -> stats.completedWeekly += 1
            else -> Unit
        }
    }

    private fun progressBar(progress: Int, required: Int): String {
        val filled = if (required <= 0) 0 else ((progress.toDouble() / required) * 10).toInt().coerceIn(0, 10)
        return "<green>${"|".repeat(filled)}<dark_gray>${"|".repeat(10 - filled)}"
    }

    private fun markDirty(progress: QuestProgress) {
        dirty["${progress.playerId}:${progress.questId}"] = progress
    }

    private fun runSimpleAction(player: Player, action: String) {
        val parts = action.split(":", limit = 2)
        when (parts[0].uppercase()) {
            "SOUND" -> SoundUtil.play(player, parts.getOrElse(1) { "" })
            "MESSAGE" -> player.sendMessage(TextUtil.mini(parts.getOrElse(1) { "" }))
            "COMMAND" -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parts.getOrElse(1) { "" }.replace("%player%", player.name))
        }
    }
}
