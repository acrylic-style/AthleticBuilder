package xyz.acrylicstyle.athleticBuilder.commands

import org.bukkit.ChatColor
import org.bukkit.entity.Player
import xyz.acrylicstyle.athleticBuilder.AthleticBuilderPlugin
import xyz.acrylicstyle.athleticBuilder.util.AthleticManager
import xyz.acrylicstyle.athleticBuilder.util.MutableAthleticPath
import xyz.acrylicstyle.tomeito_api.subcommand.PlayerSubCommandExecutor
import xyz.acrylicstyle.tomeito_api.subcommand.SubCommand

@SubCommand(name = "delete", usage = "/athletic delete <ID>", description = "アスレチックを削除します。")
class DeleteCommand: PlayerSubCommandExecutor() {
    override fun onCommand(player: Player, args: Array<String>) {
        if (args.isEmpty()) {
            player.sendMessage("${ChatColor.RED}IDを指定してください。")
            return
        }
        val id = args[0]
        if (!CreateCommand.ID_REGEX.matches(id)) {
            player.sendMessage("${ChatColor.RED}IDは英数字アンダーバーのみで英数字で始まり、英数字で終わる必要があります。")
            return
        }
        if (AthleticManager.getAthletic(id) == null) {
            player.sendMessage("${ChatColor.RED}指定したアスレチックは存在しません。")
            return
        }
        AthleticManager.deleteAthletic(id)
        player.sendMessage("${ChatColor.GREEN}アスレチック${ChatColor.YELLOW}$id${ChatColor.GREEN}を削除しました。")
    }
}
