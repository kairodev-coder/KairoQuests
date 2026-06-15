package dev.kairo.kairoquests.util

import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

object ItemUtil {
    fun material(name: String, fallback: Material = Material.BOOK): Material {
        return Material.matchMaterial(name) ?: fallback
    }

    @Suppress("DEPRECATION")
    fun simple(material: String, name: String, lore: List<String>, amount: Int = 1, customModelData: Int? = null): ItemStack {
        val item = ItemStack(material(material), amount.coerceIn(1, 64))
        val meta = item.itemMeta
        meta.displayName(TextUtil.mini(name))
        meta.lore(lore.map(TextUtil::mini))
        customModelData?.takeIf { it > 0 }?.let { meta.setCustomModelData(it) }
        meta.addItemFlags(*ItemFlag.entries.toTypedArray())
        item.itemMeta = meta
        return item
    }
}
