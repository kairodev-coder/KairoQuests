package dev.kairo.kairoquests.util

import org.bukkit.Sound
import org.bukkit.entity.Player

object SoundUtil {
    @Suppress("DEPRECATION")
    fun play(player: Player, soundName: String, volume: Float = 1.0f, pitch: Float = 1.0f) {
        val sound = runCatching { Sound.valueOf(soundName.uppercase()) }.getOrNull() ?: return
        player.playSound(player.location, sound, volume, pitch)
    }
}
