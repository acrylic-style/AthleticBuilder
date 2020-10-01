package xyz.acrylicstyle.athleticBuilder.commands

import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import xyz.acrylicstyle.athleticBuilder.util.AthleticManager
import xyz.acrylicstyle.tomeito_api.subcommand.PlayerSubCommandExecutor
import xyz.acrylicstyle.tomeito_api.subcommand.SubCommand

@SubCommand(name = "list", usage = "/athletic list", description = "アスレチック一覧を表示します。")
class ListCommand: PlayerSubCommandExecutor() {
    override fun onCommand(player: Player, args: Array<String>) {
        player.sendMessage("${ChatColor.GREEN}アスレチック一覧${ChatColor.YELLOW}(クリックでテレポート)")
        AthleticManager.getAthletics().valuesList().forEach { config ->
            val text = TextComponent("${ChatColor.YELLOW} - ${ChatColor.GREEN}アスレチック「${config.getAthleticName()}」${ChatColor.GRAY}(ID: ${config.id})")
            val initialLocation = config.getInitialLocation()!!
            text.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp ${initialLocation.x} ${initialLocation.y} ${initialLocation.z} ${initialLocation.yaw} ${initialLocation.pitch}")
            player.spigot().sendMessage(text)
        }
    }
}
