package xyz.acrylicstyle.athleticbuilder.commands

import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import xyz.acrylicstyle.athleticbuilder.util.AthleticManager

object ListCommand : SubCommand("list", "/athletic list", "アスレチック一覧を表示します。") {
    override fun onCommand(player: Player, args: Array<String>) {
        player.sendMessage("${ChatColor.GREEN}アスレチック一覧${ChatColor.YELLOW}(クリックでテレポート)")
        AthleticManager.getAthletics().values.forEach { config ->
            val text = TextComponent("${ChatColor.YELLOW} - ${ChatColor.GREEN}アスレチック「${config.getAthleticName()}」${ChatColor.GRAY}(ID: ${config.id})")
            val initialLocation = config.getInitialLocation()!!
            text.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/minecraft:tp @s ${initialLocation.x} ${initialLocation.y} ${initialLocation.z} ${initialLocation.yaw} ${initialLocation.pitch}")
            player.spigot().sendMessage(text)
        }
    }
}
