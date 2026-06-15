package dev.kairo.kairoquests.config

import org.bukkit.plugin.java.JavaPlugin

class MessageConfig(plugin: JavaPlugin) : FileConfig(plugin, "messages.yml") {
    var prefix: String = ""
        private set

    override fun reload() {
        super.reload()
        prefix = config.getString("prefix", "") ?: ""
    }

    fun get(key: String): String = config.getString(key, "<red>Missing message: $key") ?: "<red>Missing message: $key"

    fun prefixed(key: String): String = prefix + get(key)
}
