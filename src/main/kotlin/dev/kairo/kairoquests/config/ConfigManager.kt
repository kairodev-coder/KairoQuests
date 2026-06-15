package dev.kairo.kairoquests.config

import org.bukkit.plugin.java.JavaPlugin

class ConfigManager(private val plugin: JavaPlugin) {
    val main = FileConfig(plugin, "config.yml")
    val messages = MessageConfig(plugin)
    val quests = QuestConfig(plugin)
    val guis = GuiConfig(plugin)

    fun load() {
        plugin.dataFolder.mkdirs()
        main.reload()
        messages.reload()
        quests.reload()
        guis.reload()
    }

    fun reload() = load()
}
