package dev.kairo.kairoquests

import dev.kairo.kairoquests.command.KairoQuestsCommand
import dev.kairo.kairoquests.command.QuestCommand
import dev.kairo.kairoquests.config.ConfigManager
import dev.kairo.kairoquests.database.DatabaseManager
import dev.kairo.kairoquests.database.MigrationManager
import dev.kairo.kairoquests.database.PlayerQuestRepository
import dev.kairo.kairoquests.database.QuestRepository
import dev.kairo.kairoquests.gui.GuiManager
import dev.kairo.kairoquests.hook.PlaceholderApiHook
import dev.kairo.kairoquests.hook.VaultHook
import dev.kairo.kairoquests.listener.BlockListener
import dev.kairo.kairoquests.listener.CraftListener
import dev.kairo.kairoquests.listener.EntityListener
import dev.kairo.kairoquests.listener.FishingListener
import dev.kairo.kairoquests.listener.InventoryListener
import dev.kairo.kairoquests.listener.PlayerListener
import dev.kairo.kairoquests.model.QuestType
import dev.kairo.kairoquests.service.QuestProgressService
import dev.kairo.kairoquests.service.QuestService
import dev.kairo.kairoquests.service.ResetService
import dev.kairo.kairoquests.service.RewardService
import dev.kairo.kairoquests.service.StreakService
import dev.kairo.kairoquests.util.MessageService
import dev.kairo.kairoquests.util.SchedulerUtil
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID

class KairoQuestsPlugin : JavaPlugin() {
    lateinit var configManager: ConfigManager
        private set
    lateinit var questService: QuestService
        private set
    lateinit var progressService: QuestProgressService
        private set
    private lateinit var database: DatabaseManager
    private lateinit var scheduler: SchedulerUtil
    private lateinit var vaultHook: VaultHook
    private var saveTask = -1
    private var playtimeTask = -1

    override fun onEnable() {
        scheduler = SchedulerUtil(this)
        configManager = ConfigManager(this).also { it.load() }
        val messages = MessageService(configManager.messages)
        database = DatabaseManager(this).also { it.connect() }
        MigrationManager(database).migrate()

        vaultHook = VaultHook(this).also { if (configManager.main.config.getBoolean("hooks.vault", true)) it.load() }

        questService = QuestService(configManager.quests).also { it.reload() }
        val playerRepository = PlayerQuestRepository(database)
        val questRepository = QuestRepository(database)
        progressService = QuestProgressService(
            questService,
            playerRepository,
            questRepository,
            ResetService(configManager),
            StreakService(),
            RewardService(messages, vaultHook),
            messages,
            scheduler
        )
        val guiManager = GuiManager(this, configManager.guis, questService, progressService, messages)

        registerCommands(guiManager, messages)
        registerListeners(guiManager)
        registerTasks()
        registerPlaceholders()
        Bukkit.getOnlinePlayers().forEach(progressService::load)
    }

    override fun onDisable() {
        if (::progressService.isInitialized) progressService.saveAllBlocking()
        if (saveTask != -1) Bukkit.getScheduler().cancelTask(saveTask)
        if (playtimeTask != -1) Bukkit.getScheduler().cancelTask(playtimeTask)
        if (::database.isInitialized) database.close()
    }

    fun addProgress(playerId: UUID, questId: String, amount: Int): Boolean = progressService.addProgress(playerId, questId, amount)

    fun setProgress(playerId: UUID, questId: String, amount: Int): Boolean = progressService.setProgress(playerId, questId, amount)

    fun completeQuest(playerId: UUID, questId: String): Boolean = progressService.completeQuest(playerId, questId)

    fun getProgress(playerId: UUID, questId: String): Int = progressService.getProgressValue(playerId, questId)

    fun getCompletedQuestCount(playerId: UUID): Int = progressService.completedCount(playerId)

    fun addCustomProgress(playerId: UUID, target: String, amount: Int = 1): Boolean {
        val player = Bukkit.getPlayer(playerId) ?: return false
        progressService.addMatching(player, QuestType.CUSTOM, target, amount)
        return true
    }

    private fun registerCommands(guiManager: GuiManager, messages: MessageService) {
        val questCommand = QuestCommand(guiManager, questService, progressService, messages)
        getCommand("quests")?.setExecutor(questCommand)
        getCommand("quests")?.tabCompleter = questCommand
        getCommand("daily")?.setExecutor(questCommand)
        getCommand("weekly")?.setExecutor(questCommand)

        val adminCommand = KairoQuestsCommand(configManager, questService, progressService, messages, vaultHook)
        getCommand("kairoquests")?.setExecutor(adminCommand)
        getCommand("kairoquests")?.tabCompleter = adminCommand
    }

    private fun registerListeners(guiManager: GuiManager) {
        server.pluginManager.registerEvents(BlockListener(progressService), this)
        server.pluginManager.registerEvents(EntityListener(progressService), this)
        server.pluginManager.registerEvents(PlayerListener(progressService), this)
        server.pluginManager.registerEvents(InventoryListener(guiManager), this)
        server.pluginManager.registerEvents(CraftListener(progressService), this)
        server.pluginManager.registerEvents(FishingListener(progressService), this)
    }

    private fun registerTasks() {
        val saveSeconds = configManager.main.config.getLong("settings.save-interval-seconds", 120L).coerceAtLeast(30L)
        saveTask = scheduler.repeatingAsync(saveSeconds * 20L, saveSeconds * 20L) { progressService.saveDirty() }
        playtimeTask = scheduler.repeating(20L * 60L, 20L * 60L) {
            Bukkit.getOnlinePlayers().forEach { progressService.addMatching(it, QuestType.PLAY_TIME, "SECONDS", 60) }
        }
    }

    private fun registerPlaceholders() {
        if (!configManager.main.config.getBoolean("hooks.placeholderapi", true)) return
        if (server.pluginManager.isPluginEnabled("PlaceholderAPI")) {
            PlaceholderApiHook(this).register()
        }
    }
}
