package dev.kairo.kairoquests.gui

import dev.kairo.kairoquests.util.ItemUtil
import dev.kairo.kairoquests.util.MessageService
import dev.kairo.kairoquests.util.TextUtil
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

class GuiItemBuilder(private val messages: MessageService) {
    @Suppress("DEPRECATION")
    fun fromConfig(item: GuiItem, placeholders: Map<String, String>): ItemStack {
        val material = ItemUtil.material(item.material)
        val stack = ItemStack(material, item.amount.coerceIn(1, 64))
        val meta = stack.itemMeta
        meta.displayName(TextUtil.mini(messages.apply(item.displayName, placeholders)))
        meta.lore(item.lore.map { TextUtil.mini(messages.apply(it, placeholders)) })
        item.customModelData?.let { meta.setCustomModelData(it) }
        if (item.hideFlags) meta.addItemFlags(*ItemFlag.entries.toTypedArray())
        if (item.enchanted) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true)
        }
        if (meta is SkullMeta && item.skullOwner != null) {
            val owner = messages.apply(item.skullOwner, placeholders)
            if (owner.isNotBlank() && owner != "%player%") {
                meta.owningPlayer = Bukkit.getOfflinePlayer(owner)
            }
        }
        stack.itemMeta = meta
        return stack
    }

    fun filler(material: String, name: String): ItemStack {
        val stack = ItemStack(ItemUtil.material(material, Material.BLACK_STAINED_GLASS_PANE))
        val meta = stack.itemMeta
        meta.displayName(TextUtil.mini(name))
        meta.addItemFlags(*ItemFlag.entries.toTypedArray())
        stack.itemMeta = meta
        return stack
    }
}
