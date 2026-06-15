package dev.kairo.kairoquests.hook

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class VaultHook(private val plugin: JavaPlugin) {
    private var economy: Any? = null
    private var economyClass: Class<*>? = null
        private set

    val enabled: Boolean
        get() = economy != null

    fun load() {
        if (!plugin.server.pluginManager.isPluginEnabled("Vault")) return
        val clazz = runCatching { Class.forName("net.milkbowl.vault.economy.Economy") }.getOrNull() ?: return
        economyClass = clazz
        economy = Bukkit.getServicesManager().getRegistration(clazz)?.provider
    }

    fun deposit(player: Player, amount: Double): Boolean {
        val provider = economy ?: return false
        return runCatching {
            val response = provider.javaClass.getMethod("depositPlayer", org.bukkit.OfflinePlayer::class.java, Double::class.javaPrimitiveType).invoke(provider, player, amount)
            response.javaClass.getMethod("transactionSuccess").invoke(response) as Boolean
        }.getOrDefault(false)
    }
}
