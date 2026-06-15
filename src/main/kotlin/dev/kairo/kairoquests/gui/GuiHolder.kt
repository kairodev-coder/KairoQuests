package dev.kairo.kairoquests.gui

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

class GuiHolder(
    val type: String,
    val page: Int = 0,
    val questId: String? = null
) : InventoryHolder {
    private lateinit var inventory: Inventory
    val actions: MutableMap<Int, GuiSlotActions> = mutableMapOf()

    override fun getInventory(): Inventory = inventory

    fun attach(inventory: Inventory) {
        this.inventory = inventory
    }
}

data class GuiSlotActions(
    val left: List<String> = emptyList(),
    val right: List<String> = emptyList(),
    val shiftLeft: List<String> = emptyList()
)
