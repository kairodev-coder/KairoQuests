package dev.kairo.kairoquests.config

import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

open class FileConfig(
    protected val plugin: JavaPlugin,
    private val fileName: String
) {
    protected val file: File = File(plugin.dataFolder, fileName)
    var config: YamlConfiguration = YamlConfiguration()
        private set

    fun saveDefault() {
        if (!file.exists()) {
            plugin.saveResource(fileName, false)
        }
    }

    open fun reload() {
        saveDefault()
        config = YamlConfiguration.loadConfiguration(file)
    }
}
