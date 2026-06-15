package dev.kairo.kairoquests.util

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class SchedulerUtil(private val plugin: JavaPlugin) {
    fun sync(block: () -> Unit) {
        Bukkit.getScheduler().runTask(plugin, Runnable(block))
    }

    fun async(block: () -> Unit) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable(block))
    }

    fun later(delayTicks: Long, block: () -> Unit) {
        Bukkit.getScheduler().runTaskLater(plugin, Runnable(block), delayTicks)
    }

    fun repeating(delayTicks: Long, periodTicks: Long, block: () -> Unit): Int {
        return Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, block, delayTicks, periodTicks)
    }

    @Suppress("DEPRECATION")
    fun repeatingAsync(delayTicks: Long, periodTicks: Long, block: () -> Unit): Int {
        return Bukkit.getScheduler().scheduleAsyncRepeatingTask(plugin, block, delayTicks, periodTicks)
    }
}
