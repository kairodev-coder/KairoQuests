package dev.kairo.kairoquests.config

import dev.kairo.kairoquests.gui.GuiItem
import org.bukkit.plugin.java.JavaPlugin

class GuiConfig(plugin: JavaPlugin) : FileConfig(plugin, "guis.yml") {
    fun title(gui: String): String = config.getString("$gui.title", "<dark_gray>Quests") ?: "<dark_gray>Quests"

    fun size(gui: String): Int = config.getInt("$gui.size", 54).let { (it / 9).coerceIn(1, 6) * 9 }

    fun borderMaterial(): String = config.getString("settings.border-material", "BLACK_STAINED_GLASS_PANE") ?: "BLACK_STAINED_GLASS_PANE"

    fun fillerName(): String = config.getString("settings.filler-name", "<dark_gray>") ?: "<dark_gray>"

    fun questSlots(): List<Int> {
        return config.getIntegerList("settings.quest-slots").ifEmpty {
            listOf(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34)
        }
    }

    fun item(gui: String, key: String): GuiItem? {
        val path = "$gui.items.$key"
        if (!config.isConfigurationSection(path)) return null
        return GuiItem(
            material = config.getString("$path.material", "BOOK") ?: "BOOK",
            displayName = config.getString("$path.display-name", key) ?: key,
            lore = config.getStringList("$path.lore"),
            slot = config.getInt("$path.slot", 0),
            slots = config.getIntegerList("$path.slots"),
            amount = config.getInt("$path.amount", 1),
            customModelData = config.getInt("$path.custom-model-data", 0).takeIf { it > 0 },
            enchanted = config.getBoolean("$path.enchanted", false),
            hideFlags = config.getBoolean("$path.hide-flags", true),
            skullOwner = config.getString("$path.skull-owner"),
            leftClickActions = config.getStringList("$path.left-click-actions"),
            rightClickActions = config.getStringList("$path.right-click-actions"),
            shiftLeftClickActions = config.getStringList("$path.shift-left-click-actions"),
            permission = config.getString("$path.permission")
        )
    }

    fun items(gui: String): List<GuiItem> {
        val section = config.getConfigurationSection("$gui.items") ?: return emptyList()
        return section.getKeys(false).mapNotNull { item(gui, it) }
    }
}
