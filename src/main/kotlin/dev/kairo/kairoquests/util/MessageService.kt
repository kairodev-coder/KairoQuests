package dev.kairo.kairoquests.util

import dev.kairo.kairoquests.config.MessageConfig
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class MessageService(private val messages: MessageConfig) {
    fun send(sender: CommandSender, key: String, placeholders: Map<String, String> = emptyMap()) {
        sender.sendMessage(TextUtil.mini(apply(messages.prefixed(key), placeholders)))
    }

    fun sendPlain(sender: CommandSender, key: String, placeholders: Map<String, String> = emptyMap()) {
        sender.sendMessage(TextUtil.mini(apply(messages.get(key), placeholders)))
    }

    fun actionbar(player: Player, text: String, placeholders: Map<String, String> = emptyMap()) {
        player.sendActionBar(TextUtil.mini(apply(text, placeholders)))
    }

    fun apply(text: String, placeholders: Map<String, String>): String {
        var output = text
        placeholders.forEach { (key, value) -> output = output.replace(key, value) }
        return output
    }
}
