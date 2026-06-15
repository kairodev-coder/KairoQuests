package dev.kairo.kairoquests.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage

object TextUtil {
    private val miniMessage = MiniMessage.miniMessage()

    fun mini(text: String): Component {
        return miniMessage.deserialize(text).decoration(TextDecoration.ITALIC, false)
    }

    fun stripMini(text: String): String {
        return miniMessage.stripTags(text)
    }
}
